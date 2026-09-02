package med.voll.api.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;
import med.voll.api.dto.DadosAtualizacaoPacienteDto;
import med.voll.api.dto.DadosCadastroPacienteDto;

@Entity
@Table(name = "pacientes")
@Getter // Lombok gera automaticamente os métodos getX() para todos os atributos da classe
@NoArgsConstructor // Lombok gera um construtor vazio (sem argumentos) — exigido pelo JPA para instanciar a entidade via reflection
@AllArgsConstructor // Lombok gera um construtor com todos os atributos como parâmetros
@EqualsAndHashCode(of = "id") // Lombok gera equals() e hashCode() considerando apenas o campo "id" — evita comparar todos os atributos (importante em entidades JPA, para não cair em problemas com proxies/lazy loading)
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;

    @Embedded // Indica que este campo é composto por uma classe anotada com @Embeddable, cujos atributos serão incorporados (achatados) como colunas na própria tabela desta entidade — não gera relacionamento nem tabela separada
    private Endereco endereco;

    private Boolean ativo;

    public Paciente(DadosCadastroPacienteDto dto) {
        this.nome = dto.nome();
        this.email = dto.email();
        this.telefone = dto.telefone();
        this.cpf = dto.cpf();
        this.endereco = new Endereco(dto.endereco());
        this.ativo = true;
    }

    public void atualizarInformacoes(@Valid DadosAtualizacaoPacienteDto dto) {
        if (dto.nome() != null){
            this.nome = dto.nome();
        }
        if (dto.telefone() != null){
            this.telefone = dto.telefone();
        }
        if (dto.endereco() != null) {
            this.endereco.atualizarInformacoes(dto.endereco());
        }
    }

    public void excluir() {
        this.ativo = false;
    }
}