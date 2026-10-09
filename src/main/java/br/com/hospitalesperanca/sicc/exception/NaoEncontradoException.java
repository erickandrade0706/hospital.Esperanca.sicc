package br.com.hospitalesperanca.sicc.exception;

import org.springframework.http.HttpStatus;

/** Registro não existe. HTTP 404. */
public class NaoEncontradoException extends ApiException {

    public NaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}
