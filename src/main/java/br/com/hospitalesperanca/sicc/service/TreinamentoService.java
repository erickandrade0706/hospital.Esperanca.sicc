package br.com.hospitalesperanca.sicc.service;

import br.com.hospitalesperanca.sicc.exception.NaoEncontradoException;
import br.com.hospitalesperanca.sicc.exception.RegraNegocioException;
import br.com.hospitalesperanca.sicc.model.Participante;
import br.com.hospitalesperanca.sicc.model.StatusTreinamento;
import br.com.hospitalesperanca.sicc.model.Treinamento;
import br.com.hospitalesperanca.sicc.repository.TreinamentoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CRUD e regras de negócio de treinamentos. */
@Service
public class TreinamentoService {

    private final TreinamentoRepository treinamentoRepository;

    public TreinamentoService(TreinamentoRepository treinamentoRepository) {
        this.treinamentoRepository = treinamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<Treinamento> listar() {
        return treinamentoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataRealizacao"));
    }

    @Transactional(readOnly = true)
    public Treinamento buscar(Long id) {
        return treinamentoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Treinamento não encontrado (id " + id + ")."));
    }

    @Transactional
    public Treinamento criar(Treinamento dados) {
        Treinamento novo = new Treinamento();
        copiar(dados, novo);
        return treinamentoRepository.save(novo);
    }

    @Transactional
    public Treinamento atualizar(Long id, Treinamento dados) {
        Treinamento existente = buscar(id);
        copiar(dados, existente);
        return treinamentoRepository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        treinamentoRepository.delete(buscar(id));
    }

    private void copiar(Treinamento origem, Treinamento destino) {
        StatusTreinamento status = origem.getStatus() == null ? StatusTreinamento.AGENDADO : origem.getStatus();

        // Regra: a validade do treinamento não pode ser anterior à data em que ele foi realizado
        if (origem.getDataValidade() != null && origem.getDataValidade().isBefore(origem.getDataRealizacao())) {
            throw new RegraNegocioException("A data de validade não pode ser anterior à data de realização.");
        }
        // Regra: não dá para marcar como concluído um treinamento que ainda vai acontecer
        if (status == StatusTreinamento.CONCLUIDO && origem.getDataRealizacao().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Um treinamento com data futura não pode ter o status Concluído.");
        }

        destino.setTitulo(origem.getTitulo().trim());
        destino.setNorma(Textos.limpar(origem.getNorma()));
        destino.setInstrutor(Textos.limpar(origem.getInstrutor()));
        destino.setCargaHoraria(origem.getCargaHoraria());
        destino.setDataRealizacao(origem.getDataRealizacao());
        destino.setDataValidade(origem.getDataValidade());
        destino.setStatus(status);

        List<Participante> participantes = new ArrayList<>();
        if (origem.getParticipantes() != null) {
            for (Participante recebido : origem.getParticipantes()) {
                if (recebido == null) {
                    continue;
                }
                Participante participante = new Participante();
                participante.setNome(recebido.getNome().trim());
                participante.setSetor(Textos.limpar(recebido.getSetor()));
                participante.setPresenca(recebido.isPresenca());
                participantes.add(participante);
            }
        }
        destino.setParticipantes(participantes);
    }
}
