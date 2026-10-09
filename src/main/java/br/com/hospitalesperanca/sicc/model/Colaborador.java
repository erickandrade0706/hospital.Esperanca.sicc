package br.com.hospitalesperanca.sicc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Colaborador do hospital (Cadastro > Colaboradores).
 * A função é escolhida pelo id (chave estrangeira funcao_id); o nome da função e o setor
 * são copiados da função escolhida. A validação dos campos fica no ColaboradorRequest.
 */
@Entity
@Table(name = "colaboradores")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    /** Função escolhida no select (coluna funcao_id, ligada à tabela funcoes). */
    @ManyToOne
    @JoinColumn(name = "funcao_id")
    private Funcao funcao;

    /** Nome da função escolhida (cópia, usada nas listas e relatórios). */
    @Column(name = "funcao", nullable = false, length = 80)
    private String nomeFuncao;

    /** Setor puxado da função escolhida. */
    @Column(nullable = false, length = 80)
    private String setor;

    @Column(nullable = false)
    private LocalDate admissao;

    @Column(length = 60)
    private String contato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ATIVO;

    /** Liga o colaborador à função e puxa o nome e o setor dela. */
    public void definirFuncao(Funcao funcao) {
        this.funcao = funcao;
        this.nomeFuncao = funcao.getNome();
        this.setor = funcao.getSetor();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Funcao getFuncao() {
        return funcao;
    }

    public String getNomeFuncao() {
        return nomeFuncao;
    }

    public String getSetor() {
        return setor;
    }

    public LocalDate getAdmissao() {
        return admissao;
    }

    public void setAdmissao(LocalDate admissao) {
        this.admissao = admissao;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
