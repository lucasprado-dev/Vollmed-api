package med.voll.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.dto.DadosAtualizacaoMedicoDto;
import med.voll.api.domain.medico.dto.DadosCadastroMedicoDto;
import med.voll.api.domain.medico.dto.DadosDetalhamentoMedico;
import med.voll.api.domain.medico.dto.DadosListagemMedicoDto;
import med.voll.api.domain.medico.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository repository;

    @PostMapping
    @Transactional
    // UriComponentsBuilder: ferramenta do Spring pra montar a URL do novo recurso criado
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroMedicoDto dto, UriComponentsBuilder uriBuilder) {
        var medico = new Medico(dto);
        repository.save(medico);

        // Monta a URI do recurso recém-criado, ex: /medicos/5
        // uriBuilder.path define o caminho, e buildAndExpand substitui o {id} pelo ID real
        var uri = uriBuilder.path("medicos/{id}").buildAndExpand(medico.getId()).toUri();

        // Retorna HTTP 201 (Created) com:
        // - Header "Location" apontando pra URI do novo recurso (boa prática REST)
        // - Corpo da resposta contendo os dados do médico cadastrado, via DTO de detalhamento
        //   (evita expor a entidade diretamente, só os campos que o DTO define)
        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemMedicoDto>> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {

        // Busca os médicos no banco já paginados, conforme os parâmetros recebidos (page, size, sort)
        var page = repository.findAllByAtivoTrue(paginacao)

                // Mapeia cada Medico para DadosListagemMedicoDto via method reference,
                // que chama o construtor DadosListagemMedicoDto(Medico medico)
                .map(DadosListagemMedicoDto::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity buscaId(@PathVariable Long id){
        var medico = repository.getReferenceById(id);
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    @PutMapping
    @Transactional
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizacaoMedicoDto dto){
        var medico = repository.getReferenceById(dto.id());
        medico.atualizarInformacoes(dto);
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity excluir(@PathVariable Long id){
//        repository.deleteById(id);
        var medico = repository.getReferenceById(id);
        medico.excluir();
        return ResponseEntity.noContent().build();
    }
}
