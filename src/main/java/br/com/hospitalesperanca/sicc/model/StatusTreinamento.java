package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusTreinamento implements Rotulado {
    AGENDADO("Agendado"),
    CONCLUIDO("Concluído"),
    VENCIDO("Vencido");

    private final String rotulo;

    StatusTreinamento(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static StatusTreinamento deTexto(String texto) {
        return Rotulos.buscar(StatusTreinamento.class, texto);
    }
}
