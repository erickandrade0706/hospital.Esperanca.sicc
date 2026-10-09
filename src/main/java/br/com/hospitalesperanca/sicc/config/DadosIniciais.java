package br.com.hospitalesperanca.sicc.config;

import br.com.hospitalesperanca.sicc.model.Colaborador;
import br.com.hospitalesperanca.sicc.model.Epi;
import br.com.hospitalesperanca.sicc.model.Funcao;
import br.com.hospitalesperanca.sicc.model.Participante;
import br.com.hospitalesperanca.sicc.model.Perfil;
import br.com.hospitalesperanca.sicc.model.StatusTreinamento;
import br.com.hospitalesperanca.sicc.model.Treinamento;
import br.com.hospitalesperanca.sicc.model.Usuario;
import br.com.hospitalesperanca.sicc.repository.ColaboradorRepository;
import br.com.hospitalesperanca.sicc.repository.EpiRepository;
import br.com.hospitalesperanca.sicc.repository.FuncaoRepository;
import br.com.hospitalesperanca.sicc.repository.TreinamentoRepository;
import br.com.hospitalesperanca.sicc.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Na primeira execução (banco vazio) grava os mesmos dados de exemplo que estavam fixos no front end:
 * os 3 usuários de teste do README, 2 colaboradores, EPIs, funções e treinamentos.
 * Também liga à tabela de funções os colaboradores antigos que ainda não têm funcao_id.
 * Para desligar: sicc.dados-iniciais=false no application.properties.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final EpiRepository epiRepository;
    private final FuncaoRepository funcaoRepository;
    private final TreinamentoRepository treinamentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean ativo;

    public DadosIniciais(UsuarioRepository usuarioRepository,
                         ColaboradorRepository colaboradorRepository,
                         EpiRepository epiRepository,
                         FuncaoRepository funcaoRepository,
                         TreinamentoRepository treinamentoRepository,
                         PasswordEncoder passwordEncoder,
                         @Value("${sicc.dados-iniciais:true}") boolean ativo) {
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.epiRepository = epiRepository;
        this.funcaoRepository = funcaoRepository;
        this.treinamentoRepository = treinamentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.ativo = ativo;
    }

    @Override
    public void run(String... args) {
        vincularColaboradoresSemFuncao();

        if (!ativo) {
            return;
        }

        if (usuarioRepository.count() == 0) {
            // Mesmos usuários do auth.ts - a senha é gravada criptografada (BCrypt)
            usuario("Cristian Freitas", "cristian@hospitalesperanca.com", "1001", "admin123", Perfil.ADMINISTRADOR);
            usuario("Mariana Souza", "mariana@hospitalesperanca.com", "1002", "rh123", Perfil.RH);
            usuario("Carlos Mendes", "carlos@hospitalesperanca.com", "1003", "seg123", Perfil.TECNICO_SEGURANCA);
        }

        if (epiRepository.count() == 0) {
            epi("EPI-001", "Luva de Látex", "Luva de procedimento descartável", "12345",
                    LocalDate.of(2027, 1, 31), "MedSafe", "Proteção das mãos", 100, 150);
            epi("EPI-002", "Máscara N95", "Respirador facial PFF2", "67890",
                    LocalDate.of(2026, 11, 30), "3M", "Proteção respiratória", 50, 80);
            // EPI vencido de propósito, para demonstrar a regra "não entregar EPI fora da validade"
            epi("EPI-003", "Óculos de Proteção", "Óculos de proteção incolor (lote vencido)", "11223",
                    LocalDate.of(2025, 12, 31), "ProtecVision", "Proteção ocular", 20, 30);
        }

        if (funcaoRepository.count() == 0) {
            funcao("Técnico de Enfermagem", "Atua no cuidado direto ao paciente", "Enfermagem",
                    List.of("Luva de Látex", "Máscara N95"));
            funcao("Técnico de Manutenção", "Responsável por reparos e manutenção predial", "Manutenção",
                    List.of("Luva de Látex"));
        }

        if (colaboradorRepository.count() == 0) {
            colaborador("2001", "Ana Paula Ribeiro", "123.456.789-00", "Técnico de Enfermagem",
                    LocalDate.of(2023, 3, 14), "(11) 98888-1234");
            colaborador("2002", "Bruno Costa Lima", "234.567.890-11", "Técnico de Manutenção",
                    LocalDate.of(2022, 7, 1), "(11) 97777-5678");
        }

        if (treinamentoRepository.count() == 0) {
            treinamento("NR-35", "Trabalho em Altura", "Carlos Silva", 8,
                    LocalDate.of(2026, 5, 10), LocalDate.of(2027, 5, 10), StatusTreinamento.CONCLUIDO,
                    List.of(participante("João Pedro", "Manutenção", true),
                            participante("Ana Maria", "Operações", false)));
            treinamento("NR-10", "Segurança em Instalações Elétricas", "Mariana Costa", 16,
                    LocalDate.of(2026, 9, 15), LocalDate.of(2028, 9, 15), StatusTreinamento.AGENDADO,
                    List.of(participante("Lucas Lima", "Elétrica", false)));
        }
    }

    private void usuario(String nome, String email, String matricula, String senha, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Colaboradores gravados antes da função virar um select não têm funcao_id.
     * Liga cada um à função de mesmo nome, quando ela existir na tabela de funções.
     */
    private void vincularColaboradoresSemFuncao() {
        List<Funcao> funcoes = funcaoRepository.findAll();
        for (Colaborador colaborador : colaboradorRepository.findAll()) {
            if (colaborador.getFuncao() != null) {
                continue;
            }
            funcoes.stream()
                    .filter(funcao -> funcao.getNome().equalsIgnoreCase(colaborador.getNomeFuncao()))
                    .findFirst()
                    .ifPresent(funcao -> {
                        colaborador.definirFuncao(funcao);
                        colaboradorRepository.save(colaborador);
                    });
        }
    }

    private void colaborador(String matricula, String nome, String cpf, String nomeFuncao,
                             LocalDate admissao, String contato) {
        Funcao funcao = funcaoRepository.findAll().stream()
                .filter(f -> f.getNome().equals(nomeFuncao))
                .findFirst()
                .orElseThrow();
        Colaborador colaborador = new Colaborador();
        colaborador.setMatricula(matricula);
        colaborador.setNome(nome);
        colaborador.setCpf(cpf);
        colaborador.definirFuncao(funcao);
        colaborador.setAdmissao(admissao);
        colaborador.setContato(contato);
        colaboradorRepository.save(colaborador);
    }

    private void epi(String codigo, String nome, String descricao, String ca, LocalDate validade,
                     String fabricante, String categoria, int estoqueMinimo, int quantidade) {
        Epi epi = new Epi();
        epi.setCodigo(codigo);
        epi.setNome(nome);
        epi.setDescricao(descricao);
        epi.setCa(ca);
        epi.setValidade(validade);
        epi.setFabricante(fabricante);
        epi.setCategoria(categoria);
        epi.setEstoqueMinimo(estoqueMinimo);
        epi.setQuantidadeEstoque(quantidade);
        epiRepository.save(epi);
    }

    private void funcao(String nome, String descricao, String setor, List<String> epis) {
        Funcao funcao = new Funcao();
        funcao.setNome(nome);
        funcao.setDescricao(descricao);
        funcao.setSetor(setor);
        funcao.setEpisObrigatorios(new java.util.ArrayList<>(epis));
        funcaoRepository.save(funcao);
    }

    private void treinamento(String norma, String titulo, String instrutor, int cargaHoraria,
                             LocalDate realizacao, LocalDate validade, StatusTreinamento status,
                             List<Participante> participantes) {
        Treinamento treinamento = new Treinamento();
        treinamento.setNorma(norma);
        treinamento.setTitulo(titulo);
        treinamento.setInstrutor(instrutor);
        treinamento.setCargaHoraria(cargaHoraria);
        treinamento.setDataRealizacao(realizacao);
        treinamento.setDataValidade(validade);
        treinamento.setStatus(status);
        treinamento.setParticipantes(new java.util.ArrayList<>(participantes));
        treinamentoRepository.save(treinamento);
    }

    private Participante participante(String nome, String setor, boolean presenca) {
        Participante participante = new Participante();
        participante.setNome(nome);
        participante.setSetor(setor);
        participante.setPresenca(presenca);
        return participante;
    }
}
