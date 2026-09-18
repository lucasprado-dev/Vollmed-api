package med.voll.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.consulta.dto.DadosDetalhamentoConsultaDto;
import med.voll.api.domain.consulta.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @PostMapping
    @Transactional
    public ResponseEntity agendar(@RequestBody @Valid DadosAgendamentoConsultaDto dto) {
        //Validação das regras de negocio
        consultaService.agendar(dto);
        return ResponseEntity.ok(new DadosDetalhamentoConsultaDto(null, null, null, null));
    }
}
