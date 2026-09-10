package med.voll.api.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity //personalização de segurança
public class SecurityConfigurations {

    @Bean // Registra esse metodo como um Bean gerenciado pelo Spring — ele vai construir
          // e disponibilizar a SecurityFilterChain no contexto da aplicação
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // HttpSecurity é o objeto que permite configurar toda a segurança das requisições HTTP.
        // O Spring injeta essa instância automaticamente como parâmetro do metodo.
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

//                // Configura as regras de autorização das requisições HTTP
//                .authorizeHttpRequests(authorize -> authorize
//                        // Libera QUALQUER requisição (qualquer endpoint) sem exigir autenticação.
//                        // ⚠️ Atenção: isso é só para fase inicial/teste do projeto.
//                        // Depois que os endpoints de login/autenticação existirem, o normal é
//                        // restringir isso, liberando só rotas específicas (ex: /login) e
//                        // exigindo autenticação para o resto.
//                        .anyRequest().permitAll()
//                )

                // Finaliza a configuração e constrói o objeto SecurityFilterChain
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
