package med.voll.api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import med.voll.api.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Serviço responsável por tudo relacionado ao token JWT: criar (no login)
// e validar/ler (nas requisições autenticadas, usado pelo SecurityFilter).
@Service
public class TokenService {

    // Chave secreta usada para assinar e verificar o token.
    // Vem do application.properties/yml (propriedade "api.security.token.secret"),
    // pra não deixar esse valor sensível fixo no código.
    @Value("${api.security.token.secret}")
    private String secret;

    // Gera um novo token JWT para o usuário que acabou de fazer login.
    public String gerarToken(Usuario usuario) {
        try {
            // Define o algoritmo de assinatura (HMAC256), usando a secret como chave.
            var algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("API Voll.med")           // identifica quem emitiu o token
                    .withSubject(usuario.getLogin())        // "dono" do token - o login do usuário
                    .withExpiresAt(dataExpiracao())         // define quando o token vai expirar
                    .sign(algorithm);                       // assina o token com a chave secreta

        } catch (JWTCreationException exception){
            // Se der algum problema ao montar/assinar o token, converte pra uma
            // exceção de runtime (evita obrigar quem chama a tratar checked exception).
            throw new RuntimeException("Erro ao gerar token jwt", exception);
        }
    }

    // Valida um token JWT recebido e retorna o "subject" (login do usuário) contido nele.
    // É esse metodo que o SecurityFilter chama pra descobrir quem está fazendo a requisição.
    public String getSubject(String tokenJWT){
        try {
            // Recria o mesmo algoritmo/chave usados na geração, pra conseguir validar a assinatura.
            var algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("API Voll.med")   // garante que o token foi emitido por essa API
                    .build()
                    .verify(tokenJWT)             // verifica assinatura + expiração + issuer;
                                                  // lança exceção se algo estiver inválido
                    .getSubject();                // extrai o login do usuário de dentro do token

        } catch (JWTVerificationException exception){
            // Token inválido, expirado, ou assinado com outra chave - trata como erro genérico.
            throw new RuntimeException("Token JWT invalido ou expirado");
        }

    }

    // Calcula a data/hora de expiração do token: 2 horas a partir de agora,
    // fixando o fuso horário em -03:00 (horário de Brasília).
    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}