package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.dto.StatusUsuarioRequest;
import br.com.hospitalesperanca.sicc.dto.UsuarioRequest;
import br.com.hospitalesperanca.sicc.dto.UsuarioResponse;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Usuários do sistema (Configurações) - acesso só de Administrador (ver SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return usuarioService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest dados) {
        return usuarioService.criar(dados);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody UsuarioRequest dados,
                                     @AuthenticationPrincipal Usuario logado) {
        return usuarioService.atualizar(id, dados, logado);
    }

    /** Ativar/inativar (botão da tela de Configurações). */
    @PatchMapping("/{id}/status")
    public UsuarioResponse alterarStatus(@PathVariable Long id,
                                         @Valid @RequestBody StatusUsuarioRequest dados,
                                         @AuthenticationPrincipal Usuario logado) {
        return usuarioService.alterarStatus(id, dados.ativo(), logado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        usuarioService.excluir(id, logado);
    }
}
