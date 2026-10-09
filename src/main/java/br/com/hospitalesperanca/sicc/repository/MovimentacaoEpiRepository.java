package br.com.hospitalesperanca.sicc.repository;

import br.com.hospitalesperanca.sicc.model.MovimentacaoEpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEpiRepository extends JpaRepository<MovimentacaoEpi, Long> {

    List<MovimentacaoEpi> findAllByOrderByDataDescIdDesc();

    boolean existsByColaboradorId(Long colaboradorId);

    boolean existsByEpiId(Long epiId);
}
