package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.Epi;

import java.time.LocalDate;

/** Linha da tabela de estoque. Status "Baixo" quando a quantidade é menor ou igual ao mínimo (igual ao estoque.ts). */
public record EstoqueItemResponse(
        Long epiId,
        String codigo,
        String nome,
        String categoria,
        int quantidade,
        int minimo,
        String status,
        LocalDate validade,
        boolean vencido) {

    public static EstoqueItemResponse de(Epi epi) {
        int minimo = epi.getEstoqueMinimo() == null ? 0 : epi.getEstoqueMinimo();
        String status = epi.getQuantidadeEstoque() <= minimo ? "Baixo" : "Disponível";
        return new EstoqueItemResponse(
                epi.getId(),
                epi.getCodigo(),
                epi.getNome(),
                epi.getCategoria(),
                epi.getQuantidadeEstoque(),
                minimo,
                status,
                epi.getValidade(),
                epi.vencidoEm(LocalDate.now()));
    }
}
