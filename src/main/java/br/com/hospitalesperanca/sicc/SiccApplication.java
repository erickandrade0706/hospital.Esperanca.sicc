package br.com.hospitalesperanca.sicc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Ponto de entrada do back end do SICC.
 * No IntelliJ: clique com o botão direito nesta classe e escolha "Run".
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SiccApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiccApplication.class, args);
    }
}
