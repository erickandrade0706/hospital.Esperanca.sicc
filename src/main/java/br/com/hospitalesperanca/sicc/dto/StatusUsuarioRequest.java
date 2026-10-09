package br.com.hospitalesperanca.sicc.dto;

import jakarta.validation.constraints.NotNull;

/** Ativar/inativar usuário (botão da tela de Configurações). */
public record StatusUsuarioRequest(
        @NotNull(message = "Informe se o usuário deve ficar ativo (true) ou inativo (false).")
        Boolean ativo) {
}
