import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Usuarios (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD e integracao de seguranca:
 * [1] Inclusao (com criacao de autenticacao e selecao de perfil)
 * [2] Alteracao
 * [3] Exclusao
 * [4] Exibicao de todos (via ArrayList)
 * [5] Exibicao de detalhes por ID
 * [6] Busca por CPF
 * [7] Busca por E-mail
 * [8] Simulacao de Login / Autenticacao
 * [9] Submenu de Credenciais & MFA
 * [0] Voltar ao menu anterior
 */
public final class UsuarioView {

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final PerfilInvestidorDAO perfilDAO = new PerfilInvestidorDAO();
    private static final AutenticacaoDAO autenticacaoDAO = new AutenticacaoDAO();
    private static final AutenticacaoMultifatorDAO mfaDAO = new AutenticacaoMultifatorDAO();

    private UsuarioView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE USUÁRIOS E INVESTIDORES");
            System.out.println("  [1] Inclusão                 - Cadastrar novo investidor/usuário");
            System.out.println("  [2] Alteração                - Atualizar cadastro de usuário");
            System.out.println("  [3] Exclusão                 - Remover usuário do banco");
            System.out.println("  [4] Exibição de Todos        - Listar todos os usuários (ArrayList)");
            System.out.println("  [5] Exibição de Registro     - Consultar detalhes completos por ID");
            System.out.println("  [6] Consulta por CPF         - Localizar usuário pelo CPF");
            System.out.println("  [7] Consulta por E-mail      - Localizar usuário pelo e-mail");
            System.out.println("  [8] Testar Login / Acesso    - Simular autenticação de usuário");
            System.out.println("  [9] Gerenciar Credenciais    - Submenu de Autenticação e MFA");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 9);

            switch (opcao) {
                case 1 -> incluirUsuario();
                case 2 -> alterarUsuario();
                case 3 -> excluirUsuario();
                case 4 -> exibirTodos();
                case 5 -> exibirPorId();
                case 6 -> buscarPorCpf();
                case 7 -> buscarPorEmail();
                case 8 -> simularLogin();
                case 9 -> AutenticacaoView.exibirMenu();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirUsuario() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE NOVO USUÁRIO");

        try {
            String nome = ConsoleUtils.lerTexto("Nome completo do usuário: ", true);
            String email = Validador.lerEmail("E-mail: ", true);

            // Verifica duplicidade de e-mail
            Usuario usuarioExistenteEmail = usuarioDAO.buscarPorEmail(email);
            if (usuarioExistenteEmail != null) {
                ConsoleUtils.imprimirAviso("Já existe um usuário cadastrado com o e-mail '" + email + "' (ID: " + usuarioExistenteEmail.getId() + ").");
                ConsoleUtils.pausar();
                return;
            }

            String cpf = Validador.lerCpf("CPF (11 dígitos): ", true);

            // Verifica duplicidade de CPF
            Usuario usuarioExistenteCpf = usuarioDAO.buscarPorCpf(cpf);
            if (usuarioExistenteCpf != null) {
                ConsoleUtils.imprimirAviso("Já existe um usuário cadastrado com o CPF " + cpf + " (ID: " + usuarioExistenteCpf.getId() + ").");
                ConsoleUtils.pausar();
                return;
            }

            String telefone = ConsoleUtils.lerTexto("Telefone com DDD (ex: (11) 98765-4321): ", false);
            String senha = ConsoleUtils.lerTexto("Senha de acesso: ", true);

            // 1. Selecao do Perfil de Investidor
            PerfilInvestidor perfilSelecionado = selecionarPerfilInvestidor();

            // 2. Criacao da Credencial de Autenticacao
            Autenticacao autenticacaoCriada = null;
            if (ConsoleUtils.confirmar("Deseja criar a credencial de autenticação automaticamente para este usuário?")) {
                boolean ativarMfa = ConsoleUtils.confirmar("Deseja ativar proteção de Autenticação Multifator (MFA)?");
                AutenticacaoMultifator mfa = null;

                if (ativarMfa) {
                    mfa = new AutenticacaoMultifator();
                    mfa.gerarCodigo();
                    mfa = mfaDAO.inserir(mfa);
                    ConsoleUtils.imprimirSucesso("Token MFA gerado com sucesso: " + mfa.getCodigo());
                }

                Autenticacao aut = new Autenticacao(email, senha);
                aut.setAutenticacaoMultifator(mfa);
                autenticacaoCriada = autenticacaoDAO.inserir(aut);
                ConsoleUtils.imprimirSucesso("Credencial de autenticação gerada com ID: " + autenticacaoCriada.getId());
            }

            // 3. Montagem do objeto Usuario
            Usuario novoUsuario = new Usuario(nome, email, senha, cpf, telefone);
            novoUsuario.setPerfilInvestidor(perfilSelecionado);
            novoUsuario.setAutenticacao(autenticacaoCriada);

            System.out.println("\nDados do novo usuário para confirmação:");
            System.out.println("  Nome       : " + novoUsuario.getNome());
            System.out.println("  E-mail     : " + novoUsuario.getEmail());
            System.out.println("  CPF        : " + novoUsuario.getCpf());
            System.out.println("  Telefone   : " + (novoUsuario.getTelefone() != null && !novoUsuario.getTelefone().isEmpty() ? novoUsuario.getTelefone() : "-"));
            System.out.println("  Perfil     : " + (perfilSelecionado != null ? perfilSelecionado.getNomePerfil() + " (" + perfilSelecionado.getNivelRisco() + ")" : "Nenhum"));
            System.out.println("  Autentic.  : " + (autenticacaoCriada != null ? "ID " + autenticacaoCriada.getId() : "Nenhuma"));

            if (ConsoleUtils.confirmar("Confirma a gravação do usuário no banco de dados Oracle?")) {
                Usuario salvo = usuarioDAO.inserir(novoUsuario);
                ConsoleUtils.imprimirSucesso("Usuário cadastrado com sucesso! ID gerado no Oracle: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Cadastro de usuário cancelado.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar usuário", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarUsuario() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE DADOS DE USUÁRIO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do usuário que deseja alterar: ", 1L, null);
            Usuario usuario = usuarioDAO.buscarPorId(id);

            if (usuario == null) {
                ConsoleUtils.imprimirAviso("Nenhum usuário encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais do usuário:");
            System.out.println("  ID       : " + usuario.getId());
            System.out.println("  Nome     : " + usuario.getNome());
            System.out.println("  E-mail   : " + usuario.getEmail());
            System.out.println("  CPF      : " + usuario.getCpf());
            System.out.println("  Telefone : " + (usuario.getTelefone() != null ? usuario.getTelefone() : "-"));
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + usuario.getNome() + "]: ", usuario.getNome());
            String novoEmail = Validador.lerEmailOpcional("Novo E-mail [" + usuario.getEmail() + "]: ", usuario.getEmail());
            String novoCpf = Validador.lerCpfOpcional("Novo CPF [" + usuario.getCpf() + "]: ", usuario.getCpf());
            String novoTelefone = ConsoleUtils.lerTextoOpcional("Novo Telefone [" + (usuario.getTelefone() != null ? usuario.getTelefone() : "") + "]: ", usuario.getTelefone());
            String novaSenha = ConsoleUtils.lerTextoOpcional("Nova Senha (ENTER para manter atual): ", usuario.getSenha());

            usuario.setNome(novoNome);
            usuario.setEmail(novoEmail);
            usuario.setCpf(novoCpf);
            usuario.setTelefone(novoTelefone);
            usuario.setSenha(novaSenha);

            if (ConsoleUtils.confirmar("Deseja alterar o Perfil de Investidor vinculado?")) {
                PerfilInvestidor novoPerfil = selecionarPerfilInvestidor();
                if (novoPerfil != null) {
                    usuario.setPerfilInvestidor(novoPerfil);
                }
            }

            if (ConsoleUtils.confirmar("Confirma as atualizações no cadastro do usuário?")) {
                boolean sucesso = usuarioDAO.atualizar(usuario);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Cadastro do usuário atualizado com sucesso no Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar os dados do usuário.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar usuário", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirUsuario() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE USUÁRIO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do usuário que deseja excluir: ", 1L, null);
            Usuario usuario = usuarioDAO.buscarPorId(id);

            if (usuario == null) {
                ConsoleUtils.imprimirAviso("Nenhum usuário encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados do usuário a ser excluído:");
            System.out.println("  ID    : " + usuario.getId());
            System.out.println("  Nome  : " + usuario.getNome());
            System.out.println("  E-mail: " + usuario.getEmail());
            System.out.println("  CPF   : " + usuario.getCpf());

            if (ConsoleUtils.confirmar("ATENÇÃO: Deseja realmente excluir este usuário do sistema?")) {
                boolean sucesso = usuarioDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Usuário removido com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir o usuário.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir usuário", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodos() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODOS OS USUÁRIOS (ArrayList)");

        try {
            ArrayList<Usuario> usuarios = usuarioDAO.listarTodos();

            if (usuarios.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum usuário cadastrado no momento.");
            } else {
                imprimirTabelaUsuarios(usuarios);
                System.out.println("  Total de usuários cadastrados: " + usuarios.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar usuários", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE USUÁRIO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do usuário: ", 1L, null);
            Usuario usuario = usuarioDAO.buscarPorId(id);

            if (usuario == null) {
                ConsoleUtils.imprimirAviso("Nenhum usuário encontrado com o ID " + id + ".");
            } else {
                imprimirDetalhesUsuario(usuario);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar usuário por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorCpf() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE USUÁRIO POR CPF");

        try {
            String cpf = Validador.lerCpf("Informe o CPF: ", true);
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);

            if (usuario == null) {
                ConsoleUtils.imprimirAviso("Nenhum usuário localizado com o CPF " + cpf + ".");
            } else {
                imprimirDetalhesUsuario(usuario);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar usuário por CPF", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorEmail() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE USUÁRIO POR E-MAIL");

        try {
            String email = Validador.lerEmail("Informe o e-mail: ", true);
            Usuario usuario = usuarioDAO.buscarPorEmail(email);

            if (usuario == null) {
                ConsoleUtils.imprimirAviso("Nenhum usuário localizado com o e-mail '" + email + "'.");
            } else {
                imprimirDetalhesUsuario(usuario);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar usuário por e-mail", e);
        }

        ConsoleUtils.pausar();
    }

    private static void simularLogin() {
        ConsoleUtils.imprimirSubcabecalho("TESTE DE AUTENTICAÇÃO E LOGIN DE USUÁRIO");

        try {
            String email = Validador.lerEmail("E-mail para login: ", true);
            String senha = ConsoleUtils.lerTexto("Senha de acesso: ", true);

            Usuario usuario = usuarioDAO.buscarPorEmail(email);
            if (usuario == null) {
                ConsoleUtils.imprimirErro("AUTENTICAÇÃO RECUSADA: Nenhum usuário encontrado com este e-mail.");
                ConsoleUtils.pausar();
                return;
            }

            // Carrega a credencial associada para validacao completa
            if (usuario.getAutenticacao() != null && usuario.getAutenticacao().getId() != null) {
                Autenticacao aut = autenticacaoDAO.buscarPorId(usuario.getAutenticacao().getId());
                usuario.setAutenticacao(aut);
            }

            // Valida utilizando o metodo da classe de modelo Usuario
            boolean credenciaisValidas = usuario.autenticar(email, senha);

            if (!credenciaisValidas) {
                ConsoleUtils.imprimirErro("AUTENTICAÇÃO RECUSADA: Senha incorreta!");
                ConsoleUtils.pausar();
                return;
            }

            ConsoleUtils.imprimirSucesso("1º FATOR APROVADO: E-mail e senha conferem!");

            // Verifica se possui MFA ativado
            if (usuario.getAutenticacao() != null && usuario.getAutenticacao().getAutenticacaoMultifator() != null) {
                Long idMfa = usuario.getAutenticacao().getAutenticacaoMultifator().getId();
                AutenticacaoMultifator mfa = mfaDAO.buscarPorId(idMfa);

                if (mfa != null && mfa.getCodigo() != null) {
                    System.out.println("  [MFA ATIVO] Este usuário possui duplo fator habilitado.");
                    String codigoDigitado = ConsoleUtils.lerTexto("Digite o token de 6 dígitos (ou 'VER' para exibir o código de teste): ", true);

                    if (codigoDigitado.equalsIgnoreCase("VER")) {
                        System.out.println("  [TESTE/DEBUG] O token atual no Oracle é: " + mfa.getCodigo());
                        codigoDigitado = ConsoleUtils.lerTexto("Digite agora o token: ", true);
                    }

                    if (mfa.validarCodigo(codigoDigitado)) {
                        ConsoleUtils.imprimirSucesso("2º FATOR (MFA) APROVADO COM SUCESSO!");
                    } else {
                        ConsoleUtils.imprimirErro("FALHA NO SEGUNDO FATOR: Token MFA incorreto ou expirado!");
                        ConsoleUtils.pausar();
                        return;
                    }
                }
            }

            ConsoleUtils.imprimirSucesso("ACESSO CONCEDIDO! Bem-vindo(a), " + usuario.getNome() + "!");
        } catch (SQLException e) {
            tratarErroSql("autenticar usuário", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static PerfilInvestidor selecionarPerfilInvestidor() throws SQLException {
        ArrayList<PerfilInvestidor> perfis = perfilDAO.listarTodos();

        if (perfis.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhum Perfil de Investidor cadastrado no banco.");
            return null;
        }

        System.out.println("\nPerfis de Investidor disponíveis:");
        for (PerfilInvestidor p : perfis) {
            System.out.printf("  [ID %d] %-15s - Risco: %-6s (%s)\n",
                    p.getId(), p.getNomePerfil(), p.getNivelRisco(), p.getDescricao());
        }

        Long idPerfil = ConsoleUtils.lerLong("Informe o ID do Perfil desejado (ou 0 para nenhum): ", 0L, null);
        if (idPerfil == 0L) {
            return null;
        }

        PerfilInvestidor escolhido = perfilDAO.buscarPorId(idPerfil);
        if (escolhido == null) {
            ConsoleUtils.imprimirAviso("ID de perfil não encontrado. Nenhum perfil será associado.");
            return null;
        }

        return escolhido;
    }

    private static void imprimirDetalhesUsuario(Usuario usuario) throws SQLException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DO USUÁRIO (ID: " + usuario.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID             : " + usuario.getId());
        System.out.println("  Nome Completo  : " + usuario.getNome());
        System.out.println("  E-mail         : " + usuario.getEmail());
        System.out.println("  CPF            : " + usuario.getCpf());
        System.out.println("  Telefone       : " + (usuario.getTelefone() != null ? usuario.getTelefone() : "-"));

        if (usuario.getPerfilInvestidor() != null && usuario.getPerfilInvestidor().getId() != null) {
            PerfilInvestidor perfil = perfilDAO.buscarPorId(usuario.getPerfilInvestidor().getId());
            if (perfil != null) {
                System.out.println("  Perfil Risco   : " + perfil.getNomePerfil() + " (" + perfil.getNivelRisco() + ")");
                System.out.println("  Diretriz       : " + perfil.avaliarRisco());
            } else {
                System.out.println("  Perfil Risco   : ID " + usuario.getPerfilInvestidor().getId());
            }
        } else {
            System.out.println("  Perfil Risco   : Não associado");
        }

        if (usuario.getAutenticacao() != null && usuario.getAutenticacao().getId() != null) {
            Autenticacao aut = autenticacaoDAO.buscarPorId(usuario.getAutenticacao().getId());
            if (aut != null) {
                String statusMfa = (aut.getAutenticacaoMultifator() != null && aut.getAutenticacaoMultifator().getId() != null)
                        ? "Ativado (ID MFA: " + aut.getAutenticacaoMultifator().getId() + ")"
                        : "Desativado";
                System.out.println("  Autenticação   : Login '" + aut.getLogin() + "' | MFA: " + statusMfa);
            }
        } else {
            System.out.println("  Autenticação   : Nenhuma credencial vinculada");
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaUsuarios(ArrayList<Usuario> usuarios) throws SQLException {
        System.out.println("+--------+---------------------------+--------------------------------+-----------------+---------------+");
        System.out.println("| ID     | NOME                      | E-MAIL                         | CPF             | TELEFONE      |");
        System.out.println("+--------+---------------------------+--------------------------------+-----------------+---------------+");

        for (Usuario u : usuarios) {
            String nome = u.getNome();
            if (nome != null && nome.length() > 25) {
                nome = nome.substring(0, 22) + "...";
            }
            String email = u.getEmail();
            if (email != null && email.length() > 30) {
                email = email.substring(0, 27) + "...";
            }

            System.out.printf("| %-6d | %-25s | %-30s | %-15s | %-13s |\n",
                    u.getId(),
                    nome != null ? nome : "-",
                    email != null ? email : "-",
                    u.getCpf() != null ? u.getCpf() : "-",
                    u.getTelefone() != null ? u.getTelefone() : "-");
        }

        System.out.println("+--------+---------------------------+--------------------------------+-----------------+---------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            ConsoleUtils.imprimirErro("Não é possível excluir este usuário pois existem relatórios ou vínculos corporativos dependentes cadastrados.");
        } else if (e.getErrorCode() == 1) {
            ConsoleUtils.imprimirErro("Conflito de chave única no Oracle: CPF, login ou e-mail já cadastrado.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
