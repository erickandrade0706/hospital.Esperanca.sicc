package br.com.hospitalesperanca.sicc.repository;

import br.com.hospitalesperanca.sicc.model.MovimentacaoEstoque;
import br.com.hospitalesperanca.sicc.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetorRepository extends JpaRepository<Setor, Long>{

  List<Setor> findAllByOrderByNomeAsc();

  boolean existsByNome(String nome);
}




//public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
//
//  List<MovimentacaoEstoque> findAllByOrderByDataDescIdDesc();
//
//  boolean existsByEpiId(Long epiId);
//}
