package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/** Função/cargo (Cadastro > Funções). Mesmos campos do funcao.model.ts. */
@Entity
@Table(name = "funcoes")
public class Funcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome da função é obrigatório.")
    @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    @Column(length = 500)
    private String descricao;

    @NotBlank(message = "O setor relacionado é obrigatório.")
    @Size(max = 80, message = "O setor deve ter no máximo 80 caracteres.")
    @Column(nullable = false, length = 80)
    private String setor;

    /** Nomes dos EPIs obrigatórios/recomendados para a função. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "funcao_epis", joinColumns = @JoinColumn(name = "funcao_id"))
    @Column(name = "epi", length = 120)
    private List<String> episObrigatorios = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ATIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public List<String> getEpisObrigatorios() {
        return episObrigatorios;
    }

    public void setEpisObrigatorios(List<String> episObrigatorios) {
        this.episObrigatorios = episObrigatorios;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
