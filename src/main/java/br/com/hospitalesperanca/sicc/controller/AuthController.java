package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.dto.LoginRequest;
import br.com.hospitalesperanca.sicc.dto.LoginResponse;
import br.com.hospitalesperanca.sicc.dto.UsuarioResponse;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Login por matrícula ou e-mail. Único endereço da API que não exige token. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return authService.login(dados);
    }

    /** Dados do usuário logado (útil para o front conferir se o token ainda vale). */
    @GetMapping("/me")
    public UsuarioResponse usuarioLogado(@AuthenticationPrincipal Usuario logado) {
        return UsuarioResponse.de(logado);
    }
}
