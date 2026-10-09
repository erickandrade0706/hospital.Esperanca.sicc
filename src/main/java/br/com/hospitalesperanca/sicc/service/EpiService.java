package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegistroDuplicadoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Epi;
import br.com.hospitalesperanca.sicc.model.Status;
import br.com.hospitalesperanca.sicc.repository.EpiRepository;
import br.com.hospitalesperanca.sicc.repository.MovimentacaoEpiRepository;
import br.com.hospitalesperanca.sicc.repository.MovimentacaoEstoqueRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD e regras de negócio do cadastro de EPIs. */
@Service
public class EpiService {

    private final EpiRepository epiRepository;
    private final MovimentacaoEpiRepository movimentacaoEpiRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public EpiService(EpiRepository epiRepository,
                      MovimentacaoEpiRepository movimentacaoEpiRepository,
                      MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.epiRepository = epiRepository;
        this.movimentacaoEpiRepository = movimentacaoEpiRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    @Transactional(readOnly = true)
    public List<Epi> listar() {
        return epiRepository.findAll(Sort.by("nome"));
    }

    @Transactional(readOnly = true)
    public Epi buscar(Long id) {
        return epiRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("EPI não encontrado (id " + id + ")."));
    }

    @Transactional
    public Epi criar(Epi dados) {
        String codigo = dados.getCodigo().trim();

        // Regra: o código identifica o EPI e não pode se repetir
        if (epiRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RegistroDuplicadoException("Já existe um EPI cadastrado com o código " + codigo + ".");
        }

        Epi novo = new Epi(); // sempre um registro novo: id gerado pelo banco e estoque começando em zero
        copiar(dados, novo, codigo);
        return epiRepository.save(novo);
    }

    @Transactional
    public Epi atualizar(Long id, Epi dados) {
        Epi existente = buscar(id);
        String codigo = dados.getCodigo().trim();

        if (epiRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new RegistroDuplicadoException("Já existe outro EPI cadastrado com o código " + codigo + ".");
        }

        copiar(dados, existente, codigo); // a quantidade em estoque não é alterada aqui
        return epiRepository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Epi existente = buscar(id);

        // Regra: não apagar EPI que já tem histórico de estoque ou de entregas
        if (movimentacaoEpiRepository.existsByEpiId(id) || movimentacaoEstoqueRepository.existsByEpiId(id)) {
            throw new RegraNegocioException("Este EPI possui movimentações registradas e não pode ser excluído. "
                    + "Altere o status para Inativo.");
        }
        epiRepository.delete(existente);
    }

    private void copiar(Epi origem, Epi destino, String codigo) {
        destino.setCodigo(codigo);
        destino.setNome(origem.getNome().trim());
        destino.setDescricao(Textos.limpar(origem.getDescricao()));
        destino.setCa(origem.getCa().trim());
        destino.setValidade(origem.getValidade());
        destino.setFabricante(origem.getFabricante().trim());
        destino.setCategoria(origem.getCategoria().trim());
        destino.setEstoqueMinimo(origem.getEstoqueMinimo() == null ? 0 : origem.getEstoqueMinimo());
        destino.setStatus(origem.getStatus() == null ? Status.ATIVO : origem.getStatus());
    }
}
