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

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;
    // Class que dispara o metodo de autenticação

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dto) {
        var authenticationTokenoken = new UsernamePasswordAuthenticationToken(dto.login(), dto.senha());
        var authentication = manager.authenticate(authenticationTokenoken);
        var tokenJwt = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        return  ResponseEntity.ok(new TokenDadosJwtDto(tokenJwt));
    }
}
