package br.com.hospitalesperanca.sicc.repository;

import br.com.hospitalesperanca.sicc.model.Treinamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreinamentoRepository extends JpaRepository<Treinamento, Long> {
}
