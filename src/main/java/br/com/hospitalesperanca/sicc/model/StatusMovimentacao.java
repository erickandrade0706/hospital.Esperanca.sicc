package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusMovimentacao implements Rotulado {
    CONCLUIDO("Concluído"),
    PENDENTE("Pendente");

    private final String rotulo;

    StatusMovimentacao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static StatusMovimentacao deTexto(String texto) {
        return Rotulos.buscar(StatusMovimentacao.class, texto);
    }
}
