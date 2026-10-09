package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.MovimentacaoEpi;
import br.com.hospitalesperanca.sicc.model.StatusMovimentacao;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEpi;

import java.time.LocalDate;

/** Linha da tabela "Últimas Movimentações" e do relatório de entregas. */
public record MovimentacaoEpiResponse(
        Long id,
        TipoMovimentacaoEpi tipo,
        String funcionario,
        String matricula,
        String setor,
        Long epiId,
        String epi,
        int quantidade,
        LocalDate data,
        StatusMovimentacao status,
        String observacoes,
        String registradoPor) {

    public static MovimentacaoEpiResponse de(MovimentacaoEpi mov) {
        return new MovimentacaoEpiResponse(
                mov.getId(),
                mov.getTipo(),
                mov.getColaborador().getNome(),
                mov.getColaborador().getMatricula(),
                mov.getColaborador().getSetor(),
                mov.getEpi().getId(),
                mov.getEpi().getNome(),
                mov.getQuantidade(),
                mov.getData(),
                mov.getStatus(),
                mov.getObservacoes(),
                mov.getRegistradoPor());
    }
}
