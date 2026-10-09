package br.com.hospitalesperanca.sicc.dto;

/** Resposta do login: o token deve ser enviado nas próximas requisições (Authorization: Bearer token). */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEmMinutos,
        UsuarioResponse usuario) {
}
