import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Perfis de Investidor (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD:
 * [1] Inclusao
 * [2] Alteracao
 * [3] Exclusao
 * [4] Exibicao de todos (via ArrayList)
 * [5] Exibicao de um unico registro (por ID)
 * [6] Busca por Nome de Perfil
 * [7] Avaliacao de Risco do Perfil
 * [0] Voltar ao menu anterior
 */
public final class PerfilInvestidorView {

    private static final PerfilInvestidorDAO perfilDAO = new PerfilInvestidorDAO();

    private PerfilInvestidorView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE PERFIS DE INVESTIDOR");
            System.out.println("  [1] Inclusão                 - Cadastrar novo perfil");
            System.out.println("  [2] Alteração                - Atualizar perfil existente");
            System.out.println("  [3] Exclusão                 - Remover perfil do banco");
            System.out.println("  [4] Exibição de Todos        - Listar todos os perfis (ArrayList)");
            System.out.println("  [5] Exibição de Registro     - Consultar detalhes por ID");
            System.out.println("  [6] Consulta por Nome        - Localizar perfil por nome");
            System.out.println("  [7] Diagnóstico de Risco     - Avaliar e explicar diretriz de risco");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirPerfil();
                case 2 -> alterarPerfil();
                case 3 -> excluirPerfil();
                case 4 -> exibirTodos();
                case 5 -> exibirPorId();
                case 6 -> buscarPorNome();
                case 7 -> diagnosticarRisco();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirPerfil() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE NOVO PERFIL DE INVESTIDOR");

        try {
            String nome = ConsoleUtils.lerTexto("Nome do Perfil (ex: Conservador, Moderado, Agressivo): ", true);

            // Verifica duplicidade de nome
            PerfilInvestidor existente = perfilDAO.buscarPorNome(nome);
            if (existente != null) {
                ConsoleUtils.imprimirAviso("Já existe um perfil com o nome '" + nome + "' (ID: " + existente.getId() + ").");
                ConsoleUtils.pausar();
                return;
            }

            String descricao = ConsoleUtils.lerTexto("Descrição das diretrizes do perfil: ", true);
            String nivelRisco = Validador.lerNivelRisco("Nível de Risco (BAIXO, MEDIO, ALTO): ", null);

            PerfilInvestidor novoPerfil = new PerfilInvestidor(nome, descricao, nivelRisco);

            System.out.println("\nDados para confirmação:");
            System.out.println("  Nome        : " + novoPerfil.getNomePerfil());
            System.out.println("  Descrição   : " + novoPerfil.getDescricao());
            System.out.println("  Nível Risco : " + novoPerfil.getNivelRisco());
            System.out.println("  Avaliação   : " + novoPerfil.avaliarRisco());

            if (ConsoleUtils.confirmar("Confirma o cadastro deste perfil?")) {
                PerfilInvestidor salvo = perfilDAO.inserir(novoPerfil);
                ConsoleUtils.imprimirSucesso("Perfil cadastrado com sucesso! ID gerado no Oracle: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Cadastro cancelado.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar perfil", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarPerfil() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE PERFIL DE INVESTIDOR");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do perfil que deseja alterar: ", 1L, null);
            PerfilInvestidor perfil = perfilDAO.buscarPorId(id);

            if (perfil == null) {
                ConsoleUtils.imprimirAviso("Nenhum perfil encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais do perfil:");
            System.out.println("  ID          : " + perfil.getId());
            System.out.println("  Nome        : " + perfil.getNomePerfil());
            System.out.println("  Descrição   : " + perfil.getDescricao());
            System.out.println("  Nível Risco : " + perfil.getNivelRisco());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + perfil.getNomePerfil() + "]: ", perfil.getNomePerfil());
            String novaDescricao = ConsoleUtils.lerTextoOpcional("Nova Descrição [" + perfil.getDescricao() + "]: ", perfil.getDescricao());
            String novoRisco = Validador.lerNivelRisco("Novo Nível de Risco (BAIXO, MEDIO, ALTO) [" + perfil.getNivelRisco() + "]: ", perfil.getNivelRisco());

            // Verifica duplicidade se alterou o nome
            if (!novoNome.equalsIgnoreCase(perfil.getNomePerfil())) {
                PerfilInvestidor existente = perfilDAO.buscarPorNome(novoNome);
                if (existente != null && !existente.getId().equals(perfil.getId())) {
                    ConsoleUtils.imprimirAviso("O nome '" + novoNome + "' já pertence ao perfil ID " + existente.getId() + ".");
                    ConsoleUtils.pausar();
                    return;
                }
            }

            perfil.setNomePerfil(novoNome);
            perfil.setDescricao(novaDescricao);
            perfil.setNivelRisco(novoRisco);

            if (ConsoleUtils.confirmar("Confirma a atualização do perfil?")) {
                boolean sucesso = perfilDAO.atualizar(perfil);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Perfil de investidor atualizado com sucesso no Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar o perfil.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar perfil", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirPerfil() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE PERFIL DE INVESTIDOR");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do perfil que deseja excluir: ", 1L, null);
            PerfilInvestidor perfil = perfilDAO.buscarPorId(id);

            if (perfil == null) {
                ConsoleUtils.imprimirAviso("Nenhum perfil encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados do perfil a ser excluído:");
            System.out.println("  ID          : " + perfil.getId());
            System.out.println("  Nome        : " + perfil.getNomePerfil());
            System.out.println("  Nível Risco : " + perfil.getNivelRisco());

            if (ConsoleUtils.confirmar("Confirma a exclusão definitiva deste perfil?")) {
                boolean sucesso = perfilDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Perfil removido com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir o perfil.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir perfil", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodos() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODOS OS PERFIS DE INVESTIDOR");

        try {
            ArrayList<PerfilInvestidor> perfis = perfilDAO.listarTodos();

            if (perfis.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum perfil cadastrado no momento.");
            } else {
                imprimirTabelaPerfis(perfis);
                System.out.println("  Total de perfis cadastrados: " + perfis.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar perfis", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE PERFIL POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do perfil: ", 1L, null);
            PerfilInvestidor perfil = perfilDAO.buscarPorId(id);

            if (perfil == null) {
                ConsoleUtils.imprimirAviso("Nenhum perfil encontrado com o ID " + id + ".");
            } else {
                imprimirDetalhesPerfil(perfil);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar perfil por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorNome() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE PERFIL POR NOME");

        try {
            String nome = ConsoleUtils.lerTexto("Informe o nome do perfil: ", true);
            PerfilInvestidor perfil = perfilDAO.buscarPorNome(nome);

            if (perfil == null) {
                ConsoleUtils.imprimirAviso("Nenhum perfil encontrado com o nome '" + nome + "'.");
            } else {
                imprimirDetalhesPerfil(perfil);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar perfil por nome", e);
        }

        ConsoleUtils.pausar();
    }

    private static void diagnosticarRisco() {
        ConsoleUtils.imprimirSubcabecalho("DIAGNÓSTICO E DIRETRIZ DE RISCO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do perfil para diagnosticar o risco: ", 1L, null);
            PerfilInvestidor perfil = perfilDAO.buscarPorId(id);

            if (perfil == null) {
                ConsoleUtils.imprimirAviso("Perfil não encontrado com o ID " + id + ".");
            } else {
                System.out.println("\n  Perfil       : " + perfil.getNomePerfil());
                System.out.println("  Nível Risco  : " + perfil.getNivelRisco());
                System.out.println("  Diagnóstico  : " + perfil.avaliarRisco());
                System.out.println("  Descrição    : " + perfil.getDescricao());
            }
        } catch (SQLException e) {
            tratarErroSql("diagnosticar risco do perfil", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static void imprimirDetalhesPerfil(PerfilInvestidor perfil) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DO PERFIL DE INVESTIDOR (ID: " + perfil.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID          : " + perfil.getId());
        System.out.println("  Nome        : " + perfil.getNomePerfil());
        System.out.println("  Nível Risco : " + perfil.getNivelRisco());
        System.out.println("  Descrição   : " + perfil.getDescricao());
        System.out.println("  Avaliação   : " + perfil.avaliarRisco());
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaPerfis(ArrayList<PerfilInvestidor> perfis) {
        System.out.println("+--------+----------------------+-------------+------------------------------------+");
        System.out.println("| ID     | NOME DO PERFIL       | NÍVEL RISCO | DESCRIÇÃO                          |");
        System.out.println("+--------+----------------------+-------------+------------------------------------+");

        for (PerfilInvestidor p : perfis) {
            String desc = p.getDescricao();
            if (desc != null && desc.length() > 34) {
                desc = desc.substring(0, 31) + "...";
            }
            System.out.printf("| %-6d | %-20s | %-11s | %-34s |\n",
                    p.getId(),
                    p.getNomePerfil() != null ? p.getNomePerfil() : "-",
                    p.getNivelRisco() != null ? p.getNivelRisco() : "-",
                    desc != null ? desc : "-");
        }

        System.out.println("+--------+----------------------+-------------+------------------------------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            // ORA-02292: integrity constraint violated - child record found
            ConsoleUtils.imprimirErro("Não é possível excluir este perfil de investidor pois existem usuários cadastrados associados a ele.");
            ConsoleUtils.imprimirAviso("Altere o perfil dos usuários associados antes de realizar a exclusão.");
        } else if (e.getErrorCode() == 1) {
            // ORA-00001: unique constraint violated
            ConsoleUtils.imprimirErro("Já existe um perfil de investidor cadastrado com este nome no banco de dados.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
