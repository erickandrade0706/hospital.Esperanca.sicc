package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.dto.UsuarioRequest;
import br.com.hospitalesperanca.sicc.dto.UsuarioResponse;
import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegistroDuplicadoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Perfil;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de usuários do sistema (Configurações > Usuários) - somente Administrador. */
@Service
public class UsuarioService {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("nome")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.de(obter(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest dados) {
        String email = dados.email().trim().toLowerCase();
        String matricula = dados.matricula().trim();

        // Regra: não pode existir outro usuário com o mesmo e-mail ou a mesma matrícula
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegistroDuplicadoException("Já existe um usuário cadastrado com o e-mail " + email + ".");
        }
        if (usuarioRepository.existsByMatricula(matricula)) {
            throw new RegistroDuplicadoException("Já existe um usuário cadastrado com a matrícula " + matricula + ".");
        }
        if (dados.senha() == null || dados.senha().isBlank()) {
            throw new RegraNegocioException("A senha é obrigatória para cadastrar um usuário.");
        }
        validarSenha(dados.senha());

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
        usuario.setPerfil(dados.perfil());
        usuario.setAtivo(dados.ativo() == null || dados.ativo());
        usuario.setSenhaHash(passwordEncoder.encode(dados.senha()));

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest dados, Usuario logado) {
        Usuario usuario = obter(id);
        String email = dados.email().trim().toLowerCase();
        String matricula = dados.matricula().trim();
        boolean ativo = dados.ativo() == null ? usuario.isAtivo() : dados.ativo();

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new RegistroDuplicadoException("Já existe outro usuário cadastrado com o e-mail " + email + ".");
        }
        if (usuarioRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new RegistroDuplicadoException("Já existe outro usuário cadastrado com a matrícula " + matricula + ".");
        }
        if (id.equals(logado.getId()) && !ativo) {
            throw new RegraNegocioException("Você não pode inativar o seu próprio usuário.");
        }
        garantirAdministradorAtivo(usuario, dados.perfil(), ativo);

        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
        usuario.setPerfil(dados.perfil());
        definirAtivo(usuario, ativo);

        // Senha só é trocada se uma nova for informada
        if (dados.senha() != null && !dados.senha().isBlank()) {
            validarSenha(dados.senha());
            usuario.setSenhaHash(passwordEncoder.encode(dados.senha()));
        }

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse alterarStatus(Long id, boolean ativo, Usuario logado) {
        Usuario usuario = obter(id);

        if (id.equals(logado.getId()) && !ativo) {
            throw new RegraNegocioException("Você não pode inativar o seu próprio usuário.");
        }
        garantirAdministradorAtivo(usuario, usuario.getPerfil(), ativo);
        definirAtivo(usuario, ativo);

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public void excluir(Long id, Usuario logado) {
        Usuario usuario = obter(id);

        if (id.equals(logado.getId())) {
            throw new RegraNegocioException("Você não pode excluir o seu próprio usuário.");
        }
        garantirAdministradorAtivo(usuario, usuario.getPerfil(), false);

        usuarioRepository.delete(usuario);
    }

    private Usuario obter(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado (id " + id + ")."));
    }

    private void validarSenha(String senha) {
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException("A senha deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    /** Ao reativar um usuário, também libera um eventual bloqueio por tentativas de login. */
    private void definirAtivo(Usuario usuario, boolean ativo) {
        if (ativo && !usuario.isAtivo()) {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
        }
        usuario.setAtivo(ativo);
    }

    /** Regra: o sistema não pode ficar sem nenhum administrador ativo. */
    private void garantirAdministradorAtivo(Usuario usuario, Perfil novoPerfil, boolean novoAtivo) {
        boolean eraAdminAtivo = usuario.getPerfil() == Perfil.ADMINISTRADOR && usuario.isAtivo();
        boolean continuaAdminAtivo = novoPerfil == Perfil.ADMINISTRADOR && novoAtivo;

        if (eraAdminAtivo && !continuaAdminAtivo
                && usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMINISTRADOR) <= 1) {
            throw new RegraNegocioException("O sistema precisa ter pelo menos um administrador ativo.");
        }
    }
}
