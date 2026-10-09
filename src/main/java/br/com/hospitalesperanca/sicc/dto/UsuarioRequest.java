package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados para criar/alterar um usuário (Configurações > Usuários).
 * As regras de e-mail e matrícula são as mesmas expressões regulares do login.ts.
 * A senha é obrigatória ao criar e opcional ao alterar (verificado no UsuarioService).
 */
public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Digite um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "A matrícula é obrigatória.")
        @Pattern(regexp = "^\\d{4,10}$", message = "A matrícula deve ter de 4 a 10 dígitos numéricos.")
        String matricula,

        @NotNull(message = "O perfil é obrigatório.")
        Perfil perfil,

        String senha,

        Boolean ativo) {
}
