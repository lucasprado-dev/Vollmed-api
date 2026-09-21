package med.voll.api.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// @Entity: diz ao JPA/Hibernate que essa classe representa uma tabela do banco.
// @Table(name = "usuarios"): define o nome exato da tabela (sem isso, usaria "usuario").
@Entity
@Table(name = "usuarios")

// ---- LOMBOK (gera código repetitivo em tempo de compilação) ----
// @Getter: cria getId(), getLogin() e getSenha() automaticamente.
@Getter
// @NoArgsConstructor: cria o construtor vazio. O JPA EXIGE isso para conseguir
// instanciar a entidade quando lê os dados do banco.
@NoArgsConstructor
// @AllArgsConstructor: cria um construtor com todos os campos (id, login, senha). Útil em testes.
@AllArgsConstructor
// @EqualsAndHashCode(of = "id"): dois Usuario são considerados iguais se tiverem o mesmo id,
// independente dos outros campos.
@EqualsAndHashCode(of = "id")

// implements UserDetails: é o contrato do Spring Security para "o que é um usuário".
// Ao implementar essa interface, o Spring consegue autenticar direto com a sua entidade,
// sem precisar de uma classe intermediária.
// Analogia: é o formato de "crachá" que o Spring Security sabe ler.
public class Usuario implements UserDetails {

    // @Id: chave primária da tabela.
    // GenerationType.IDENTITY: quem gera o id é o banco (auto increment).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos mapeados para as colunas "login" e "senha" (mesmo nome do atributo).
    private String login;
    private String senha;

    // ---- MÉTODOS DO UserDetails ----
    // O Spring Security chama esses métodos para saber quem é o usuário e o que ele pode fazer.

    // Lista de permissões (roles) do usuário.
    // Aqui está fixo: todo mundo tem ROLE_USER. O prefixo "ROLE_" é a convenção do Spring.
    // Se um dia tiver perfis diferentes (ADMIN, MEDICO), é aqui que a lógica entra.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // Devolve a senha para o Spring comparar com a que veio no login.
    // O Spring exige o nome getPassword(), mas o seu campo se chama "senha", então este método faz a ponte.
    // @Nullable (do JSpecify) é só um aviso de que o retorno pode ser null. Não muda o comportamento.
    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    // Mesma ideia: o Spring pede "username", o seu campo é "login".
    @Override
    public String getUsername() {
        return login;
    }

    // Os 4 métodos abaixo controlam o estado da conta.
    // Todos retornam true, ou seja, a conta nunca expira, nunca bloqueia e está sempre ativa.
    // Se retornassem false, o Spring recusaria o login mesmo com senha correta.

    // A conta expirou? (true = não expirou)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // A conta está bloqueada? (true = não está bloqueada)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // A senha expirou? (true = não expirou)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // A conta está ativa/habilitada? (true = ativa)
    @Override
    public boolean isEnabled() {
        return true;
    }
}