package br.com.hospitalesperanca.sicc.repository;

import br.com.hospitalesperanca.sicc.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findAllByOrderByDataDescIdDesc();

    boolean existsByEpiId(Long epiId);
}
