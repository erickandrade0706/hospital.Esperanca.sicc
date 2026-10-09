package br.com.hospitalesperanca.sicc.repository;

import br.com.hospitalesperanca.sicc.model.Perfil;
import br.com.hospitalesperanca.sicc.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByMatricula(String matricula);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    long countByPerfilAndAtivoTrue(Perfil perfil);
}
