package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.MovimentacaoEstoque;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEstoque;

import java.time.LocalDate;

/** Linha do histórico de estoque. Como no estoque.ts, a saída vem com quantidade negativa. */
public record EstoqueMovimentacaoResponse(
        Long id,
        LocalDate data,
        TipoMovimentacaoEstoque tipo,
        Long epiId,
        String epi,
        int quantidade,
        String responsavel,
        String observacao) {

    public static EstoqueMovimentacaoResponse de(MovimentacaoEstoque mov) {
        int quantidade = mov.getTipo() == TipoMovimentacaoEstoque.SAIDA ? -mov.getQuantidade() : mov.getQuantidade();
        return new EstoqueMovimentacaoResponse(
                mov.getId(),
                mov.getData(),
                mov.getTipo(),
                mov.getEpi().getId(),
                mov.getEpi().getNome(),
                quantidade,
                mov.getResponsavel(),
                mov.getObservacao());
    }
}
