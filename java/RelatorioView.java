import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Camada de visualizacao e controle para o Modulo de Relatorios Financeiros (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD de Relatorios,
 * gerando e exibindo relatorios Mensais, Anuais e Consolidados de investidores,
 * testando sobrecargas de metodos e armazenamento de conteudo formatado.
 *
 * Operacoes:
 * [1] Gerar Novo Relatorio (Mensal, Anual ou Consolidado)
 * [2] Alteracao de Relatorio
 * [3] Exclusao de Relatorio
 * [4] Exibicao de Todos os Relatorios (via ArrayList)
 * [5] Exibicao de Relatorio por ID (Visualizar conteudo completo)
 * [6] Consulta de Relatorios por Usuario
 * [7] Demonstracao de Sobrecarga de Metodos de Relatorio
 * [0] Voltar ao menu anterior
 */
public final class RelatorioView {

    private static final RelatorioDAO relatorioDAO = new RelatorioDAO();
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private RelatorioView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE RELATÓRIOS FINANCEIROS");
            System.out.println("  [1] Emitir Novo Relatório    - Gerar relatório financeiro para usuário");
            System.out.println("  [2] Alteração de Relatório   - Atualizar dados de um relatório");
            System.out.println("  [3] Exclusão de Relatório    - Remover relatório do banco");
            System.out.println("  [4] Exibição de Todos        - Listar todos os relatórios emitidos (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Visualizar conteúdo completo de um relatório");
            System.out.println("  [6] Relatórios por Usuário   - Consultar emissões de um investidor");
            System.out.println("  [7] Teste de Sobrecarga      - Executar métodos sobrecarregados de emissão");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirRelatorio();
                case 2 -> alterarRelatorio();
                case 3 -> excluirRelatorio();
                case 4 -> exibirTodos();
                case 5 -> exibirPorId();
                case 6 -> buscarPorUsuario();
                case 7 -> testarSobrecarga();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirRelatorio() {
        ConsoleUtils.imprimirSubcabecalho("EMISSÃO DE NOVO RELATÓRIO FINANCEIRO");

        try {
            Usuario usuario = selecionarUsuario();
            if (usuario == null) {
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nTipos de Relatório Disponíveis:");
            System.out.println("  [1] Relatório Mensal (RelatorioMensal)");
            System.out.println("  [2] Relatório Anual (RelatorioAnual)");
            System.out.println("  [3] Relatório Consolidado Geral");

            int escolhaTipo = ConsoleUtils.lerInt("Escolha o tipo: ", 1, 3);

            Relatorio relatorio;

            if (escolhaTipo == 1) {
                int mes = ConsoleUtils.lerInt("Mês de referência (1 a 12): ", 1, 12);
                int ano = ConsoleUtils.lerInt("Ano de referência (ex: 2026): ", 2000, 2100);
                RelatorioMensal rm = new RelatorioMensal(mes, ano);
                rm.gerarRelatorio();
                rm.setConteudo(rm.getConteudo() + "\nInvestidor: " + usuario.getNome() + " (CPF: " + usuario.getCpf() + ")");
                relatorio = rm;
            } else if (escolhaTipo == 2) {
                int ano = ConsoleUtils.lerInt("Ano fiscal de referência (ex: 2026): ", 2000, 2100);
                RelatorioAnual ra = new RelatorioAnual(ano);
                ra.gerarRelatorio();
                ra.setConteudo(ra.getConteudo() + "\nInvestidor: " + usuario.getNome() + " (CPF: " + usuario.getCpf() + ")");
                relatorio = ra;
            } else {
                relatorio = new Relatorio("CONSOLIDADO");
                relatorio.gerarRelatorio("Extrato Geral");
                relatorio.setConteudo(relatorio.getConteudo() + "\nInvestidor: " + usuario.getNome() + " (CPF: " + usuario.getCpf() + ")");
            }

            relatorio.setUsuario(usuario);

            System.out.println("\nConteúdo gerado para o relatório:");
            System.out.println(relatorio.getConteudo());

            if (ConsoleUtils.confirmar("Deseja gravar este relatório no banco Oracle?")) {
                Relatorio salvo = relatorioDAO.inserir(relatorio);
                ConsoleUtils.imprimirSucesso("Relatório gravado com sucesso! ID gerado no Oracle: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Gravação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("gravar relatório", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarRelatorio() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE RELATÓRIO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do relatório que deseja alterar: ", 1L, null);
            Relatorio r = relatorioDAO.buscarPorId(id);

            if (r == null) {
                ConsoleUtils.imprimirAviso("Nenhum relatório encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais do Relatório:");
            System.out.println("  ID           : " + r.getId());
            System.out.println("  Tipo         : " + r.getTipo());
            System.out.println("  Data Geração : " + ConsoleUtils.formatarDataHora(r.getDataGeracao()));
            System.out.println("  Conteúdo     : " + r.getConteudo());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoTipo = ConsoleUtils.lerTextoOpcional("Novo Tipo [" + r.getTipo() + "]: ", r.getTipo()).toUpperCase();
            String novoConteudo = ConsoleUtils.lerTextoOpcional("Novo Conteúdo: ", r.getConteudo());

            r.setTipo(novoTipo);
            r.setConteudo(novoConteudo);

            if (ConsoleUtils.confirmar("Confirma as atualizações no relatório?")) {
                boolean sucesso = relatorioDAO.atualizar(r);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Relatório atualizado com sucesso no Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar o relatório.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar relatório", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirRelatorio() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE RELATÓRIO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do relatório que deseja excluir: ", 1L, null);
            Relatorio r = relatorioDAO.buscarPorId(id);

            if (r == null) {
                ConsoleUtils.imprimirAviso("Nenhum relatório encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados do relatório a ser excluído:");
            System.out.println("  ID   : " + r.getId());
            System.out.println("  Tipo : " + r.getTipo());
            System.out.println("  Data : " + ConsoleUtils.formatarDataHora(r.getDataGeracao()));

            if (ConsoleUtils.confirmar("Confirma a exclusão definitiva deste relatório?")) {
                boolean sucesso = relatorioDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Relatório excluído com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir o relatório.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir relatório", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodos() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODOS OS RELATÓRIOS (ArrayList)");

        try {
            ArrayList<Relatorio> lista = relatorioDAO.listarTodos();

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum relatório cadastrado no momento.");
            } else {
                imprimirTabelaRelatorios(lista);
                System.out.println("  Total de relatórios arquivados: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar relatórios", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("VISUALIZAÇÃO DE RELATÓRIO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do relatório: ", 1L, null);
            Relatorio r = relatorioDAO.buscarPorId(id);

            if (r == null) {
                ConsoleUtils.imprimirAviso("Nenhum relatório encontrado com o ID " + id + ".");
            } else {
                imprimirDetalhesRelatorio(r);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar relatório por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorUsuario() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE RELATÓRIOS POR USUÁRIO");

        try {
            Long idUsuario = ConsoleUtils.lerLong("Informe o ID do usuário: ", 1L, null);
            Usuario u = usuarioDAO.buscarPorId(idUsuario);

            if (u == null) {
                ConsoleUtils.imprimirAviso("Usuário com ID " + idUsuario + " não encontrado.");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<Relatorio> lista = relatorioDAO.listarPorUsuario(idUsuario);

            System.out.println("\nRelatórios do Usuário: " + u.getNome() + " (CPF: " + u.getCpf() + ")");

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum relatório emitido para este usuário.");
            } else {
                imprimirTabelaRelatorios(lista);
                System.out.println("  Total de relatórios emitidos para este usuário: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar relatórios por usuário", e);
        }

        ConsoleUtils.pausar();
    }

    private static void testarSobrecarga() {
        ConsoleUtils.imprimirSubcabecalho("DEMONSTRAÇÃO DE SOBRECARGA DE MÉTODOS (OVERLOAD)");
        System.out.println("Testando as 3 assinaturas de gerarRelatorio() da classe de modelo Relatorio:\n");

        Relatorio r = new Relatorio("AUDITORIA");

        System.out.println("1. Chamada sem argumentos: r.gerarRelatorio()");
        r.gerarRelatorio();

        System.out.println("\n2. Chamada com filtro: r.gerarRelatorio(\"Ações de Alta Volatilidade\")");
        r.gerarRelatorio("Ações de Alta Volatilidade");

        System.out.println("\n3. Chamada com filtro e data de início: r.gerarRelatorio(\"Fundos Multimercado\", dataInicio)");
        Date dataInicio = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000); // 30 dias atras
        r.gerarRelatorio("Fundos Multimercado", dataInicio);

        ConsoleUtils.imprimirSucesso("Todas as 3 sobrecargas foram executadas com sucesso!");
        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static Usuario selecionarUsuario() throws SQLException {
        ArrayList<Usuario> usuarios = usuarioDAO.listarTodos();
        if (usuarios.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhum usuário cadastrado. Cadastre um usuário primeiro!");
            return null;
        }

        System.out.println("\nUsuários cadastrados no sistema:");
        for (Usuario u : usuarios) {
            System.out.printf("  [ID %d] %-30s | CPF: %s\n", u.getId(), u.getNome(), u.getCpf());
        }

        Long id = ConsoleUtils.lerLong("Informe o ID do usuário: ", 1L, null);
        Usuario escolhido = usuarioDAO.buscarPorId(id);
        if (escolhido == null) {
            ConsoleUtils.imprimirAviso("Usuário com ID " + id + " não encontrado.");
            return null;
        }
        return escolhido;
    }

    private static void imprimirDetalhesRelatorio(Relatorio r) throws SQLException {
        System.out.println("\n================================================================================");
        System.out.println("  RELATÓRIO FINANCEIRO #" + r.getId() + " - TIPO: " + r.getTipo());
        System.out.println("================================================================================");
        System.out.println("  Data de Emissão : " + ConsoleUtils.formatarDataHora(r.getDataGeracao()));

        if (r.getUsuario() != null && r.getUsuario().getId() != null) {
            Usuario u = usuarioDAO.buscarPorId(r.getUsuario().getId());
            if (u != null) {
                System.out.println("  Beneficiário    : " + u.getNome() + " | CPF: " + u.getCpf());
            } else {
                System.out.println("  Beneficiário    : ID " + r.getUsuario().getId());
            }
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  CONTEÚDO DO DOCUMENTO:");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(r.getConteudo() != null ? r.getConteudo() : "[Sem conteúdo]");
        System.out.println("================================================================================");
    }

    private static void imprimirTabelaRelatorios(ArrayList<Relatorio> lista) {
        System.out.println("+--------+---------------+---------------------+-------------------+------------------------------------+");
        System.out.println("| ID     | TIPO          | DATA DE GERAÇÃO     | USUÁRIO           | RESUMO DO CONTEÚDO                 |");
        System.out.println("+--------+---------------+---------------------+-------------------+------------------------------------+");

        for (Relatorio r : lista) {
            String conteudo = r.getConteudo();
            if (conteudo != null) {
                conteudo = conteudo.replace("\n", " ");
                if (conteudo.length() > 34) {
                    conteudo = conteudo.substring(0, 31) + "...";
                }
            } else {
                conteudo = "-";
            }

            String usuDesc = r.getUsuario() != null ? "Usuário #" + r.getUsuario().getId() : "-";

            System.out.printf("| %-6d | %-13s | %-19s | %-17s | %-34s |\n",
                    r.getId(),
                    r.getTipo(),
                    ConsoleUtils.formatarDataHora(r.getDataGeracao()),
                    usuDesc,
                    conteudo);
        }

        System.out.println("+--------+---------------+---------------------+-------------------+------------------------------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
    }
}
