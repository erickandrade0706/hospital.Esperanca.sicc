package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoMovimentacaoEstoque implements Rotulado {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String rotulo;

    TipoMovimentacaoEstoque(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static TipoMovimentacaoEstoque deTexto(String texto) {
        return Rotulos.buscar(TipoMovimentacaoEstoque.class, texto);
    }
}
