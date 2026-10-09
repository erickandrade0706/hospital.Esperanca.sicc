# SICC - Back end (API REST)

Back end do **SICC - Sistema Interno de Cadastro e Controle** (Hospital Esperança), feito em
**Java 17 + Spring Boot 3**, para ser aberto no **IntelliJ IDEA**.

## Como rodar no IntelliJ

1. `File > Open` e selecione a pasta `backend` (a que tem o `pom.xml`). Aceite "Load Maven Project".
2. Espere o IntelliJ baixar as dependências (primeira vez demora alguns minutos; precisa de internet).
3. Se pedir JDK: `File > Project Structure > SDK > Add SDK > Download JDK` e escolha a versão **17** (ou mais nova).
4. Abra `src/main/java/.../SiccApplication.java` e clique na seta verde (**Run**).
5. A API sobe em `http://localhost:8080`. O banco (H2) é criado sozinho na pasta `data/`.

Para testar sem o front: abra o arquivo `testes.http` e clique nas setas verdes (ou use Postman/Insomnia).

### Usuários de teste (os mesmos do front)

| Perfil | Matrícula | E-mail | Senha |
|---|---|---|---|
| Administrador | 1001 | cristian@hospitalesperanca.com | admin123 |
| RH | 1002 | mariana@hospitalesperanca.com | rh123 |
| Técnico de Segurança | 1003 | carlos@hospitalesperanca.com | seg123 |

## Onde está cada coisa

Tudo dentro de `src/main/java/br/com/hospitalesperanca/sicc/`:

| Pasta | O que tem |
|---|---|
| `model/` | Tabelas do banco (Usuario, Colaborador, Epi, Funcao, Treinamento, MovimentacaoEpi, MovimentacaoEstoque) e os campos obrigatórios |
| `repository/` | Acesso ao banco (consultas como "existe matrícula X?") |
| `service/` | **Regras de negócio** e o CRUD de cada cadastro |
| `controller/` | Endereços da API (`/api/...`) chamados pelo front |
| `security/` | Token JWT e o filtro que confere o token em cada requisição |
| `config/` | `SecurityConfig` (quem acessa o quê) e `DadosIniciais` (dados de exemplo) |
| `dto/` | Formato dos dados de entrada/saída (login, usuário, movimentações) |
| `exception/` | Erros padronizados em JSON |

Configurações: `src/main/resources/application.properties`.

## Segurança do login

Baseada no que já existia no JavaScript (`login.ts`, `auth.ts`, `auth-guard.ts`, `admin-guard.ts`), agora no servidor:

| No front (JavaScript) | No back end |
|---|---|
| Login por matrícula ou e-mail | `AuthService.login` - mesma busca (e-mail sem diferenciar maiúsculas) |
| Regex de e-mail e matrícula (4 a 10 dígitos) | Mesmas regex em `AuthService` |
| Campos obrigatórios | `LoginRequest` com as mesmas mensagens |
| Senhas em texto puro no código | Senhas com hash **BCrypt** no banco |
| `authGuard` (precisa estar logado) | Toda rota exige **token JWT** válido (`JwtAuthFilter`) |
| `adminGuard` (Configurações) | `/api/usuarios` só para Administrador |
| Abas por perfil (`cadastro.ts`) | Permissão por perfil em `SecurityConfig` |

Proteções extras: mensagem única para usuário/senha errados, bloqueio de 15 minutos após 5 senhas erradas,
usuário inativo não entra, token expira em 8 horas, usuário inativado perde o acesso na hora.

**Permissões**

| Endereço | Quem acessa |
|---|---|
| `POST /api/auth/login` | Todos |
| `/api/colaboradores` | Administrador, RH |
| `/api/epis`, `/api/funcoes`, `/api/movimentacoes`, `/api/estoque`, `/api/treinamentos` | Administrador, Técnico de Segurança |
| `GET /api/funcoes` (só consulta, para o select de função) | Administrador, RH, Técnico de Segurança |
| `/api/usuarios` | Administrador |

## Endereços da API (CRUD)

Todos (menos o login) precisam do cabeçalho `Authorization: Bearer <token>`.

| Recurso | Listar | Buscar | Criar | Alterar | Excluir |
|---|---|---|---|---|---|
| Colaboradores | `GET /api/colaboradores` | `GET /{id}` | `POST` | `PUT /{id}` | `DELETE /{id}` |
| EPIs | `GET /api/epis` | `GET /{id}` | `POST` | `PUT /{id}` | `DELETE /{id}` |
| Funções | `GET /api/funcoes` | `GET /{id}` | `POST` | `PUT /{id}` | `DELETE /{id}` |
| Treinamentos | `GET /api/treinamentos` | `GET /{id}` | `POST` | `PUT /{id}` | `DELETE /{id}` |
| Usuários | `GET /api/usuarios` | `GET /{id}` | `POST` | `PUT /{id}`, `PATCH /{id}/status` | `DELETE /{id}` |
| Entrega/devolução/troca de EPI | `GET /api/movimentacoes` (filtros: `tipo`, `dataInicio`, `dataFim`, `setor`, `colaborador`) | `GET /{id}` | `POST` | `PUT /{id}`, `PATCH /{id}/concluir` | `DELETE /{id}` |
| Estoque | `GET /api/estoque` e `GET /api/estoque/movimentacoes` | - | `POST /api/estoque/movimentacoes` | - | - |

Login: `POST /api/auth/login` com `{"identificacao": "1001", "senha": "admin123"}`. Usuário logado: `GET /api/auth/me`.

Os JSON usam os mesmos nomes de campos dos `models` do Angular (ex.: `status: "Ativo"`, datas `AAAA-MM-DD`).

## Regras de negócio

**Identificadores únicos (não cadastra repetido)** - resposta HTTP 409
- O `id` é sempre gerado pelo banco; um `id` enviado no cadastro é ignorado.
- Colaborador: matrícula e CPF. EPI: código. Função: nome. Usuário: matrícula e e-mail.

**Campos obrigatórios (iguais aos `required` do HTML)** - resposta HTTP 400 com a mensagem de cada campo
- Colaborador: matrícula, nome, CPF, função (`funcaoId`, escolhida no select) e data de admissão. O setor é puxado da função.
- EPI: código, nome, CA, validade, fabricante, categoria.
- Função: nome, setor. Treinamento: título, data da realização.
- Movimentação de EPI: tipo, matrícula, EPI, quantidade, data, status. Estoque: EPI, tipo, quantidade, responsável.
- Usuário: nome, e-mail, matrícula, perfil e senha (mínimo 6 caracteres).

**Validade e estoque** - resposta HTTP 422
- Não entrega (nem troca) EPI **vencido**, EPI inativo ou para colaborador inativo/não cadastrado.
- Não dá entrada no estoque de EPI vencido.
- Estoque nunca fica negativo; entrega/troca baixam o estoque e devolução devolve.
- Movimentação pendente não mexe no estoque; concluída não pode ser alterada nem excluída.
- A quantidade em estoque só muda por movimentação, nunca pelo cadastro do EPI.

**Outras**
- Datas: admissão e data de movimentação não podem ser futuras; validade do treinamento não pode ser anterior à realização; treinamento futuro não pode estar "Concluído".
- Colaborador ou EPI com movimentações não pode ser excluído (use o status Inativo).
- A função do colaborador vem da tabela de funções (coluna `funcao_id`): precisa existir e estar ativa, e o setor é copiado dela. Se a função mudar de nome ou setor, os colaboradores dela são atualizados. Função usada por colaborador não pode ser excluída.
- Usuário não pode excluir/inativar a si mesmo; o sistema sempre mantém um administrador ativo.

## Observações

- O EPI "Óculos de Proteção" (EPI-003) já vem **vencido** de propósito, para demonstrar a regra da validade.
- Para recomeçar do zero, pare a aplicação e apague a pasta `data/`.
- Para usar MySQL, veja os comentários no `pom.xml` e no `application.properties`.
- O front (Angular) já chama esta API: login, cadastros, treinamentos, estoque, gestão de EPIs e lista de usuários.
  Suba o back end primeiro e depois rode `ng serve` na pasta do front. O endereço da API fica em `src/app/shared/api.ts`.
- Ainda não feito: recuperação de senha por e-mail e dados do Dashboard e dos Relatórios (seguem com dados de exemplo no front).
