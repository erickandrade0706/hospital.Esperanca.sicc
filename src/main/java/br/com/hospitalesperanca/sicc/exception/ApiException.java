package br.com.hospitalesperanca.sicc.exception;

import org.springframework.http.HttpStatus;

/** Erro "esperado" da API: carrega o status HTTP e a mensagem que o front end vai exibir. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
