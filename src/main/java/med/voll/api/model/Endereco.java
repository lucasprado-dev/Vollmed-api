package med.voll.api.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import med.voll.api.dto.DadosEnderecoDto;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {
    private String logradouro;
    private String bairro;
    private String cep;
    private String cidade;
    private String uf;
    private String numero;
    private String complemento;

    public Endereco(DadosEnderecoDto endereco) {
        this.logradouro = endereco.logradouro();
        this.bairro = endereco.bairro();
        this.cep = endereco.cep();
        this.cidade = endereco.cidade();
        this.uf = endereco.uf();
        this.numero = endereco.numero();
        this.complemento = endereco.complemento();
    }

    public void atualizarInformacoes(DadosEnderecoDto dto) {
        if (dto.logradouro() != null) {
            this.logradouro = dto.logradouro();
        }
        if (dto.bairro() != null) {
            this.bairro = dto.bairro();
        }
        if (dto.cep() != null) {
            this.cep = dto.cep();
        }
        if (dto.cidade() != null) {
            this.cidade = dto.cidade();
        }
        if (dto.uf() != null) {
            this.uf = dto.uf();
        }
        if (dto.numero() != null) {
            this.numero = dto.numero();
        }
        if (dto.complemento() != null) {
            this.complemento = dto.complemento();
        }
    }
}
