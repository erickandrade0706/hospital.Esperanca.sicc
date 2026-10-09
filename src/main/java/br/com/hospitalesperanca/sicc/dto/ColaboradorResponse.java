package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.Colaborador;
import br.com.hospitalesperanca.sicc.model.Status;

import java.time.LocalDate;

/** Colaborador devolvido ao front: mesmos campos do colaborador.model.ts, mais o funcaoId. */
public record ColaboradorResponse(
        Long id,
        String matricula,
        String nome,
        String cpf,
        Long funcaoId,
        String funcao,
        String setor,
        LocalDate admissao,
        String contato,
        Status status) {

    public static ColaboradorResponse de(Colaborador colaborador) {
        return new ColaboradorResponse(
                colaborador.getId(),
                colaborador.getMatricula(),
                colaborador.getNome(),
                colaborador.getCpf(),
                colaborador.getFuncao() == null ? null : colaborador.getFuncao().getId(),
                colaborador.getNomeFuncao(),
                colaborador.getSetor(),
                colaborador.getAdmissao(),
                colaborador.getContato(),
                colaborador.getStatus());
    }
}
