package br.com.hospitalesperanca.sicc.controller;

import br.com.hospitalesperanca.sicc.dto.ColaboradorRequest;
import br.com.hospitalesperanca.sicc.dto.ColaboradorResponse;
import br.com.hospitalesperanca.sicc.service.ColaboradorService;
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

/** CRUD de colaboradores (Cadastro > Colaboradores) - Administrador e RH. */
@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @GetMapping
    public List<ColaboradorResponse> listar() {
        return colaboradorService.listar();
    }

    @GetMapping("/{id}")
    public ColaboradorResponse buscar(@PathVariable Long id) {
        return colaboradorService.buscar(id);
    }

    /** @Valid dispara a validação dos campos obrigatórios antes de chegar no serviço. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ColaboradorResponse criar(@Valid @RequestBody ColaboradorRequest dados) {
        return colaboradorService.criar(dados);
    }

    @PutMapping("/{id}")
    public ColaboradorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ColaboradorRequest dados) {
        return colaboradorService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        colaboradorService.excluir(id);
    }
}
