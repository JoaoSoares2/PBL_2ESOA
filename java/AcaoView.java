import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Acoes (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD da especializacao Acao:
 * [1] Inclusao (Nome, Cotacao, Quantidade, Ticker, Bolsa, Variacao)
 * [2] Alteracao
 * [3] Exclusao
 * [4] Exibicao de todas as acoes (via ArrayList)
 * [5] Exibicao por ID
 * [6] Busca por Ticker B3
 * [7] Simulacao de Valor Total com Variacao Diaria
 * [0] Voltar ao menu anterior
 */
public final class AcaoView {

    private static final AcaoDAO acaoDAO = new AcaoDAO();

    private AcaoView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("SUBMÓDULO: GESTÃO DE AÇÕES (RENDA VARIÁVEL)");
            System.out.println("  [1] Inclusão de Ação         - Cadastrar nova ação de empresa");
            System.out.println("  [2] Alteração de Ação        - Atualizar cotação/dados da ação");
            System.out.println("  [3] Exclusão de Ação         - Remover ação do banco");
            System.out.println("  [4] Exibição de Todas        - Listar todas as ações (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar detalhes por ID");
            System.out.println("  [6] Consulta por Ticker      - Localizar ação pelo código (ex: PETR4)");
            System.out.println("  [7] Simulação de Patrimônio  - Calcular valor total com oscilação diária");
            System.out.println("  [0] Voltar ao Menu de Ativos");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirAcao();
                case 2 -> alterarAcao();
                case 3 -> excluirAcao();
                case 4 -> exibirTodas();
                case 5 -> exibirPorId();
                case 6 -> buscarPorTicker();
                case 7 -> simularValorTotal();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirAcao() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE NOVA AÇÃO");

        try {
            String ticker = Validador.lerTicker("Código do Ticker (ex: PETR4, VALE3, ITUB4): ", true, null);

            // Verifica se o ticker ja existe no banco
            Acao existente = acaoDAO.buscarPorTicker(ticker);
            if (existente != null) {
                ConsoleUtils.imprimirAviso("Já existe uma ação cadastrada com o ticker " + ticker + " (ID: " + existente.getId() + " - " + existente.getNome() + ").");
                ConsoleUtils.pausar();
                return;
            }

            String nome = ConsoleUtils.lerTexto("Nome da Empresa / Ativo: ", true);
            Double valorAtual = ConsoleUtils.lerDouble("Cotação atual (R$): ", 0.01, null);
            Double quantidade = ConsoleUtils.lerDouble("Quantidade de ações / lote: ", 0.0, null);
            String bolsa = ConsoleUtils.lerTextoOpcional("Bolsa de Valores [B3]: ", "B3");
            Double variacaoDiaria = ConsoleUtils.lerDouble("Variação diária estimada (%) [ex: 1.5 ou -2.0]: ", -100.0, 1000.0);

            Acao novaAcao = new Acao(nome, valorAtual, quantidade, ticker, bolsa);
            novaAcao.setVariacaoDiaria(variacaoDiaria);

            System.out.println("\nDados da Ação para confirmação:");
            System.out.println("  Ticker       : " + novaAcao.getTicker());
            System.out.println("  Empresa      : " + novaAcao.getNome());
            System.out.println("  Cotação      : " + ConsoleUtils.formatarMoeda(novaAcao.getValorAtual()));
            System.out.println("  Quantidade   : " + novaAcao.getQuantidade());
            System.out.println("  Bolsa        : " + novaAcao.getBolsa());
            System.out.printf("  Variação     : %+.2f%%\n", novaAcao.getVariacaoDiaria());
            System.out.println("  Valor Total  : " + ConsoleUtils.formatarMoeda(novaAcao.calcularValorTotal()));

            if (ConsoleUtils.confirmar("Confirma a gravação desta ação no Oracle?")) {
                Acao salva = acaoDAO.inserir(novaAcao);
                ConsoleUtils.imprimirSucesso("Ação cadastrada com sucesso! ID gerado no Oracle: " + salva.getId());
            } else {
                ConsoleUtils.imprimirAviso("Cadastro cancelado.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar ação", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarAcao() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE DADOS DE AÇÃO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da ação que deseja alterar: ", 1L, null);
            Acao acao = acaoDAO.buscarPorId(id);

            if (acao == null) {
                ConsoleUtils.imprimirAviso("Nenhuma ação encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais da Ação:");
            System.out.println("  ID          : " + acao.getId());
            System.out.println("  Ticker      : " + acao.getTicker());
            System.out.println("  Nome        : " + acao.getNome());
            System.out.println("  Cotação     : " + ConsoleUtils.formatarMoeda(acao.getValorAtual()));
            System.out.println("  Quantidade  : " + acao.getQuantidade());
            System.out.println("  Bolsa       : " + acao.getBolsa());
            System.out.printf("  Variação    : %+.2f%%\n", acao.getVariacaoDiaria());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoTicker = Validador.lerTicker("Novo Ticker [" + acao.getTicker() + "]: ", false, acao.getTicker());
            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + acao.getNome() + "]: ", acao.getNome());
            Double novoValor = ConsoleUtils.lerDoubleOpcional("Nova Cotação [" + acao.getValorAtual() + "]: ", acao.getValorAtual());
            Double novaQtd = ConsoleUtils.lerDoubleOpcional("Nova Quantidade [" + acao.getQuantidade() + "]: ", acao.getQuantidade());
            String novaBolsa = ConsoleUtils.lerTextoOpcional("Nova Bolsa [" + acao.getBolsa() + "]: ", acao.getBolsa());
            Double novaVariacao = ConsoleUtils.lerDoubleOpcional("Nova Variação Diária (%) [" + acao.getVariacaoDiaria() + "]: ", acao.getVariacaoDiaria());

            acao.setTicker(novoTicker);
            acao.setNome(novoNome);
            acao.setValorAtual(novoValor);
            acao.setQuantidade(novaQtd);
            acao.setBolsa(novaBolsa);
            acao.setVariacaoDiaria(novaVariacao);

            if (ConsoleUtils.confirmar("Confirma as atualizações nesta ação?")) {
                boolean sucesso = acaoDAO.atualizar(acao);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Ação atualizada com sucesso no banco Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar os dados da ação.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar ação", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirAcao() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE AÇÃO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da ação que deseja excluir: ", 1L, null);
            Acao acao = acaoDAO.buscarPorId(id);

            if (acao == null) {
                ConsoleUtils.imprimirAviso("Nenhuma ação encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados da ação a ser excluída:");
            System.out.println("  ID     : " + acao.getId());
            System.out.println("  Ticker : " + acao.getTicker());
            System.out.println("  Empresa: " + acao.getNome());

            if (ConsoleUtils.confirmar("ATENÇÃO: Deseja realmente excluir esta ação do banco?")) {
                boolean sucesso = acaoDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Ação removida com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir a ação.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir ação", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodas() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODAS AS AÇÕES (ArrayList)");

        try {
            ArrayList<Acao> acoes = acaoDAO.listarTodos();

            if (acoes.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma ação cadastrada no momento.");
            } else {
                imprimirTabelaAcoes(acoes);
                System.out.println("  Total de ações cadastradas: " + acoes.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar ações", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE AÇÃO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da ação: ", 1L, null);
            Acao acao = acaoDAO.buscarPorId(id);

            if (acao == null) {
                ConsoleUtils.imprimirAviso("Nenhuma ação encontrada com o ID " + id + ".");
            } else {
                imprimirDetalhesAcao(acao);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar ação por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorTicker() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE AÇÃO POR TICKER");

        try {
            String ticker = Validador.lerTicker("Informe o Ticker (ex: PETR4): ", true, null);
            Acao acao = acaoDAO.buscarPorTicker(ticker);

            if (acao == null) {
                ConsoleUtils.imprimirAviso("Nenhuma ação encontrada com o ticker " + ticker + ".");
            } else {
                imprimirDetalhesAcao(acao);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar ação por ticker", e);
        }

        ConsoleUtils.pausar();
    }

    private static void simularValorTotal() {
        ConsoleUtils.imprimirSubcabecalho("SIMULAÇÃO DE VALOR PATRIMONIAL COM OSCILAÇÃO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da ação: ", 1L, null);
            Acao acao = acaoDAO.buscarPorId(id);

            if (acao == null) {
                ConsoleUtils.imprimirAviso("Ação não encontrada.");
                ConsoleUtils.pausar();
                return;
            }

            Double variacaoSimulada = ConsoleUtils.lerDouble("Informe a variação percentual do dia a simular (ex: 3.5 ou -1.8): ", -100.0, 1000.0);
            acao.setVariacaoDiaria(variacaoSimulada);

            System.out.println("\nResultado da Simulação:");
            System.out.println("  Ticker               : " + acao.getTicker());
            System.out.println("  Empresa              : " + acao.getNome());
            System.out.println("  Cotação Base         : " + ConsoleUtils.formatarMoeda(acao.getValorAtual()));
            System.out.println("  Quantidade           : " + acao.getQuantidade());
            System.out.printf("  Oscilação Aplicada   : %+.2f%%\n", acao.getVariacaoDiaria());
            System.out.println("  Patrimônio Projetado : " + ConsoleUtils.formatarMoeda(acao.calcularValorTotal()));
        } catch (SQLException e) {
            tratarErroSql("simular valor da ação", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static void imprimirDetalhesAcao(Acao acao) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DA AÇÃO (ID: " + acao.getId() + " | TICKER: " + acao.getTicker() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID                  : " + acao.getId());
        System.out.println("  Ticker              : " + acao.getTicker());
        System.out.println("  Nome da Empresa     : " + acao.getNome());
        System.out.println("  Bolsa               : " + acao.getBolsa());
        System.out.println("  Cotação Unitária    : " + ConsoleUtils.formatarMoeda(acao.getValorAtual()));
        System.out.println("  Quantidade em Lote  : " + acao.getQuantidade());
        System.out.printf("  Variação Diária     : %+.2f%%\n", acao.getVariacaoDiaria());
        System.out.println("  Valor Total Ajustado: " + ConsoleUtils.formatarMoeda(acao.calcularValorTotal()));
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaAcoes(ArrayList<Acao> acoes) {
        System.out.println("+--------+--------+----------------------------+--------------+------------+------------+-----------------+");
        System.out.println("| ID     | TICKER | NOME                       | COTAÇÃO      | QUANTIDADE | VARIAÇÃO   | VALOR TOTAL     |");
        System.out.println("+--------+--------+----------------------------+--------------+------------+------------+-----------------+");

        for (Acao a : acoes) {
            String nome = a.getNome();
            if (nome != null && nome.length() > 26) {
                nome = nome.substring(0, 23) + "...";
            }

            System.out.printf("| %-6d | %-6s | %-26s | %-12s | %-10.2f | %+9.2f%% | %-15s |\n",
                    a.getId(),
                    a.getTicker() != null ? a.getTicker() : "-",
                    nome != null ? nome : "-",
                    ConsoleUtils.formatarMoeda(a.getValorAtual()),
                    a.getQuantidade() != null ? a.getQuantidade() : 0.0,
                    a.getVariacaoDiaria() != null ? a.getVariacaoDiaria() : 0.0,
                    ConsoleUtils.formatarMoeda(a.calcularValorTotal()));
        }

        System.out.println("+--------+--------+----------------------------+--------------+------------+------------+-----------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            ConsoleUtils.imprimirErro("Não é possível excluir esta ação pois ela está vinculada a carteiras ou transações.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
