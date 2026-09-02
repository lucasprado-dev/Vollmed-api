package med.voll.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.dto.DadosAtualizacaoMedicoDto;
import med.voll.api.dto.DadosCadastroMedicoDto;
import med.voll.api.dto.DadosListagemMedicoDto;
import med.voll.api.model.Medico;
import med.voll.api.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository repository;

    @PostMapping
    @Transactional
    public void cadastrar(@RequestBody @Valid DadosCadastroMedicoDto dto){
        var medico = new Medico(dto);
        repository.save(medico);
    }

    @GetMapping
    public Page<DadosListagemMedicoDto> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
        // Busca os médicos no banco já paginados, conforme os parâmetros recebidos (page, size, sort)
        return repository.findAllByAtivoTrue(paginacao)
                // Mapeia cada Medico para DadosListagemMedicoDto via method reference,
                // que chama o construtor DadosListagemMedicoDto(Medico medico)
                .map(DadosListagemMedicoDto::new);
    }

    @PutMapping
    @Transactional
    public void atualizar(@RequestBody @Valid DadosAtualizacaoMedicoDto dto){
        var medico = repository.getReferenceById(dto.id());
        medico.atualizarInformacoes(dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void excluir(@PathVariable Long id){
//        repository.deleteById(id);
        var medico = repository.getReferenceById(id);
        medico.excluir();
    }
}
