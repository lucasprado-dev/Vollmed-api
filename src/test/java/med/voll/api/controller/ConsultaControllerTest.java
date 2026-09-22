package med.voll.api.controller;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.consulta.dto.DadosDetalhamentoConsultaDto;
import med.voll.api.domain.consulta.service.ConsultaService;
import med.voll.api.domain.medico.Especialidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;

// ============================================================
// CONFIGURAÇÃO DA CLASSE DE TESTE (isso NÃO é teste em si,
// é a "preparação do palco" antes de qualquer teste rodar)
// ============================================================

@SpringBootTest          // Sobe o contexto completo do Spring (como se a aplicação real estivesse rodando)
@AutoConfigureMockMvc     // Habilita o MockMvc: um "cliente HTTP falso" para simular requisições sem precisar
// subir um servidor de verdade na porta 8080
@AutoConfigureJsonTesters // Habilita os "JacksonTester", que ajudam a converter objetos Java <-> JSON nos testes
class ConsultaControllerTest {

    // ------------------------------------------------------------
    // FERRAMENTAS QUE O TESTE PRECISA (injetadas pelo Spring)
    // Isso também NÃO é teste, é o "kit de ferramentas" usado
    // dentro dos métodos de teste lá embaixo
    // ------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;
    // Simula chamadas HTTP (GET, POST, etc) para o seu Controller,
    // sem precisar de Postman, navegador ou servidor real rodando.

    @Autowired
    private JacksonTester<DadosAgendamentoConsultaDto> dadosAgendamentoConsultaDtoJson;
    // Converte o DTO de entrada (dados que o cliente envia pra agendar) em JSON,
    // pra simular o "corpo" (body) da requisição.

    @Autowired
    private JacksonTester<DadosDetalhamentoConsultaDto> dadosDetalhamentoConsultaDtoJson;
    // Converte o DTO de saída (dados que a API devolve depois de agendar) em JSON,
    // pra comparar com o que o Controller realmente respondeu.

    @MockitoBean
    private ConsultaService consultaService;
    // ATENÇÃO, esse é o pulo do gato: aqui a gente NÃO usa o service de verdade.
    // O Mockito cria um "dublê" (mock) do ConsultaService.
    // Isso porque aqui a gente quer testar SÓ o Controller (a porta de entrada HTTP),
    // e não se preocupar se a regra de negócio ou o banco estão funcionando.
    // Quem testa o Service/Repository de verdade são outras classes de teste.


    // ============================================================
    // OS TESTES DE FATO COMEÇAM AQUI (cada @Test é um cenário)
    // ============================================================

    @Test
    @DisplayName("Deveria devolver código HTTP 400 quando informações estão invalidas")
    @WithMockUser // simula que existe um usuário autenticado (senão a Security barraria antes de validar o corpo)
    void agendarCenario1() throws Exception {
        // CENÁRIO: faz um POST /consultas SEM enviar nenhum dado no corpo da requisição.

        var response = mockMvc.perform(post("/consultas"))
                .andReturn().getResponse();

        // EXPECTATIVA: como não mandamos nada, o Bean Validation (as anotações @NotNull etc
        // dentro do DTO) deve barrar a requisição e devolver 400 (Bad Request).
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver código HTTP 200 quando informações estão validas")
    @WithMockUser
    void agendarCenario2() throws Exception {

        // 1) Monta os dados de entrada válidos que "o cliente" enviaria
        var idMedico = 2l;
        var idPaciente = 5l;
        var data = LocalDateTime.now().plusHours(1);
        var especialidade = Especialidade.CARDIOLOGIA;

        // 2) Monta o que o Service (mockado) DEVERIA devolver se tudo desse certo
        var dadosDetalhamento = new DadosDetalhamentoConsultaDto(null, idMedico, idPaciente, data);

        // 3) Aqui é o "combinado" com o mock: "quando alguém chamar consultaService.agendar(qualquer coisa),
        //    finja que a resposta é 'dadosDetalhamento'". Isso substitui a lógica real do Service.
        when(consultaService.agendar(any())).thenReturn(dadosDetalhamento);

        // 4) Faz a requisição POST de verdade (via MockMvc), enviando o JSON do DTO de agendamento
        var response = mockMvc
                .perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosAgendamentoConsultaDtoJson.write(
                                new DadosAgendamentoConsultaDto(idMedico, idPaciente, data, especialidade)
                        ).getJson())
                )
                .andReturn().getResponse();

        // 5) Verifica se o status HTTP foi 200 (OK), como esperado para uma requisição válida
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        // 6) Converte o "dadosDetalhamento" esperado para JSON...
        var jsonEsperado = dadosDetalhamentoConsultaDtoJson.write(
                dadosDetalhamento
        ).getJson();

        // 7) ...e compara com o JSON que o Controller realmente devolveu no corpo da resposta.
        //    Se bater, o Controller está serializando e retornando os dados corretamente.
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }

    @Test
    @DisplayName("Deveria devolver código HTTP 401 quando a requisição não está autenticada")
    void agendarCenario3() throws Exception {
        // Perceba: SEM @WithMockUser aqui de propósito — é isso que estamos testando.
        // Ou seja: ninguém "logado" está fazendo essa requisição.

        var idMedico = 2l;
        var idPaciente = 5l;
        var data = LocalDateTime.now().plusHours(1);
        var especialidade = Especialidade.CARDIOLOGIA;

        var response = mockMvc
                .perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosAgendamentoConsultaDtoJson.write(
                                new DadosAgendamentoConsultaDto(idMedico, idPaciente, data, especialidade)
                        ).getJson())
                )
                .andReturn().getResponse();

        // EXPECTATIVA (segundo o @DisplayName): deveria vir 401 (Unauthorized).
        // NOTA: o código está comparando com FORBIDDEN (403), não UNAUTHORIZED (401).
        // Vale conferir com seu professor qual dos dois o Spring Security está
        // realmente devolvendo na configuração de vocês — 401 e 403 são coisas diferentes
        // (401 = "eu não sei quem você é"; 403 = "eu sei quem você é, mas você não pode").
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}