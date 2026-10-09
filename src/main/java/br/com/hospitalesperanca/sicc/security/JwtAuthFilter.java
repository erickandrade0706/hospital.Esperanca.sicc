package br.com.hospitalesperanca.sicc.security;

import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Executa antes de cada requisição: lê o cabeçalho "Authorization: Bearer token",
 * valida o token e identifica o usuário logado.
 * Equivale ao authGuard do Angular, só que no servidor (não dá para burlar pelo navegador).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            String token = cabecalho.substring(PREFIXO.length()).trim();

            // Busca o usuário no banco a cada requisição: se ele foi inativado ou mudou de perfil,
            // o token antigo deixa de valer na hora.
            Optional<Usuario> usuario = jwtService.obterIdUsuario(token)
                    .flatMap(usuarioRepository::findById)
                    .filter(Usuario::isAtivo);

            if (usuario.isPresent()) {
                Usuario logado = usuario.get();
                UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                        logado,
                        null,
                        List.of(new SimpleGrantedAuthority(logado.getPerfil().getRole())));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        }

        filterChain.doFilter(request, response);
    }
}
