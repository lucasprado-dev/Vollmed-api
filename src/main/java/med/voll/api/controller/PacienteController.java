package med.voll.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.dto.DadosAtualizacaoMedicoDto;
import med.voll.api.dto.DadosAtualizacaoPacienteDto;
import med.voll.api.dto.DadosCadastroPacienteDto;
import med.voll.api.dto.DadosListagemPacienteDto;
import med.voll.api.model.Paciente;
import med.voll.api.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository repository;

    @PostMapping
    @Transactional
    public void cadastrar(@RequestBody @Valid DadosCadastroPacienteDto dto){
        var paciente = new Paciente(dto);
        repository.save(paciente);
    }

    @GetMapping
    public Page<DadosListagemPacienteDto> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao)
                .map(DadosListagemPacienteDto::new);
    }

    @PutMapping
    @Transactional
    public void atualizar(@RequestBody @Valid DadosAtualizacaoPacienteDto dto){
        var paciente = repository.getReferenceById(dto.id());
        paciente.atualizarInformacoes(dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void excluir(@PathVariable Long id){
        var medico = repository.getReferenceById(id);
        medico.excluir();
    }
}
