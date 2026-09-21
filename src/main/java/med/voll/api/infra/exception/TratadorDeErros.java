package med.voll.api.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice: transforma a classe em um "tratador global de erros".
// Toda exceção que estourar em qualquer controller passa por aqui antes de virar resposta HTTP.
// Analogia: é o "balcão de reclamações" da API. Em vez de cada controller ter seu try/catch,
// os erros caem todos neste balcão, que decide qual status HTTP e qual mensagem devolver.
@RestControllerAdvice
public class TratadorDeErros {

    // @ExceptionHandler: "quando essa exceção acontecer, execute este método".
    // EntityNotFoundException é lançada pelo JPA quando você busca um registro que não existe
    // (ex: getReferenceById com um id inválido). Devolve 404 sem corpo.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity tratarErro404() {
        return ResponseEntity.notFound().build();
    }

    // Erro de validação do Bean Validation (@NotBlank, @Email, @Valid nos DTOs).
    // Acontece quando o JSON chega, mas algum campo não respeita as regras.
    // Devolve 400 com a lista de campos inválidos e a mensagem de cada um.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity tratarErro400(MethodArgumentNotValidException ex) {
        var erros = ex.getFieldErrors();  // lista de FieldError: um por campo que falhou
        // Converte cada FieldError no record DadosErroValidacao (campo + mensagem) e devolve como JSON
        return ResponseEntity.badRequest().body(erros.stream().map(DadosErroValidacao::new).toList());
    }

    // Exceção própria do projeto, lançada nas regras de negócio
    // (ex: "médico não disponível nesse horário", "consulta com menos de 30 min de antecedência").
    // Devolve 400 com a mensagem que você colocou na exceção.
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity tratarErroRegraNegocio(ValidacaoException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // O corpo da requisição veio quebrado: JSON malformado, tipo errado num campo, corpo vazio, etc.
    // O Spring nem consegue montar o DTO. Devolve 400 com a mensagem técnica do erro.
    // (Mesmo nome do método de cima, mas o parâmetro é diferente: isso é sobrecarga, é válido.)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity tratarErro400(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // ---- SEGURANÇA ----

    // Login com usuário ou senha errados. Devolve 401 (não autenticado).
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity tratarErroBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }

    // Qualquer outra falha de autenticação (conta bloqueada, token inválido, etc.).
    // BadCredentialsException é "filha" desta classe, então o Spring escolhe sempre o handler
    // mais específico: senha errada cai no de cima, o resto cai aqui. Também devolve 401.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity tratarErroAuthentication() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha na autenticação");
    }

    // Usuário está autenticado, mas não tem permissão para o recurso (ex: perfil sem role de ADMIN).
    // Devolve 403. Diferença de 401: 401 = "quem é você?", 403 = "sei quem é, mas você não pode".
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity tratarErroAcessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
    }

    // ---- REDE DE SEGURANÇA ----

    // Pega qualquer exceção que não tenha handler específico acima. Devolve 500.
    // Analogia: é o "resto" do switch/case, o default.
    @ExceptionHandler(Exception.class)
    public ResponseEntity tratarErro500(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro: " +ex.getLocalizedMessage());
    }

    // Record privado, usado só aqui dentro, para padronizar o JSON dos erros de validação.
    // Resultado na resposta: [{"campo": "email", "mensagem": "must be a well-formed email address"}]
    private record DadosErroValidacao(String campo, String mensagem) {
        // Construtor auxiliar: recebe o FieldError do Spring e extrai o nome do campo e a mensagem.
        // O this(...) chama o construtor principal do record.
        public DadosErroValidacao(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}