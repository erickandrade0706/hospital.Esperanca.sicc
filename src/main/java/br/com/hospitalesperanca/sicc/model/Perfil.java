package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Perfis de acesso - os mesmos três do front end (auth.ts). */
public enum Perfil implements Rotulado {
    ADMINISTRADOR("Administrador"),
    RH("RH"),
    TECNICO_SEGURANCA("Técnico de Segurança");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    /** Nome usado pelo Spring Security (ex.: ROLE_ADMINISTRADOR). */
    public String getRole() {
        return "ROLE_" + name();
    }

    @JsonCreator
    public static Perfil deTexto(String texto) {
        return Rotulos.buscar(Perfil.class, texto);
    }
}
