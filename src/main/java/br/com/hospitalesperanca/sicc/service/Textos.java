package br.com.hospitalesperanca.sicc.service;

/** Pequenos utilitários de texto usados pelos serviços. */
final class Textos {

    private Textos() {
    }

    /** Remove espaços das pontas; texto vazio vira null (para campos opcionais). */
    static String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
