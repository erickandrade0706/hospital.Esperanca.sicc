package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.dto.ColaboradorRequest;
import br.com.hospitalesperanca.sicc.dto.ColaboradorResponse;
import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegistroDuplicadoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Colaborador;
import br.com.hospitalesperanca.sicc.model.Funcao;
import br.com.hospitalesperanca.sicc.model.Status;
import br.com.hospitalesperanca.sicc.repository.ColaboradorRepository;
import br.com.hospitalesperanca.sicc.repository.FuncaoRepository;
import br.com.hospitalesperanca.sicc.repository.MovimentacaoEpiRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD e regras de negócio de colaboradores. */
@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;
    private final MovimentacaoEpiRepository movimentacaoEpiRepository;
    private final FuncaoRepository funcaoRepository;

    public ColaboradorService(ColaboradorRepository colaboradorRepository,
                              MovimentacaoEpiRepository movimentacaoEpiRepository,
                              FuncaoRepository funcaoRepository) {
        this.colaboradorRepository = colaboradorRepository;
        this.movimentacaoEpiRepository = movimentacaoEpiRepository;
        this.funcaoRepository = funcaoRepository;
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return colaboradorRepository.findAll(Sort.by("nome")).stream().map(ColaboradorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse buscar(Long id) {
        return ColaboradorResponse.de(obter(id));
    }

    @Transactional
    public ColaboradorResponse criar(ColaboradorRequest dados) {
        String matricula = dados.matricula().trim();
        String cpf = formatarCpf(dados.cpf());

        // Regra: matrícula e CPF identificam o colaborador e não podem se repetir
        if (colaboradorRepository.existsByMatricula(matricula)) {
            throw new RegistroDuplicadoException("Já existe um colaborador cadastrado com a matrícula " + matricula + ".");
        }
        if (colaboradorRepository.existsByCpf(cpf)) {
            throw new RegistroDuplicadoException("Já existe um colaborador cadastrado com o CPF " + cpf + ".");
        }

        Funcao funcao = obterFuncao(dados.funcaoId(), null);

        Colaborador novo = new Colaborador(); // sempre um registro novo: o id é gerado pelo banco
        copiar(dados, novo, matricula, cpf, funcao);
        return ColaboradorResponse.de(colaboradorRepository.save(novo));
    }

    @Transactional
    public ColaboradorResponse atualizar(Long id, ColaboradorRequest dados) {
        Colaborador existente = obter(id);
        String matricula = dados.matricula().trim();
        String cpf = formatarCpf(dados.cpf());

        if (colaboradorRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new RegistroDuplicadoException("Já existe outro colaborador cadastrado com a matrícula " + matricula + ".");
        }
        if (colaboradorRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new RegistroDuplicadoException("Já existe outro colaborador cadastrado com o CPF " + cpf + ".");
        }

        Funcao funcao = obterFuncao(dados.funcaoId(), existente.getFuncao());

        copiar(dados, existente, matricula, cpf, funcao);
        return ColaboradorResponse.de(colaboradorRepository.save(existente));
    }

    @Transactional
    public void excluir(Long id) {
        Colaborador existente = obter(id);

        // Regra: não apagar o histórico de entregas de EPI
        if (movimentacaoEpiRepository.existsByColaboradorId(id)) {
            throw new RegraNegocioException("Este colaborador possui movimentações de EPI registradas e não pode ser "
                    + "excluído. Altere o status para Inativo.");
        }
        colaboradorRepository.delete(existente);
    }

    private Colaborador obter(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Colaborador não encontrado (id " + id + ")."));
    }

    /**
     * Regra: a função precisa existir na tabela de funções e estar ativa.
     * (Uma função que ficou inativa pode continuar no colaborador que já a tinha.)
     */
    private Funcao obterFuncao(Long funcaoId, Funcao funcaoAtual) {
        Funcao funcao = funcaoRepository.findById(funcaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Função não encontrada (id " + funcaoId + "). Selecione uma função cadastrada."));

        boolean mesmaFuncao = funcaoAtual != null && funcaoAtual.getId().equals(funcao.getId());
        if (funcao.getStatus() != Status.ATIVO && !mesmaFuncao) {
            throw new RegraNegocioException("A função \"" + funcao.getNome() + "\" está inativa e não pode ser escolhida.");
        }
        return funcao;
    }

    private void copiar(ColaboradorRequest origem, Colaborador destino, String matricula, String cpf, Funcao funcao) {
        destino.setMatricula(matricula);
        destino.setCpf(cpf);
        destino.setNome(origem.nome().trim());
        destino.definirFuncao(funcao); // função pelo id + setor puxado da função
        destino.setAdmissao(origem.admissao());
        destino.setContato(Textos.limpar(origem.contato()));
        destino.setStatus(origem.status() == null ? Status.ATIVO : origem.status());
    }

    /** Grava o CPF sempre no formato 000.000.000-00, para a verificação de duplicidade funcionar. */
    private String formatarCpf(String cpf) {
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new RegraNegocioException("CPF inválido: informe os 11 dígitos.");
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }
}
