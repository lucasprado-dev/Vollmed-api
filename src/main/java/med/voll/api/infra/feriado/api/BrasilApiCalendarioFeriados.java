package med.voll.api.infra.feriado.api;

import med.voll.api.domain.consulta.validacoes.CalendarioFeriados;
import med.voll.api.infra.feriado.dto.FeriadoDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class BrasilApiCalendarioFeriados implements CalendarioFeriados {

    // RestClient: o "navegador" do Java. Você monta a requisição e ele devolve o corpo já convertido.
    private final RestClient restClient = RestClient.create("https://brasilapi.com.br/api");

    @Override
    public boolean ehFeriado(LocalDate data) {
        List<FeriadoDto> feriados = restClient
                .get()
                .uri("/feriados/v1/{ano}", data.getYear())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FeriadoDto>>() {});

        return feriados.stream().anyMatch(f -> f.date().isEqual(data));
    }

}
