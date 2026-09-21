package med.voll.api.controller;

import jakarta.validation.Valid;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.dto.DadosAutenticacao;
import med.voll.api.infra.security.TokenService;
import med.voll.api.infra.security.dto.TokenDadosJwtDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController: controller que devolve dados (JSON) direto no corpo da resposta, sem view/HTML.
// @RequestMapping("/login"): todos os endpoints desta classe começam com /login.
// Este é o ponto de entrada da autenticação: recebe login e senha e devolve o token JWT.
@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    // AuthenticationManager: a classe que dispara o processo de autenticação do Spring Security.
    // Ele é quem chama o AutenticacaoService (loadUserByUsername) e compara a senha com o hash do banco.
    // Você não escreve essa lógica: só entrega as credenciais e ele diz se são válidas ou não.
    // Analogia: é o "segurança da portaria". Você entrega o documento e ele decide se libera ou barra.
    @Autowired
    private AuthenticationManager manager;
    // Class que dispara o metodo de autenticação

    // TokenService: classe sua que gera (e valida) o token JWT.
    // Analogia: é a "máquina que imprime o crachá" depois que o segurança liberou a entrada.
    @Autowired
    private TokenService tokenService;

    // @PostMapping: responde a POST /login (POST porque envia credenciais no corpo, nunca na URL).
    // @RequestBody: converte o JSON recebido no DTO DadosAutenticacao (login e senha).
    // @Valid: roda as validações do DTO (@NotBlank etc.). Se falhar, cai no
    // MethodArgumentNotValidException do TratadorDeErros e devolve 400.
    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dto) {

        // 1) Empacota login e senha no formato que o Spring Security entende.
        // Esse objeto ainda NÃO está autenticado: é só uma "solicitação de autenticação".
        var authenticationTokenoken = new UsernamePasswordAuthenticationToken(dto.login(), dto.senha());

        // 2) Dispara a autenticação. Por baixo dos panos:
        //    manager -> AutenticacaoService.loadUserByUsername(login) -> UsuarioRepository.findByLogin
        //    -> compara a senha recebida com o hash em getPassword().
        // Se algo estiver errado, lança exceção (BadCredentialsException etc.) e o método para aqui;
        // o TratadorDeErros converte isso em 401.
        // Se der certo, devolve um Authentication já preenchido com o usuário logado.
        var authentication = manager.authenticate(authenticationTokenoken);

        // 3) Pega o usuário autenticado (getPrincipal) e gera o token JWT para ele.
        // O cast (Usuario) é necessário porque getPrincipal() devolve Object genérico,
        // mas sabemos que é a sua entidade Usuario (foi ela que o AutenticacaoService retornou).
        var tokenJwt = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        // 4) Devolve 200 OK com o token dentro de um DTO.
        // O cliente guarda esse token e o envia nas próximas requisições no cabeçalho:
        // Authorization: Bearer <token>
        return  ResponseEntity.ok(new TokenDadosJwtDto(tokenJwt));
    }
}