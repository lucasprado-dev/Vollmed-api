package med.voll.api.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import med.voll.api.domain.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filtro que intercepta TODAS as requisições que chegam na API, antes delas
// serem processadas pelos controllers. É aqui que a gente verifica se existe
// um token JWT válido e, se existir, autentica o usuário na requisição atual.
@Component
public class SecurityFilter extends OncePerRequestFilter {

    // Serviço responsável por validar o token JWT e extrair informações dele
    // (ex: descobrir de quem é o token - o "subject").
    @Autowired
    private TokenService tokenService;

    // Repositório usado para buscar no banco o usuário dono do token,
    // a partir do login extraído do JWT.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Metodo principal do filtro. O Spring Security executa esse metodo
    // uma vez a cada requisição (por isso "OncePerRequestFilter").
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Tenta extrair o token JWT do cabeçalho "Authorization" da requisição.
        var tokenJwt = recuperarToken(request);

        // Se veio um token na requisição, significa que o usuário está tentando
        // se autenticar (ex: acessando uma rota protegida).
        if (tokenJwt != null) {

            // Decodifica o token e extrai o "subject" - normalmente é o login
            // (ou e-mail/CPF) do usuário que foi salvo no token na hora do login.
            var subject = tokenService.getSubject(tokenJwt);

            // Busca no banco de dados o usuário correspondente a esse login.
            var usuario = usuarioRepository.findByLogin(subject);

            // Cria um objeto de autenticação do Spring Security, informando
            // quem é o usuário autenticado e quais são suas permissões (roles).
            // O segundo parâmetro (senha) é null porque, nesse ponto, a autenticação
            // já foi validada via token - não precisamos checar senha de novo.
            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

            // Registra essa autenticação no contexto de segurança do Spring.
            // A partir daqui, o Spring "sabe" que essa requisição está autenticada
            // e o usuário fica disponível, por exemplo, via @AuthenticationPrincipal.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Independente de ter autenticado ou não, deixa a requisição seguir
        // seu fluxo normal (chega no próximo filtro ou no controller).
        filterChain.doFilter(request, response);
    }

    // Metodo auxiliar que extrai o token JWT "puro" do cabeçalho Authorization.
    private String recuperarToken(HttpServletRequest request) {
        // O cabeçalho Authorization normalmente vem no formato: "Bearer <token>"
        var authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null) {
            // Remove o prefixo "Bearer " para sobrar só o token em si.
            return authorizationHeader.replace("Bearer ", "").trim();
        }

        // Se não veio o header, retorna null (requisição não autenticada).
        return null;
    }
}