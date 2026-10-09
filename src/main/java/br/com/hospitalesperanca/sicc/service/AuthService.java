package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.dto.LoginRequest;
import br.com.hospitalesperanca.sicc.dto.LoginResponse;
import br.com.hospitalesperanca.sicc.dto.UsuarioResponse;
import br.com.hospitalesperanca.sicc.exception.ApiException;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.repository.UsuarioRepository;
import br.com.hospitalesperanca.sicc.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Login no servidor, com as mesmas regras do front end (login.ts e auth.ts) e proteções extras:
 *  1. identificação é e-mail válido OU matrícula numérica de 4 a 10 dígitos (mesmas regex do login.ts);
 *  2. busca por e-mail (sem diferenciar maiúsculas) ou matrícula, como no auth.ts;
 *  3. senha conferida com BCrypt (no front ela ficava em texto puro dentro do código);
 *  4. mensagem de erro única para "usuário não existe" e "senha errada" (não revela qual dos dois);
 *  5. bloqueio temporário após várias senhas erradas (ataque de força bruta);
 *  6. usuário inativo não entra.
 */
@Service
public class AuthService {

    // Mesmas expressões regulares do login.ts
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern MATRICULA = Pattern.compile("^\\d{4,10}$");

    // Mesma mensagem exibida pelo login.ts
    private static final String CREDENCIAIS_INVALIDAS =
            "Matrícula, e-mail ou senha inválidos. Verifique os dados informados.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final int maxTentativas;
    private final int minutosBloqueio;
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${sicc.login.max-tentativas}") int maxTentativas,
                       @Value("${sicc.login.minutos-bloqueio}") int minutosBloqueio) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.maxTentativas = maxTentativas;
        this.minutosBloqueio = minutosBloqueio;
        // Usado quando o usuário não existe, para a resposta demorar o mesmo tempo de uma senha errada
        this.hashFalso = passwordEncoder.encode("usuario-inexistente");
    }

    public LoginResponse login(LoginRequest dados) {
        String identificacao = dados.identificacao().trim().toLowerCase();
        boolean ehEmail = identificacao.contains("@");

        if (ehEmail ? !EMAIL.matcher(identificacao).matches() : !MATRICULA.matcher(identificacao).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Digite uma matrícula ou um e-mail válido.");
        }

        Optional<Usuario> encontrado = ehEmail
                ? usuarioRepository.findByEmailIgnoreCase(identificacao)
                : usuarioRepository.findByMatricula(identificacao);

        if (encontrado.isEmpty()) {
            passwordEncoder.matches(dados.senha(), hashFalso);
            throw new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
        }

        Usuario usuario = encontrado.get();
        LocalDateTime agora = LocalDateTime.now();

        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(agora)) {
            long minutos = Math.max(1, Duration.between(agora, usuario.getBloqueadoAte()).toMinutes() + 1);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente em " + minutos + " minuto(s).");
        }

        if (!passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            registrarFalha(usuario, agora);
            throw new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
        }

        if (!usuario.isAtivo()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Usuário inativo. Procure o administrador do sistema.");
        }

        // Login correto: zera o contador de falhas
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        return new LoginResponse(
                jwtService.gerarToken(usuario),
                "Bearer",
                jwtService.getExpiracaoMinutos(),
                UsuarioResponse.de(usuario));
    }

    private void registrarFalha(Usuario usuario, LocalDateTime agora) {
        int falhas = usuario.getTentativasFalhas() + 1;
        if (falhas >= maxTentativas) {
            usuario.setBloqueadoAte(agora.plusMinutes(minutosBloqueio));
            falhas = 0;
        }
        usuario.setTentativasFalhas(falhas);
        usuarioRepository.save(usuario);
    }
}
