import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Camada de visualizacao e controle para o Modulo de Autenticacao e MFA (Fase 7).
 *
 * Implementa o submenu com operacoes de CRUD e utilitarios de seguranca:
 * [1] Inclusao de Credencial
 * [2] Alteracao de Credencial
 * [3] Exclusao de Credencial
 * [4] Exibicao de Todas as Credenciais (ArrayList)
 * [5] Exibicao por ID
 * [6] Gerar Novo Codigo MFA
 * [7] Validar Codigo MFA
 * [0] Voltar ao menu anterior
 */
public final class AutenticacaoView {

    private static final AutenticacaoDAO autenticacaoDAO = new AutenticacaoDAO();
    private static final AutenticacaoMultifatorDAO mfaDAO = new AutenticacaoMultifatorDAO();

    private AutenticacaoView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: SEGURANÇA E AUTENTICAÇÃO (MFA)");
            System.out.println("  [1] Inclusão de Credencial      - Cadastrar novo login/senha");
            System.out.println("  [2] Alteração de Credencial     - Atualizar senha/dados de acesso");
            System.out.println("  [3] Exclusão de Credencial      - Remover credencial do banco");
            System.out.println("  [4] Exibição de Todas           - Listar credenciais cadastradas (ArrayList)");
            System.out.println("  [5] Exibição por ID             - Consultar detalhes de uma credencial");
            System.out.println("  [6] Gerar Código MFA (Token)    - Emitir token temporário de 6 dígitos");
            System.out.println("  [7] Validar Código MFA          - Testar autenticação de duplo fator");
            System.out.println("  [0] Voltar ao Menu Anterior");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirAutenticacao();
                case 2 -> alterarAutenticacao();
                case 3 -> excluirAutenticacao();
                case 4 -> exibirTodas();
                case 5 -> exibirPorId();
                case 6 -> gerarCodigoMfa();
                case 7 -> validarCodigoMfa();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirAutenticacao() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE CREDENCIAL DE ACESSO");

        try {
            String login = ConsoleUtils.lerTexto("Login de acesso (ex: email ou usuario): ", true);

            // Verifica duplicidade de login
            Autenticacao existente = autenticacaoDAO.buscarPorLogin(login);
            if (existente != null) {
                ConsoleUtils.imprimirAviso("Já existe uma credencial com o login '" + login + "' (ID: " + existente.getId() + ").");
                ConsoleUtils.pausar();
                return;
            }

            String senha = ConsoleUtils.lerTexto("Senha de acesso: ", true);
            boolean vincularMfa = ConsoleUtils.confirmar("Deseja ativar e vincular Autenticação Multifator (MFA) para esta conta?");

            AutenticacaoMultifator mfa = null;
            if (vincularMfa) {
                mfa = new AutenticacaoMultifator();
                mfa.gerarCodigo();
                mfa = mfaDAO.inserir(mfa);
                ConsoleUtils.imprimirSucesso("MFA ativado! Token inicial gerado: " + mfa.getCodigo() + " (Válido até " + ConsoleUtils.formatarDataHora(mfa.getExpiracao()) + ")");
            }

            Autenticacao novaAut = new Autenticacao(login, senha);
            novaAut.setAutenticacaoMultifator(mfa);

            if (ConsoleUtils.confirmar("Confirma o cadastro desta credencial?")) {
                Autenticacao salva = autenticacaoDAO.inserir(novaAut);
                ConsoleUtils.imprimirSucesso("Credencial cadastrada com sucesso! ID gerado no Oracle: " + salva.getId());
            } else {
                ConsoleUtils.imprimirAviso("Operação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar credencial", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarAutenticacao() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE CREDENCIAL DE ACESSO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da credencial que deseja alterar: ", 1L, null);
            Autenticacao aut = autenticacaoDAO.buscarPorId(id);

            if (aut == null) {
                ConsoleUtils.imprimirAviso("Nenhuma credencial encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println("  ID   : " + aut.getId());
            System.out.println("  Login: " + aut.getLogin());
            System.out.println("  Senha: [protegida]");
            System.out.println("  MFA  : " + (aut.getAutenticacaoMultifator() != null ? "ID " + aut.getAutenticacaoMultifator().getId() : "Não configurado"));

            String novoLogin = ConsoleUtils.lerTextoOpcional("Novo Login [" + aut.getLogin() + "]: ", aut.getLogin());
            String novaSenha = ConsoleUtils.lerTextoOpcional("Nova Senha (ENTER para manter atual): ", aut.getSenha());

            if (!novoLogin.equalsIgnoreCase(aut.getLogin())) {
                Autenticacao comMesmoLogin = autenticacaoDAO.buscarPorLogin(novoLogin);
                if (comMesmoLogin != null && !comMesmoLogin.getId().equals(aut.getId())) {
                    ConsoleUtils.imprimirAviso("O login '" + novoLogin + "' já está em uso pela credencial ID " + comMesmoLogin.getId() + ".");
                    ConsoleUtils.pausar();
                    return;
                }
            }

            aut.setLogin(novoLogin);
            aut.setSenha(novaSenha);

            if (ConsoleUtils.confirmar("Confirma a atualização da credencial?")) {
                boolean sucesso = autenticacaoDAO.atualizar(aut);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Credencial atualizada com sucesso no banco Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar a credencial.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar credencial", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirAutenticacao() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE CREDENCIAL");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da credencial que deseja excluir: ", 1L, null);
            Autenticacao aut = autenticacaoDAO.buscarPorId(id);

            if (aut == null) {
                ConsoleUtils.imprimirAviso("Nenhuma credencial encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados a serem excluídos:");
            System.out.println("  ID   : " + aut.getId());
            System.out.println("  Login: " + aut.getLogin());

            if (ConsoleUtils.confirmar("Confirma a exclusão definitiva desta credencial?")) {
                boolean sucesso = autenticacaoDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Credencial removida com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir a credencial.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir credencial", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodas() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODAS AS CREDENCIAIS (ArrayList)");

        try {
            ArrayList<Autenticacao> lista = autenticacaoDAO.listarTodos();

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma credencial cadastrada no momento.");
            } else {
                System.out.println("+--------+------------------------------------------+--------------------+");
                System.out.println("| ID     | LOGIN                                    | VÍNCULO MFA        |");
                System.out.println("+--------+------------------------------------------+--------------------+");

                for (Autenticacao a : lista) {
                    String login = a.getLogin();
                    if (login != null && login.length() > 40) {
                        login = login.substring(0, 37) + "...";
                    }
                    String mfaDesc = (a.getAutenticacaoMultifator() != null && a.getAutenticacaoMultifator().getId() != null)
                            ? "ID MFA " + a.getAutenticacaoMultifator().getId()
                            : "Nenhum";

                    System.out.printf("| %-6d | %-40s | %-18s |\n",
                            a.getId(),
                            login != null ? login : "-",
                            mfaDesc);
                }

                System.out.println("+--------+------------------------------------------+--------------------+");
                System.out.println("  Total de credenciais cadastradas: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar credenciais", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE CREDENCIAL POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da credencial: ", 1L, null);
            Autenticacao aut = autenticacaoDAO.buscarPorId(id);

            if (aut == null) {
                ConsoleUtils.imprimirAviso("Nenhuma credencial encontrada com o ID " + id + ".");
            } else {
                System.out.println("\n--------------------------------------------------------------------------------");
                System.out.println("  DETALHES DA CREDENCIAL (ID: " + aut.getId() + ")");
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("  ID     : " + aut.getId());
                System.out.println("  Login  : " + aut.getLogin());
                System.out.println("  Senha  : [armazenada no banco Oracle]");

                if (aut.getAutenticacaoMultifator() != null && aut.getAutenticacaoMultifator().getId() != null) {
                    AutenticacaoMultifator mfa = mfaDAO.buscarPorId(aut.getAutenticacaoMultifator().getId());
                    if (mfa != null) {
                        System.out.println("  MFA ID : " + mfa.getId());
                        System.out.println("  Código : " + mfa.getCodigo());
                        System.out.println("  Expira : " + ConsoleUtils.formatarDataHora(mfa.getExpiracao()));
                    }
                } else {
                    System.out.println("  MFA    : Desativado para esta credencial.");
                }
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            tratarErroSql("buscar credencial por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void gerarCodigoMfa() {
        ConsoleUtils.imprimirSubcabecalho("GERAÇÃO DE NOVO CÓDIGO MFA (TOKEN)");

        try {
            Long idMfa = ConsoleUtils.lerLong("Informe o ID da configuração MFA a atualizar: ", 1L, null);
            AutenticacaoMultifator mfa = mfaDAO.buscarPorId(idMfa);

            if (mfa == null) {
                ConsoleUtils.imprimirAviso("Nenhuma configuração MFA encontrada com o ID " + idMfa + ".");
                ConsoleUtils.pausar();
                return;
            }

            String novoCodigo = mfa.gerarCodigo();
            boolean sucesso = mfaDAO.atualizar(mfa);

            if (sucesso) {
                ConsoleUtils.imprimirSucesso("Novo código MFA gerado com sucesso: " + novoCodigo);
                ConsoleUtils.imprimirSucesso("Expira em: " + ConsoleUtils.formatarDataHora(mfa.getExpiracao()) + " (Vigência de 5 minutos)");
            } else {
                ConsoleUtils.imprimirErro("Falha ao atualizar código MFA no banco.");
            }
        } catch (SQLException e) {
            tratarErroSql("gerar código MFA", e);
        }

        ConsoleUtils.pausar();
    }

    private static void validarCodigoMfa() {
        ConsoleUtils.imprimirSubcabecalho("VALIDAÇÃO DE TOKEN MFA");

        try {
            Long idMfa = ConsoleUtils.lerLong("Informe o ID da configuração MFA: ", 1L, null);
            AutenticacaoMultifator mfa = mfaDAO.buscarPorId(idMfa);

            if (mfa == null) {
                ConsoleUtils.imprimirAviso("Configuração MFA não encontrada com o ID " + idMfa + ".");
                ConsoleUtils.pausar();
                return;
            }

            String codigoDigitado = ConsoleUtils.lerTexto("Digite o código de 6 dígitos recebido: ", true);

            if (mfa.validarCodigo(codigoDigitado)) {
                ConsoleUtils.imprimirSucesso("AUTENTICAÇÃO MULTIFATOR APROVADA! O código informado é válido.");
            } else {
                ConsoleUtils.imprimirErro("FALHA NA AUTENTICAÇÃO: Código incorreto ou expirado!");
                if (mfa.getExpiracao() != null && new Date().after(mfa.getExpiracao())) {
                    ConsoleUtils.imprimirAviso("O código expirou em " + ConsoleUtils.formatarDataHora(mfa.getExpiracao()) + ".");
                }
            }
        } catch (SQLException e) {
            tratarErroSql("validar código MFA", e);
        }

        ConsoleUtils.pausar();
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            ConsoleUtils.imprimirErro("Não é possível excluir esta credencial pois ela está vinculada a um Usuário no sistema.");
        } else if (e.getErrorCode() == 1) {
            ConsoleUtils.imprimirErro("Já existe uma credencial com este login no banco de dados.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
