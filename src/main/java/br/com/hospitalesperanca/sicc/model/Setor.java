package br.com.hospitalesperanca.sicc.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "setores")
public class Setor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "O nome do setor é obrigatório.")
  @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
  @Column(nullable = false, unique = true, length = 80)
  private String nome;

  public Long getId() { return id; }
  public String getNome() { return nome; }
  public void setNome(String nome) { this.nome = nome; }
}
