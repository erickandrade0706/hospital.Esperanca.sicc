package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoMovimentacaoEpi implements Rotulado {
    ENTREGA("Entrega"),
    DEVOLUCAO("Devolução"),
    TROCA("Troca");

    private final String rotulo;

    TipoMovimentacaoEpi(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static TipoMovimentacaoEpi deTexto(String texto) {
        return Rotulos.buscar(TipoMovimentacaoEpi.class, texto);
    }
}
