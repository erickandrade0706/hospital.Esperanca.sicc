package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status implements Rotulado {
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String rotulo;

    Status(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static Status deTexto(String texto) {
        return Rotulos.buscar(Status.class, texto);
    }
}
