package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.model.Epi;
import br.com.hospitalesperanca.sicc.service.EpiService;
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

/** CRUD de EPIs (Cadastro > EPIs) - Administrador e Técnico de Segurança. */
@RestController
@RequestMapping("/api/epis")
public class EpiController {

    private final EpiService epiService;

    public EpiController(EpiService epiService) {
        this.epiService = epiService;
    }

    @GetMapping
    public List<Epi> listar() {
        return epiService.listar();
    }

    @GetMapping("/{id}")
    public Epi buscar(@PathVariable Long id) {
        return epiService.buscar(id);
    }

    /** @Valid dispara a validação dos campos obrigatórios antes de chegar no serviço. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Epi criar(@Valid @RequestBody Epi dados) {
        return epiService.criar(dados);
    }

    @PutMapping("/{id}")
    public Epi atualizar(@PathVariable Long id, @Valid @RequestBody Epi dados) {
        return epiService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        epiService.excluir(id);
    }
}
