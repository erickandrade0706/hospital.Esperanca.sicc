package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegistroDuplicadoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Colaborador;
import br.com.hospitalesperanca.sicc.model.Funcao;
import br.com.hospitalesperanca.sicc.model.Status;
import br.com.hospitalesperanca.sicc.repository.ColaboradorRepository;
import br.com.hospitalesperanca.sicc.repository.FuncaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** CRUD e regras de negócio do cadastro de funções. */
@Service
public class FuncaoService {

    private final FuncaoRepository funcaoRepository;
    private final ColaboradorRepository colaboradorRepository;

    public FuncaoService(FuncaoRepository funcaoRepository, ColaboradorRepository colaboradorRepository) {
        this.funcaoRepository = funcaoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional(readOnly = true)
    public List<Funcao> listar() {
        return funcaoRepository.findAll(Sort.by("nome"));
    }

    @Transactional(readOnly = true)
    public Funcao buscar(Long id) {
        return funcaoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Função não encontrada (id " + id + ")."));
    }

    @Transactional
    public Funcao criar(Funcao dados) {
        String nome = dados.getNome().trim();

        // Regra: não pode existir duas funções com o mesmo nome
        if (funcaoRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegistroDuplicadoException("Já existe uma função cadastrada com o nome " + nome + ".");
        }

        Funcao nova = new Funcao();
        copiar(dados, nova, nome);
        return funcaoRepository.save(nova);
    }

    @Transactional
    public Funcao atualizar(Long id, Funcao dados) {
        Funcao existente = buscar(id);
        String nome = dados.getNome().trim();

        if (funcaoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new RegistroDuplicadoException("Já existe outra função cadastrada com o nome " + nome + ".");
        }

        copiar(dados, existente, nome);
        Funcao salva = funcaoRepository.save(existente);

        // Os colaboradores desta função passam a mostrar o novo nome e o novo setor
        List<Colaborador> colaboradores = colaboradorRepository.findByFuncaoId(id);
        for (Colaborador colaborador : colaboradores) {
            colaborador.definirFuncao(salva);
        }
        colaboradorRepository.saveAll(colaboradores);

        return salva;
    }

    @Transactional
    public void excluir(Long id) {
        Funcao existente = buscar(id);

        // Regra: função escolhida por algum colaborador não pode ser apagada
        if (colaboradorRepository.existsByFuncaoId(id)) {
            throw new RegraNegocioException("Esta função está vinculada a colaboradores e não pode ser excluída. "
                    + "Altere o status para Inativo.");
        }
        funcaoRepository.delete(existente);
    }

    private void copiar(Funcao origem, Funcao destino, String nome) {
        destino.setNome(nome);
        destino.setDescricao(Textos.limpar(origem.getDescricao()));
        destino.setSetor(origem.getSetor().trim());
        destino.setStatus(origem.getStatus() == null ? Status.ATIVO : origem.getStatus());

        // Lista de EPIs: tira espaços, itens vazios e repetidos (como o funcoes.ts faz)
        List<String> epis = new ArrayList<>();
        if (origem.getEpisObrigatorios() != null) {
            for (String epi : origem.getEpisObrigatorios()) {
                String limpo = Textos.limpar(epi);
                if (limpo != null && !epis.contains(limpo)) {
                    epis.add(limpo);
                }
            }
        }
        destino.setEpisObrigatorios(epis);
    }
}
