package med.voll.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		// Aqui o Spring Boot inicia o servidor Tomcat sozinho embutido!
		SpringApplication.run(ApiApplication.class, args);

	}

}
