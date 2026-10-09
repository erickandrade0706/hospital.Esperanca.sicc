package br.com.hospitalesperanca.sicc.model;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Converte o texto recebido do front end (ex.: "Técnico de Segurança") no enum correspondente. */
public final class Rotulos {

    private Rotulos() {
    }

    public static <E extends Enum<E> & Rotulado> E buscar(Class<E> tipo, String texto) {
        if (texto == null || texto.isBlank()) {
            return null; // campo vazio: a validação de obrigatório (@NotNull) cuida da mensagem
        }
        String valor = texto.trim();
        for (E item : tipo.getEnumConstants()) {
            if (item.getRotulo().equalsIgnoreCase(valor) || item.name().equalsIgnoreCase(valor)) {
                return item;
            }
        }
        String aceitos = Arrays.stream(tipo.getEnumConstants())
                .map(Rotulado::getRotulo)
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Valor inválido: \"" + valor + "\". Valores aceitos: " + aceitos + ".");
    }
}
