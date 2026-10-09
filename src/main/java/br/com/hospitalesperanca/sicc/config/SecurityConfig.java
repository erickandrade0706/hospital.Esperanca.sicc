package br.com.hospitalesperanca.sicc.config;

import br.com.hospitalesperanca.sicc.exception.ErroResponse;
import br.com.hospitalesperanca.sicc.repository.UsuarioRepository;
import br.com.hospitalesperanca.sicc.security.JwtAuthFilter;
import br.com.hospitalesperanca.sicc.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Regras de acesso da API - espelham o que o front end já faz:
 *  - authGuard  -> toda rota (menos o login) exige usuário autenticado;
 *  - adminGuard -> Configurações/Usuários só para Administrador;
 *  - cadastro.ts e sidebar.ts -> Colaboradores para Administrador e RH;
 *                                EPIs, Funções, Gestão de EPIs, Estoque e Treinamentos
 *                                para Administrador e Técnico de Segurança (o RH só consulta as funções).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String RH = "RH";
    private static final String TECNICO = "TECNICO_SEGURANCA";

    @Bean
    public SecurityFilterChain filtrosDeSeguranca(HttpSecurity http,
                                                  JwtService jwtService,
                                                  UsuarioRepository usuarioRepository,
                                                  ObjectMapper objectMapper) throws Exception {
        http
                // API sem sessão/cookie: a identificação vem do token em cada requisição, por isso CSRF não se aplica
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll() // página de erro padrão (ex.: endereço inexistente = 404)
                        .requestMatchers("/api/usuarios/**").hasRole(ADMIN)
                        .requestMatchers("/api/colaboradores/**").hasAnyRole(ADMIN, RH)
                        // RH só lê as funções: precisa delas no select do cadastro de colaborador
                        .requestMatchers(HttpMethod.GET, "/api/funcoes/**").hasAnyRole(ADMIN, RH, TECNICO)
                        .requestMatchers(
                                "/api/epis/**",
                                "/api/funcoes/**",
                                "/api/movimentacoes/**",
                                "/api/estoque/**",
                                "/api/treinamentos/**").hasAnyRole(ADMIN, TECNICO)
                        .anyRequest().authenticated())
                .exceptionHandling(erros -> erros
                        // sem token ou token inválido/expirado
                        .authenticationEntryPoint((request, response, ex) -> escreverErro(response, objectMapper,
                                HttpStatus.UNAUTHORIZED, "Acesso negado. Faça login para continuar."))
                        // logado, mas o perfil não tem permissão
                        .accessDeniedHandler((request, response, ex) -> escreverErro(response, objectMapper,
                                HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para acessar este recurso.")))
                .addFilterBefore(new JwtAuthFilter(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt: a senha é gravada como um hash com "sal" e não pode ser revertida. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Libera o front end Angular (outra porta/origem) para chamar a API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${sicc.cors.origens}") List<String> origens) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origens);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    private static void escreverErro(HttpServletResponse response, ObjectMapper objectMapper,
                                     HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ErroResponse.de(status, mensagem));
    }
}
