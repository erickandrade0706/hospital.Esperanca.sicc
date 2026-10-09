package br.com.hospitalesperanca.sicc.exception;

import org.springframework.http.HttpStatus;

/** Uma regra de negócio foi violada (ex.: entregar EPI vencido). HTTP 422. */
public class RegraNegocioException extends ApiException {

    public RegraNegocioException(String mensagem) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
