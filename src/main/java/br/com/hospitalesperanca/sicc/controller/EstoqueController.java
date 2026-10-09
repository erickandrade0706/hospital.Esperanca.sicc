package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.dto.EstoqueItemResponse;
import br.com.hospitalesperanca.sicc.dto.EstoqueMovimentacaoRequest;
import br.com.hospitalesperanca.sicc.dto.EstoqueMovimentacaoResponse;
import br.com.hospitalesperanca.sicc.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Estoque de EPIs (tela Estoque) - Administrador e Técnico de Segurança. */
@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    /** Saldo atual de cada EPI. */
    @GetMapping
    public List<EstoqueItemResponse> listar() {
        return estoqueService.listar();
    }

    /** Histórico de entradas e saídas. */
    @GetMapping("/movimentacoes")
    public List<EstoqueMovimentacaoResponse> historico() {
        return estoqueService.historico();
    }

    /** Entrada ou saída manual. */
    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public EstoqueMovimentacaoResponse registrar(@Valid @RequestBody EstoqueMovimentacaoRequest dados) {
        return estoqueService.registrar(dados);
    }
}
