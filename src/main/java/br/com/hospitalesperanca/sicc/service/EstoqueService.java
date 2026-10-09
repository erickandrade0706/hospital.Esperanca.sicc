package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.dto.EstoqueItemResponse;
import br.com.hospitalesperanca.sicc.dto.EstoqueMovimentacaoRequest;
import br.com.hospitalesperanca.sicc.dto.EstoqueMovimentacaoResponse;
import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Epi;
import br.com.hospitalesperanca.sicc.model.MovimentacaoEstoque;
import br.com.hospitalesperanca.sicc.model.Status;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEstoque;
import br.com.hospitalesperanca.sicc.repository.EpiRepository;
import br.com.hospitalesperanca.sicc.repository.MovimentacaoEstoqueRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Saldo de estoque dos EPIs e histórico de entradas/saídas (tela Estoque). */
@Service
public class EstoqueService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EpiRepository epiRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public EstoqueService(EpiRepository epiRepository,
                          MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.epiRepository = epiRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    @Transactional(readOnly = true)
    public List<EstoqueItemResponse> listar() {
        return epiRepository.findAll(Sort.by("nome")).stream().map(EstoqueItemResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<EstoqueMovimentacaoResponse> historico() {
        return movimentacaoEstoqueRepository.findAllByOrderByDataDescIdDesc().stream()
                .map(EstoqueMovimentacaoResponse::de)
                .toList();
    }

    /** Entrada ou saída manual, registrada pelo formulário da tela Estoque. */
    @Transactional
    public EstoqueMovimentacaoResponse registrar(EstoqueMovimentacaoRequest dados) {
        Epi epi = epiRepository.findById(dados.epiId())
                .orElseThrow(() -> new NaoEncontradoException("EPI não encontrado (id " + dados.epiId() + ")."));

        if (epi.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("O EPI \"" + epi.getNome() + "\" está inativo e não pode ser movimentado.");
        }
        // Regra: não entra no estoque um EPI que já está vencido
        if (dados.tipo() == TipoMovimentacaoEstoque.ENTRADA && epi.vencidoEm(LocalDate.now())) {
            throw new RegraNegocioException("Não é permitido dar entrada em EPI vencido. O EPI \"" + epi.getNome()
                    + "\" venceu em " + epi.getValidade().format(DATA_BR) + ".");
        }

        MovimentacaoEstoque movimentacao = movimentar(epi, dados.tipo(), dados.quantidade(),
                dados.responsavel().trim(), "Lançamento manual");
        return EstoqueMovimentacaoResponse.de(movimentacao);
    }

    /**
     * Altera o saldo do EPI e grava o histórico. Também é chamado pelo MovimentacaoEpiService
     * (entrega/troca = saída; devolução = entrada).
     */
    @Transactional
    public MovimentacaoEstoque movimentar(Epi epi, TipoMovimentacaoEstoque tipo, int quantidade,
                                          String responsavel, String observacao) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }

        if (tipo == TipoMovimentacaoEstoque.SAIDA) {
            // Regra: o estoque nunca fica negativo
            if (epi.getQuantidadeEstoque() < quantidade) {
                throw new RegraNegocioException("Estoque insuficiente de \"" + epi.getNome() + "\". Disponível: "
                        + epi.getQuantidadeEstoque() + ", solicitado: " + quantidade + ".");
            }
            epi.setQuantidadeEstoque(epi.getQuantidadeEstoque() - quantidade);
        } else {
            epi.setQuantidadeEstoque(epi.getQuantidadeEstoque() + quantidade);
        }
        epiRepository.save(epi);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setData(LocalDate.now());
        movimentacao.setTipo(tipo);
        movimentacao.setEpi(epi);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setResponsavel(responsavel);
        movimentacao.setObservacao(observacao);
        return movimentacaoEstoqueRepository.save(movimentacao);
    }
}
