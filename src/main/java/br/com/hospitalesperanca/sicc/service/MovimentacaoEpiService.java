package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.dto.MovimentacaoEpiRequest;
import br.com.hospitalesperanca.sicc.dto.MovimentacaoEpiResponse;
import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Colaborador;
import br.com.hospitalesperanca.sicc.model.Epi;
import br.com.hospitalesperanca.sicc.model.MovimentacaoEpi;
import br.com.hospitalesperanca.sicc.model.Status;
import br.com.hospitalesperanca.sicc.model.StatusMovimentacao;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEpi;
import br.com.hospitalesperanca.sicc.model.TipoMovimentacaoEstoque;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.repository.ColaboradorRepository;
import br.com.hospitalesperanca.sicc.repository.EpiRepository;
import br.com.hospitalesperanca.sicc.repository.MovimentacaoEpiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Entrega, devolução e troca de EPIs (tela Gestão de EPIs).
 * Aqui ficam as principais regras de negócio do sistema.
 */
@Service
public class MovimentacaoEpiService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovimentacaoEpiRepository movimentacaoEpiRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final EpiRepository epiRepository;
    private final EstoqueService estoqueService;

    public MovimentacaoEpiService(MovimentacaoEpiRepository movimentacaoEpiRepository,
                                  ColaboradorRepository colaboradorRepository,
                                  EpiRepository epiRepository,
                                  EstoqueService estoqueService) {
        this.movimentacaoEpiRepository = movimentacaoEpiRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.epiRepository = epiRepository;
        this.estoqueService = estoqueService;
    }

    /** Lista com filtros opcionais (também serve para o relatório de entregas). */
    @Transactional(readOnly = true)
    public List<MovimentacaoEpiResponse> listar(TipoMovimentacaoEpi tipo, LocalDate dataInicio, LocalDate dataFim,
                                                String setor, String colaborador) {
        String setorBusca = Textos.limpar(setor);
        String nomeBusca = Textos.limpar(colaborador);

        return movimentacaoEpiRepository.findAllByOrderByDataDescIdDesc().stream()
                .filter(mov -> tipo == null || mov.getTipo() == tipo)
                .filter(mov -> dataInicio == null || !mov.getData().isBefore(dataInicio))
                .filter(mov -> dataFim == null || !mov.getData().isAfter(dataFim))
                .filter(mov -> setorBusca == null || mov.getColaborador().getSetor().equalsIgnoreCase(setorBusca))
                .filter(mov -> nomeBusca == null
                        || mov.getColaborador().getNome().toLowerCase().contains(nomeBusca.toLowerCase()))
                .map(MovimentacaoEpiResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimentacaoEpiResponse buscar(Long id) {
        return MovimentacaoEpiResponse.de(obter(id));
    }

    @Transactional
    public MovimentacaoEpiResponse registrar(MovimentacaoEpiRequest dados, Usuario logado) {
        MovimentacaoEpi movimentacao = new MovimentacaoEpi();
        preencher(movimentacao, dados, logado);

        if (movimentacao.getStatus() == StatusMovimentacao.CONCLUIDO) {
            aplicarNoEstoque(movimentacao, logado);
        }
        return MovimentacaoEpiResponse.de(movimentacaoEpiRepository.save(movimentacao));
    }

    /** Só movimentações pendentes podem ser alteradas. */
    @Transactional
    public MovimentacaoEpiResponse atualizar(Long id, MovimentacaoEpiRequest dados, Usuario logado) {
        MovimentacaoEpi movimentacao = obter(id);
        exigirPendente(movimentacao, "alterada");
        preencher(movimentacao, dados, logado);

        if (movimentacao.getStatus() == StatusMovimentacao.CONCLUIDO) {
            aplicarNoEstoque(movimentacao, logado);
        }
        return MovimentacaoEpiResponse.de(movimentacaoEpiRepository.save(movimentacao));
    }

    /** Conclui uma movimentação pendente: confere as regras de novo, com a data de hoje, e baixa o estoque. */
    @Transactional
    public MovimentacaoEpiResponse concluir(Long id, Usuario logado) {
        MovimentacaoEpi movimentacao = obter(id);
        exigirPendente(movimentacao, "concluída novamente");

        movimentacao.setData(LocalDate.now());
        validarRegras(movimentacao.getTipo(), movimentacao.getColaborador(), movimentacao.getEpi(),
                movimentacao.getData());
        aplicarNoEstoque(movimentacao, logado);
        movimentacao.setStatus(StatusMovimentacao.CONCLUIDO);

        return MovimentacaoEpiResponse.de(movimentacaoEpiRepository.save(movimentacao));
    }

    /** Regra: movimentação concluída já alterou o estoque e fica como histórico - não pode ser apagada. */
    @Transactional
    public void excluir(Long id) {
        MovimentacaoEpi movimentacao = obter(id);
        exigirPendente(movimentacao, "excluída");
        movimentacaoEpiRepository.delete(movimentacao);
    }

    private void preencher(MovimentacaoEpi movimentacao, MovimentacaoEpiRequest dados, Usuario logado) {
        String matricula = dados.matricula().trim();

        // Regra: o funcionário precisa estar cadastrado
        Colaborador colaborador = colaboradorRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RegraNegocioException(
                        "Não existe colaborador cadastrado com a matrícula " + matricula + "."));

        // Regra: o EPI precisa estar cadastrado
        Epi epi = epiRepository.findById(dados.epiId())
                .orElseThrow(() -> new NaoEncontradoException("EPI não encontrado (id " + dados.epiId() + ")."));

        validarRegras(dados.tipo(), colaborador, epi, dados.data());

        movimentacao.setTipo(dados.tipo());
        movimentacao.setColaborador(colaborador);
        movimentacao.setEpi(epi);
        movimentacao.setQuantidade(dados.quantidade());
        movimentacao.setData(dados.data());
        movimentacao.setStatus(dados.status());
        movimentacao.setObservacoes(Textos.limpar(dados.observacoes()));
        movimentacao.setRegistradoPor(logado.getNome());
    }

    private void validarRegras(TipoMovimentacaoEpi tipo, Colaborador colaborador, Epi epi, LocalDate data) {
        boolean saiDoEstoque = tipo == TipoMovimentacaoEpi.ENTREGA || tipo == TipoMovimentacaoEpi.TROCA;

        if (saiDoEstoque) {
            // Regra: só colaborador ativo recebe EPI
            if (colaborador.getStatus() != Status.ATIVO) {
                throw new RegraNegocioException("O colaborador " + colaborador.getNome()
                        + " está inativo e não pode receber EPIs.");
            }
            // Regra: EPI inativo não é entregue
            if (epi.getStatus() != Status.ATIVO) {
                throw new RegraNegocioException("O EPI \"" + epi.getNome() + "\" está inativo e não pode ser entregue.");
            }
            // Regra: não entregar EPI fora da validade
            if (epi.vencidoEm(data)) {
                throw new RegraNegocioException("O EPI \"" + epi.getNome() + "\" está vencido desde "
                        + epi.getValidade().format(DATA_BR) + " e não pode ser entregue.");
            }
        }
    }

    /** Entrega e troca tiram do estoque; devolução devolve ao estoque. */
    private void aplicarNoEstoque(MovimentacaoEpi movimentacao, Usuario logado) {
        boolean devolucao = movimentacao.getTipo() == TipoMovimentacaoEpi.DEVOLUCAO;
        TipoMovimentacaoEstoque tipoEstoque = devolucao ? TipoMovimentacaoEstoque.ENTRADA : TipoMovimentacaoEstoque.SAIDA;
        String observacao = movimentacao.getTipo().getRotulo() + (devolucao ? " de " : " para ")
                + movimentacao.getColaborador().getNome();

        estoqueService.movimentar(movimentacao.getEpi(), tipoEstoque, movimentacao.getQuantidade(),
                logado.getNome(), observacao);
    }

    private void exigirPendente(MovimentacaoEpi movimentacao, String acao) {
        if (movimentacao.getStatus() == StatusMovimentacao.CONCLUIDO) {
            throw new RegraNegocioException("Esta movimentação já foi concluída e não pode ser " + acao + ".");
        }
    }

    private MovimentacaoEpi obter(Long id) {
        return movimentacaoEpiRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Movimentação não encontrada (id " + id + ")."));
    }
}
