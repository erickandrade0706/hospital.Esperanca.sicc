package br.com.hospitalesperanca.sicc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Equipamento de Proteção Individual (Cadastro > EPIs). Mesmos campos do epi.model.ts. */
@Entity
@Table(name = "epis")
public class Epi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O código é obrigatório.")
    @Size(max = 30, message = "O código deve ter no máximo 30 caracteres.")
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotBlank(message = "O nome do EPI é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    @Column(length = 500)
    private String descricao;

    @NotBlank(message = "O CA (Certificado de Aprovação) é obrigatório.")
    @Size(max = 20, message = "O CA deve ter no máximo 20 caracteres.")
    @Column(nullable = false, length = 20)
    private String ca;

    @NotNull(message = "A validade é obrigatória.")
    @Column(nullable = false)
    private LocalDate validade;

    @NotBlank(message = "O fabricante é obrigatório.")
    @Size(max = 80, message = "O fabricante deve ter no máximo 80 caracteres.")
    @Column(nullable = false, length = 80)
    private String fabricante;

    @NotBlank(message = "A categoria é obrigatória.")
    @Size(max = 80, message = "A categoria deve ter no máximo 80 caracteres.")
    @Column(nullable = false, length = 80)
    private String categoria;

    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ATIVO;

    /** Saldo atual. Só muda pelas movimentações de estoque - nunca pelo formulário de cadastro. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidadeEstoque = 0;

    /** Regra de negócio: EPI com validade anterior à data informada está vencido. */
    public boolean vencidoEm(LocalDate data) {
        return validade != null && validade.isBefore(data);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
}
