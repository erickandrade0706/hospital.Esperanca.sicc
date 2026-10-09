package br.com.hospitalesperanca.sicc.repository;

import br.com.hospitalesperanca.sicc.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    Optional<Colaborador> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    /** Colaboradores ligados a uma função (pelo funcao_id). */
    List<Colaborador> findByFuncaoId(Long funcaoId);

    boolean existsByFuncaoId(Long funcaoId);
}
