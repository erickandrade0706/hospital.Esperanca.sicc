package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Formulário de colaborador enviado pelo front.
 * A função vem como id (valor do select); o setor não é enviado, é puxado da função.
 */
public record ColaboradorRequest(
        @NotBlank(message = "A matrícula é obrigatória.")
        @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres.")
        String matricula,

        @NotBlank(message = "O nome completo é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF inválido. Use 000.000.000-00 ou 11 dígitos.")
        String cpf,

        @NotNull(message = "Selecione a função.")
        Long funcaoId,

        @NotNull(message = "A data de admissão é obrigatória.")
        @PastOrPresent(message = "A data de admissão não pode ser uma data futura.")
        LocalDate admissao,

        @Size(max = 60, message = "O contato deve ter no máximo 60 caracteres.")
        String contato,

        Status status) {
}
