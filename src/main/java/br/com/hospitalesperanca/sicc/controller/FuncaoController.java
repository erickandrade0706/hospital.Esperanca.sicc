package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.model.Funcao;
import br.com.hospitalesperanca.sicc.service.FuncaoService;
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

/** CRUD de funções (Cadastro > Funções) - Administrador e Técnico de Segurança. */
@RestController
@RequestMapping("/api/funcoes")
public class FuncaoController {

    private final FuncaoService funcaoService;

    public FuncaoController(FuncaoService funcaoService) {
        this.funcaoService = funcaoService;
    }

    @GetMapping
    public List<Funcao> listar() {
        return funcaoService.listar();
    }

    @GetMapping("/{id}")
    public Funcao buscar(@PathVariable Long id) {
        return funcaoService.buscar(id);
    }

    /** @Valid dispara a validação dos campos obrigatórios antes de chegar no serviço. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Funcao criar(@Valid @RequestBody Funcao dados) {
        return funcaoService.criar(dados);
    }

    @PutMapping("/{id}")
    public Funcao atualizar(@PathVariable Long id, @Valid @RequestBody Funcao dados) {
        return funcaoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        funcaoService.excluir(id);
    }
}
