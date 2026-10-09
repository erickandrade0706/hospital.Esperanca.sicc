package br.com.hospitalesperanca.sicc.exception;

import org.springframework.http.HttpStatus;

/** Já existe um registro com o mesmo identificador (matrícula, CPF, código, e-mail...). HTTP 409. */
public class RegistroDuplicadoException extends ApiException {

    public RegistroDuplicadoException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
