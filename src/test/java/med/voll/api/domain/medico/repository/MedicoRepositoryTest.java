package med.voll.api.domain.medico.repository;

import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.endereco.dto.DadosEnderecoDto;
import med.voll.api.domain.medico.Especialidade;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.dto.DadosCadastroMedicoDto;
import med.voll.api.domain.paciente.Paciente;
import med.voll.api.domain.paciente.dto.DadosCadastroPacienteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTE DE INTEGRAÇÃO DO REPOSITÓRIO (não é teste de unidade!)
 * ---------------------------------------------------------------
 * @DataJpaTest -> sobe só a camada JPA (não o contexto Spring inteiro),
 *   configura um EntityManager de teste e, por padrão, roda tudo dentro
 *   de uma transação que é desfeita (rollback) ao final de cada teste.
 *   Isso significa: cada teste começa com o banco "limpo".
 *
 * @AutoConfigureTestDatabase(replace = NONE) -> diz para o Spring: "não troque
 *   o banco configurado por um H2 embutido automático, use o banco que eu
 *   configurei no application-test.properties/yml".
 *
 * @ActiveProfiles("test") -> carrega as configurações do perfil "test"
 *   (ex: application-test.properties), que normalmente aponta pra um
 *   banco de testes (pode ser H2, pode ser um Postgres de teste, etc).
 *
 * IMPORTANTE: aqui estamos testando a QUERY customizada do repositório,
 * não a lógica de negócio do Service. A regra "só considero médico livre"
 * está escrita dentro da própria query JPQL/SQL — por isso só dá pra
 * validar rodando contra um banco de verdade, e não com Mockito.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MedicoRepositoryTest {

    @Autowired
    private MedicoRepository medicoRepository; // é o que estamos testando de fato

    @Autowired
    private TestEntityManager testEntityManager; // usado só para preparar (persistir) dados de teste

    // =================================================================================================
    // ============================================ TESTES ============================================
    // =================================================================================================

    @Test
    @DisplayName("Deveria devolver null quando unico medico cadastrado não está disponivel na data")
    void escolherMedicoAleatorioLivreNaDataCenario1() {

        // GIVEN/ARRANGE: calcula a próxima segunda-feira às 10h,
        // só para ter uma data fixa e previsível para o teste
        var proximaSegundaAs10 = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(10, 0);

        // WHEN/ACT: cadastra 1 médico, 1 paciente, e marca uma consulta
        // EXATAMENTE nesse horário -> ou seja, o médico já está ocupado
        var medico = cadastrarMedico("Medico", "medico@voll.med", "123456", Especialidade.CARDIOLOGIA);
        var paciente = cadastrarPaciente("Paciente", "paciente@email.com", "0000000000000");
        cadastrarConsulta(medico, paciente, proximaSegundaAs10);

        // THEN/ASSERT: como o único médico cadastrado já tem consulta
        // nesse horário, a query não deve encontrar ninguém livre -> null
        var medicoLivre = medicoRepository.escolherMedicoAleatorioLivreNaData(Especialidade.CARDIOLOGIA, proximaSegundaAs10);
        assertThat(medicoLivre).isNull();
    }

    @Test
    @DisplayName("Deveria devolver medico quando ele estiver disponivel na data")
    void escolherMedicoAleatorioLivreNaDataCenario2() {

        // GIVEN/ARRANGE: mesma data de referência do cenário anterior
        var proximaSegundaAs10 = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(10, 0);

        // WHEN/ACT: cadastra o médico, mas NÃO marca nenhuma consulta
        // -> ou seja, ele está livre nesse horário
        var medico = cadastrarMedico("Medico", "medico@voll.med", "123456", Especialidade.CARDIOLOGIA);

        // THEN/ASSERT: como não há consulta ocupando o horário,
        // a query deve encontrar justamente esse médico
        var medicoLivre = medicoRepository.escolherMedicoAleatorioLivreNaData(Especialidade.CARDIOLOGIA, proximaSegundaAs10);
        assertThat(medicoLivre).isEqualTo(medico);
    }

    @Test
    @DisplayName("Não deveria devolver medico inativo, mesmo que ele esteja livre na data")
    void escolherMedicoAleatorioLivreNaDataCenario3() {

        // GIVEN/ARRANGE
        var proximaSegundaAs10 = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(10, 0);

        // WHEN/ACT: cadastra o médico e IMEDIATAMENTE o inativa.
        // Ele está "livre" no horário (sem consulta marcada), mas
        // não deveria contar porque m.ativo = true é um filtro da query.
        //
        // ATENÇÃO: aqui eu chutei o metodo `excluir()` (padrão comum
        // do curso Voll.med, que seta ativo = false). Ajuste para o
        // nome real do método/atributo que existe na sua entidade Medico.
        var medico = cadastrarMedico("Medico", "medico@voll.med", "123456", Especialidade.CARDIOLOGIA);
        medico.excluir();

        // THEN/ASSERT: mesmo estando livre, não deve ser retornado
        var medicoLivre = medicoRepository.escolherMedicoAleatorioLivreNaData(Especialidade.CARDIOLOGIA, proximaSegundaAs10);
        assertThat(medicoLivre).isNull();
    }

    @Test
    @DisplayName("Não deveria devolver medico de especialidade diferente da solicitada")
    void escolherMedicoAleatorioLivreNaDataCenario4() {

        // GIVEN/ARRANGE
        var proximaSegundaAs10 = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(10, 0);

        // WHEN/ACT: cadastra um médico de ORTOPEDIA, livre no horário,
        // mas a busca abaixo pede CARDIOLOGIA -> não deve casar
        cadastrarMedico("Medico Ortopedista", "orto@voll.med", "654321", Especialidade.ORTOPEDIA);

        // THEN/ASSERT: nenhum médico de CARDIOLOGIA está cadastrado,
        // então mesmo havendo um médico livre (de outra especialidade),
        // o retorno deve ser null
        var medicoLivre = medicoRepository.escolherMedicoAleatorioLivreNaData(Especialidade.CARDIOLOGIA, proximaSegundaAs10);
        assertThat(medicoLivre).isNull();
    }

    // =================================================================================================
    // ================================ MÉTODOS AUXILIARES (SETUP) ===================================
    // ============= Não são testes! Só existem para montar cenários (massa de dados) =================
    // =================================================================================================

    /**
     * Persiste uma Consulta direto no banco de teste, "pulando" as regras
     * de negócio do Service (ex: antecedência mínima de 30min). Isso é
     * proposital: aqui queremos só o dado no banco, não validar a regra
     * de agendamento — essa regra é testada em outro lugar (teste do Service).
     */
    private void cadastrarConsulta(Medico medico, Paciente paciente, LocalDateTime data) {
        testEntityManager.persist(new Consulta(null, medico, paciente, data));
    }

    /**
     * Cria e persiste um Médico de teste a partir dos dados básicos passados.
     */
    private Medico cadastrarMedico(String nome, String email, String crm, Especialidade especialidade) {
        var medico = new Medico(dadosMedico(nome, email, crm, especialidade));
        testEntityManager.persist(medico);
        return medico;
    }

    /**
     * Cria e persiste um Paciente de teste a partir dos dados básicos passados.
     */
    private Paciente cadastrarPaciente(String nome, String email, String cpf) {
        var paciente = new Paciente(dadosPaciente(nome, email, cpf));
        testEntityManager.persist(paciente);
        return paciente;
    }

    /**
     * Monta o DTO de cadastro de médico. Telefone e endereço são fixos/fake
     * porque não importam para o que estamos testando aqui.
     */
    private DadosCadastroMedicoDto dadosMedico(String nome, String email, String crm, Especialidade especialidade) {
        return new DadosCadastroMedicoDto(
                nome,
                email,
                "61999999999",
                crm,
                especialidade,
                dadosEndereco()
        );
    }

    /**
     * Monta o DTO de cadastro de paciente. Mesma ideia: só o essencial
     * para o cenário de teste, o resto é valor fixo/fake.
     */
    private DadosCadastroPacienteDto dadosPaciente(String nome, String email, String cpf) {
        return new DadosCadastroPacienteDto(
                nome,
                email,
                "61999999999",
                cpf,
                dadosEndereco()
        );
    }

    /**
     * Endereço fake reutilizado em médico e paciente — só para satisfazer
     * o construtor, já que endereço não é o foco deste teste.
     */
    private DadosEnderecoDto dadosEndereco(){
        return new DadosEnderecoDto(
                "Rua xpto",
                "bairro",
                "00000000",
                "São Paulo",
                "SP",
                "99",
                null
        );
    }
}