package br.com.hospitalesperanca.sicc.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Transforma as exceções em respostas JSON padronizadas (ErroResponse). */
@RestControllerAdvice
public class TratadorDeErros {

    /** Regras de negócio, duplicidade, não encontrado, login inválido... */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> tratarApi(ApiException ex) {
        return resposta(ex.getStatus(), ex.getMessage());
    }

    /** Campos obrigatórios não preenchidos ou inválidos (@NotBlank, @NotNull, @Pattern...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ErroResponse corpo = ErroResponse.de(HttpStatus.BAD_REQUEST,
                "Existem campos obrigatórios não preenchidos ou com valor inválido.", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    /** JSON mal formado, data em formato errado ou valor fora da lista (ex.: status "Xyz"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensagem = "Dados enviados em formato inválido.";
        if (causa instanceof IllegalArgumentException) {
            mensagem = causa.getMessage();
        } else if (causa instanceof DateTimeParseException) {
            mensagem = "Data inválida. Use o formato AAAA-MM-DD.";
        }
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro \"" + ex.getName() + "\".");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Rede de segurança: o banco recusou a gravação (campo único repetido ou registro em uso). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT,
                "Operação não permitida: registro duplicado ou em uso por outro cadastro.");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(status, mensagem));
    }
}
