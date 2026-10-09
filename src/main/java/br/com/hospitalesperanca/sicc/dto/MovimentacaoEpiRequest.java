package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.StatusMovimentacao;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEpi;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Formulário "Registrar Movimentação de EPI" (tela Gestão de EPIs). */
public record MovimentacaoEpiRequest(
        @NotNull(message = "O tipo da movimentação é obrigatório (Entrega, Devolução ou Troca).")
        TipoMovimentacaoEpi tipo,

        @NotBlank(message = "A matrícula do funcionário é obrigatória.")
        String matricula,

        @NotNull(message = "Selecione um EPI.")
        Long epiId,

        @NotNull(message = "A quantidade é obrigatória.")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
        Integer quantidade,

        @NotNull(message = "A data é obrigatória.")
        @PastOrPresent(message = "A data da movimentação não pode ser futura.")
        LocalDate data,

        @NotNull(message = "O status é obrigatório (Concluído ou Pendente).")
        StatusMovimentacao status,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
        String observacoes) {
}
