package br.com.hospitalesperanca.sicc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Participante de um treinamento (fica gravado junto do treinamento). */
@Embeddable
public class Participante {

    @NotBlank(message = "O nome do participante é obrigatório.")
    @Size(max = 120, message = "O nome do participante deve ter no máximo 120 caracteres.")
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 80, message = "O setor deve ter no máximo 80 caracteres.")
    @Column(length = 80)
    private String setor;

    @Column(nullable = false)
    private boolean presenca;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public boolean isPresenca() {
        return presenca;
    }

    public void setPresenca(boolean presenca) {
        this.presenca = presenca;
    }
}
