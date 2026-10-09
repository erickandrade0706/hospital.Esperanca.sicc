package br.com.hospitalesperanca.sicc.dto;

import jakarta.validation.constraints.NotBlank;

/** Dados enviados pela tela de login. Mesmas mensagens de obrigatório do login.html. */
public record LoginRequest(
        @NotBlank(message = "O campo Matrícula ou e-mail é obrigatório.")
        String identificacao,

        @NotBlank(message = "O campo Senha é obrigatório.")
        String senha) {
}
