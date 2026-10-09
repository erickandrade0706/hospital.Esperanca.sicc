package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.model.Treinamento;
import br.com.hospitalesperanca.sicc.service.TreinamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD de treinamentos (tela Treinamentos) - Administrador e Técnico de Segurança. */
@RestController
@RequestMapping("/api/treinamentos")
public class TreinamentoController {

    private final TreinamentoService treinamentoService;

    public TreinamentoController(TreinamentoService treinamentoService) {
        this.treinamentoService = treinamentoService;
    }

    @GetMapping
    public List<Treinamento> listar() {
        return treinamentoService.listar();
    }

    @GetMapping("/{id}")
    public Treinamento buscar(@PathVariable Long id) {
        return treinamentoService.buscar(id);
    }

    /** @Valid dispara a validação dos campos obrigatórios antes de chegar no serviço. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Treinamento criar(@Valid @RequestBody Treinamento dados) {
        return treinamentoService.criar(dados);
    }

    @PutMapping("/{id}")
    public Treinamento atualizar(@PathVariable Long id, @Valid @RequestBody Treinamento dados) {
        return treinamentoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        treinamentoService.excluir(id);
    }
}
