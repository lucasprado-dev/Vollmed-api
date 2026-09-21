package med.voll.api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity //personalização de segurança
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean // Registra esse metodo como um Bean gerenciado pelo Spring — ele vai construir
    // e disponibilizar a SecurityFilterChain no contexto da aplicação
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // HttpSecurity é o objeto que permite configurar toda a segurança das requisições HTTP.
        // O Spring injeta essa instância automaticamente como parâmetro do método.
        return http
                // Desabilita a proteção contra CSRF (Cross-Site Request Forgery).
                // Faz sentido desabilitar aqui porque essa API é stateless (sem sessão/cookies),
                // então o ataque de CSRF (que depende de sessão de navegador) não se aplica.
                // AbstractHttpConfigurer::disable é a forma "method reference" de dizer
                // "desabilita essa configuração", equivalente a (csrf -> csrf.disable())
                .csrf(AbstractHttpConfigurer::disable)

                // Configura o gerenciamento de sessão da aplicação
                .sessionManagement(session -> session
                        // Define a política de criação de sessão como STATELESS,
                        // ou seja: o Spring NÃO vai criar nem usar HttpSession.
                        // Cada requisição precisa se autenticar sozinha (ex: via token JWT),
                        // sem depender de estado guardado no servidor entre requisições.
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura as regras de autorização das requisições HTTP
                .authorizeHttpRequests(authorize -> authorize
                                // Libera sem autenticação apenas o POST em /login (rota de autenticação).
                                // Todo o restante das requisições exige usuário autenticado.
                                // ⚠️ Se precisar liberar outras rotas futuramente (ex: /cadastro, /public/**),
                                // adicione novos .requestMatchers(...).permitAll() ANTES do .anyRequest().authenticated().
                                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                                .anyRequest().authenticated()
//                        .anyRequest().permitAll() // versão antiga: liberava tudo (só fase inicial/teste)
                )

                // Adiciona um filtro customizado (securityFilter) ANTES do filtro padrão
                // UsernamePasswordAuthenticationFilter na cadeia de filtros do Spring Security.
                // Isso significa: antes de tentar autenticar via usuário/senha, o Spring
                // primeiro passa a requisição pelo seu filtro (geralmente usado para
                // validar/interceptar um token JWT e autenticar o usuário manualmente).
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)

                // Finaliza a configuração e constrói o objeto SecurityFilterChain
                .build();
    }

    // Expõe o AuthenticationManager do Spring Security como um Bean.
    // É ele quem efetivamente valida as credenciais (usuário/senha) durante o login,
    // usando o UserDetailsService e o PasswordEncoder configurados no projeto.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Define o algoritmo usado para criptografar (hash) as senhas antes de salvar no banco
    // e também para comparar a senha digitada no login com o hash armazenado.
    // BCrypt é o padrão recomendado: aplica salt automático e é resistente a ataques de força bruta.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
