package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.dto.MovimentacaoEpiRequest;
import br.com.hospitalesperanca.sicc.dto.MovimentacaoEpiResponse;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEpi;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.service.MovimentacaoEpiService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Entregas, devoluções e trocas de EPI (tela Gestão de EPIs) - Administrador e Técnico de Segurança. */
@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoEpiController {

    private final MovimentacaoEpiService movimentacaoEpiService;

    public MovimentacaoEpiController(MovimentacaoEpiService movimentacaoEpiService) {
        this.movimentacaoEpiService = movimentacaoEpiService;
    }

    /**
     * Filtros opcionais, ex.: /api/movimentacoes?tipo=Entrega&dataInicio=2026-08-01&dataFim=2026-08-31&setor=Enfermagem
     */
    @GetMapping
    public List<MovimentacaoEpiResponse> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) String colaborador) {
        return movimentacaoEpiService.listar(TipoMovimentacaoEpi.deTexto(tipo), dataInicio, dataFim, setor, colaborador);
    }

    @GetMapping("/{id}")
    public MovimentacaoEpiResponse buscar(@PathVariable Long id) {
        return movimentacaoEpiService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEpiResponse registrar(@Valid @RequestBody MovimentacaoEpiRequest dados,
                                             @AuthenticationPrincipal Usuario logado) {
        return movimentacaoEpiService.registrar(dados, logado);
    }

    @PutMapping("/{id}")
    public MovimentacaoEpiResponse atualizar(@PathVariable Long id,
                                             @Valid @RequestBody MovimentacaoEpiRequest dados,
                                             @AuthenticationPrincipal Usuario logado) {
        return movimentacaoEpiService.atualizar(id, dados, logado);
    }

    /** Conclui uma movimentação que estava Pendente. */
    @PatchMapping("/{id}/concluir")
    public MovimentacaoEpiResponse concluir(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return movimentacaoEpiService.concluir(id, logado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        movimentacaoEpiService.excluir(id);
    }
}
