package br.com.hospitalesperanca.sicc.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padrão de erro devolvido pela API.
 * "campos" traz a mensagem de cada campo inválido (nomeDoCampo -> mensagem).
 */
public record ErroResponse(
        LocalDateTime dataHora,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos) {

    public static ErroResponse de(HttpStatus status, String mensagem) {
        return de(status, mensagem, Map.of());
    }

    public static ErroResponse de(HttpStatus status, String mensagem, Map<String, String> campos) {
        return new ErroResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, campos);
    }
}
