package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEstoque;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Formulário "Registrar movimentação" da tela Estoque. */
public record EstoqueMovimentacaoRequest(
        @NotNull(message = "Selecione um EPI.")
        Long epiId,

        @NotNull(message = "O tipo de movimentação é obrigatório (Entrada ou Saída).")
        TipoMovimentacaoEstoque tipo,

        @NotNull(message = "A quantidade é obrigatória.")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
        Integer quantidade,

        @NotBlank(message = "O responsável é obrigatório.")
        @Size(max = 120, message = "O responsável deve ter no máximo 120 caracteres.")
        String responsavel) {
}
