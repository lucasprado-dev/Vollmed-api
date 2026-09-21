package med.voll.api.domain.usuario.service;

import med.voll.api.domain.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// @Service: registra a classe como um bean do Spring (camada de serviço).
// Assim o Spring cria uma instância dela na inicialização e a disponibiliza para quem precisar.
//
// implements UserDetailsService: é o contrato do Spring Security para "como buscar um usuário pelo login".
// O Spring Security encontra esse bean sozinho e o usa toda vez que alguém tenta se autenticar.
// Analogia: é o "atendente do cadastro". Quando chega um login, o Spring Security pergunta a ele:
// "existe alguém com esse nome? Me traga a ficha completa". Depois é o próprio Spring que confere a senha.
@Service
public class AutenticacaoService implements UserDetailsService {

    // @Autowired: injeta automaticamente o repository (o Spring cria e entrega pronto).
    // É por ele que a classe consulta a tabela "usuarios" no banco.
    @Autowired
    private UsuarioRepository repository;

    // Único metodo do contrato. É chamado pelo Spring Security durante o login.
    // O parâmetro "username" é o login que veio na requisição (o Spring chama de username,
    // mas no seu projeto corresponde ao campo "login" da entidade Usuario).
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca no banco o usuário com esse login (findByLogin é um metodo derivado:
        // o Spring Data gera o SELECT ... WHERE login = ? só pelo nome do metodo).
        // O retorno é um UserDetails, e como sua entidade Usuario implementa essa interface,
        // ela pode ser devolvida diretamente.
        // Depois disso, o Spring Security compara a senha do login com o hash de getPassword().
        return repository.findByLogin(username);
    }
}