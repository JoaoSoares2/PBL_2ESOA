import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de CarteiraAtivo (Fase 7).
 *
 * Gerencia a tabela associativa N:M entre Carteiras e Ativos de mercado,
 * registrando a quantidade em custodia, preco medio de compra e calculando
 * em tempo real o valor de mercado atual e o lucro/prejuizo acumulado.
 *
 * Operacoes:
 * [1] Inclusao (Alocar ativo em carteira)
 * [2] Alteracao (Atualizar quantidade ou preco medio)
 * [3] Exclusao (Remover ativo da carteira)
 * [4] Exibicao de Todas as Alocacoes (ArrayList)
 * [5] Exibicao por ID
 * [6] Extrato de Composicao por Carteira (Posicao consolidada)
 * [7] Rastrear Ativo em Carteiras
 * [0] Voltar ao Menu de Carteiras
 */
public final class CarteiraAtivoView {

    private static final CarteiraAtivoDAO carteiraAtivoDAO = new CarteiraAtivoDAO();
    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final AtivoDAO ativoDAO = new AtivoDAO();

    private CarteiraAtivoView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("SUBMÓDULO: ALOCAÇÃO DE ATIVOS EM CARTEIRAS (CUSTÓDIA)");
            System.out.println("  [1] Alocar Ativo em Carteira - Adicionar ativo à carteira");
            System.out.println("  [2] Alterar Posição / Preço  - Atualizar quantidade ou preço médio");
            System.out.println("  [3] Desalocar Ativo          - Remover ativo da custódia");
            System.out.println("  [4] Exibição de Todas        - Listar todas as alocações (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar detalhes de uma alocação");
            System.out.println("  [6] Posição por Carteira     - Ver composição detalhada de uma carteira");
            System.out.println("  [7] Rastrear Ativo           - Ver carteiras que possuem determinado ativo");
            System.out.println("  [0] Voltar ao Menu de Carteiras");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirAlocacao();
                case 2 -> alterarAlocacao();
                case 3 -> excluirAlocacao();
                case 4 -> exibirTodas();
                case 5 -> exibirPorId();
                case 6 -> exibirPorCarteira();
                case 7 -> exibirPorAtivo();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirAlocacao() {
        ConsoleUtils.imprimirSubcabecalho("ALOCAÇÃO DE ATIVO EM CARTEIRA");

        try {
            // 1. Seleciona Carteira
            Carteira carteira = selecionarCarteira();
            if (carteira == null) {
                ConsoleUtils.pausar();
                return;
            }

            // 2. Seleciona Ativo
            Ativo ativo = selecionarAtivo();
            if (ativo == null) {
                ConsoleUtils.pausar();
                return;
            }

            Double quantidade = ConsoleUtils.lerDouble("Quantidade a alocar: ", 0.0001, null);
            Double precoMedio = ConsoleUtils.lerDouble("Preço médio de aquisição (R$): ", 0.01, null);

            CarteiraAtivo ca = new CarteiraAtivo(carteira, ativo, quantidade, precoMedio);

            System.out.println("\nResumo da alocação para confirmação:");
            System.out.println("  Carteira     : " + carteira.getNome() + " (ID: " + carteira.getId() + ")");
            System.out.println("  Ativo        : " + ativo.getNome() + " (ID: " + ativo.getId() + ")");
            System.out.println("  Quantidade   : " + ca.getQuantidade());
            System.out.println("  Preço Médio  : " + ConsoleUtils.formatarMoeda(ca.getPrecoMedio()));
            System.out.println("  Cotação Atual: " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
            System.out.println("  Valor Atual  : " + ConsoleUtils.formatarMoeda(ca.calcularValorAtual()));
            System.out.println("  Lucro/Prej.  : " + ConsoleUtils.formatarMoeda(ca.calcularLucro()));

            if (ConsoleUtils.confirmar("Confirma a alocação deste ativo na carteira?")) {
                CarteiraAtivo salvo = carteiraAtivoDAO.inserir(ca);
                ConsoleUtils.imprimirSucesso("Ativo alocado com sucesso! ID de custódia gerado: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Alocação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("alocar ativo em carteira", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarAlocacao() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE POSIÇÃO EM CARTEIRA");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da alocação (Carteira-Ativo): ", 1L, null);
            CarteiraAtivo ca = carteiraAtivoDAO.buscarPorId(id);

            if (ca == null) {
                ConsoleUtils.imprimirAviso("Nenhuma alocação encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais da alocação:");
            System.out.println("  ID Custódia  : " + ca.getId());
            System.out.println("  Carteira     : " + (ca.getCarteira() != null ? ca.getCarteira().getNome() : "ID " + ca.getCarteira().getId()));
            System.out.println("  Ativo        : " + (ca.getAtivo() != null ? ca.getAtivo().getNome() : "ID " + ca.getAtivo().getId()));
            System.out.println("  Quantidade   : " + ca.getQuantidade());
            System.out.println("  Preço Médio  : " + ConsoleUtils.formatarMoeda(ca.getPrecoMedio()));
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            Double novaQtd = ConsoleUtils.lerDoubleOpcional("Nova Quantidade [" + ca.getQuantidade() + "]: ", ca.getQuantidade());
            Double novoPm = ConsoleUtils.lerDoubleOpcional("Novo Preço Médio (R$) [" + ca.getPrecoMedio() + "]: ", ca.getPrecoMedio());

            ca.setQuantidade(novaQtd);
            ca.setPrecoMedio(novoPm);

            if (ConsoleUtils.confirmar("Confirma as alterações na posição?")) {
                boolean sucesso = carteiraAtivoDAO.atualizar(ca);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Posição atualizada com sucesso no banco de dados!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar a posição.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar posição", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirAlocacao() {
        ConsoleUtils.imprimirSubcabecalho("DESALOCAÇÃO DE ATIVO (REMOÇÃO DE CUSTÓDIA)");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da alocação que deseja remover: ", 1L, null);
            CarteiraAtivo ca = carteiraAtivoDAO.buscarPorId(id);

            if (ca == null) {
                ConsoleUtils.imprimirAviso("Nenhuma alocação encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados da alocação a ser removida:");
            System.out.println("  ID Custódia : " + ca.getId());
            System.out.println("  Carteira    : " + (ca.getCarteira() != null ? ca.getCarteira().getNome() : "ID " + ca.getCarteira().getId()));
            System.out.println("  Ativo       : " + (ca.getAtivo() != null ? ca.getAtivo().getNome() : "ID " + ca.getAtivo().getId()));
            System.out.println("  Quantidade  : " + ca.getQuantidade());

            if (ConsoleUtils.confirmar("Confirma a remoção desta custódia?")) {
                boolean sucesso = carteiraAtivoDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Ativo desalocado da carteira com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir a alocação.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Desalocação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("desalocar ativo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodas() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODAS AS ALOCAÇÕES DO SISTEMA (ArrayList)");

        try {
            ArrayList<CarteiraAtivo> lista = carteiraAtivoDAO.listarTodos();

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma posição de ativo em carteira cadastrada.");
            } else {
                imprimirTabelaAlocacoes(lista);
                System.out.println("  Total de posições em custódia: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar alocações", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE ALOCAÇÃO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da alocação: ", 1L, null);
            CarteiraAtivo ca = carteiraAtivoDAO.buscarPorId(id);

            if (ca == null) {
                ConsoleUtils.imprimirAviso("Nenhuma alocação encontrada com o ID " + id + ".");
            } else {
                imprimirDetalhesAlocacao(ca);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar alocação por ID", e);
        }

        ConsoleUtils.pausar();
    }

    public static void exibirPorCarteira() {
        ConsoleUtils.imprimirSubcabecalho("COMPOSIÇÃO E EXTRATO DE CUSTÓDIA POR CARTEIRA");

        try {
            Long idCarteira = ConsoleUtils.lerLong("Informe o ID da carteira: ", 1L, null);
            Carteira carteira = carteiraDAO.buscarPorId(idCarteira);

            if (carteira == null) {
                ConsoleUtils.imprimirAviso("Carteira não encontrada com o ID " + idCarteira + ".");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<CarteiraAtivo> posicoes = carteiraAtivoDAO.listarPorCarteira(idCarteira);

            System.out.println("\nCarteira: " + carteira.getNome() + " (ID: " + carteira.getId() + ")");
            System.out.println("Saldo Contábil Registrado: " + ConsoleUtils.formatarMoeda(carteira.getSaldoTotal()));

            if (posicoes.isEmpty()) {
                ConsoleUtils.imprimirAviso("Esta carteira não possui nenhum ativo alocado em custódia.");
            } else {
                imprimirTabelaAlocacoes(posicoes);

                double totalMercado = 0.0;
                double lucroAcumulado = 0.0;

                for (CarteiraAtivo ca : posicoes) {
                    totalMercado += ca.calcularValorAtual();
                    lucroAcumulado += ca.calcularLucro();
                }

                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("  Patrimônio em Ativos a Mercado : " + ConsoleUtils.formatarMoeda(totalMercado));
                System.out.println("  Lucro/Prejuízo Total Estimado   : " + ConsoleUtils.formatarMoeda(lucroAcumulado));
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            tratarErroSql("listar ativos da carteira", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorAtivo() {
        ConsoleUtils.imprimirSubcabecalho("RASTREAMENTO DE ATIVO EM CARTEIRAS");

        try {
            Long idAtivo = ConsoleUtils.lerLong("Informe o ID do ativo: ", 1L, null);
            Ativo ativo = ativoDAO.buscarPorId(idAtivo);

            if (ativo == null) {
                ConsoleUtils.imprimirAviso("Ativo não encontrado com o ID " + idAtivo + ".");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<CarteiraAtivo> posicoes = carteiraAtivoDAO.listarPorAtivo(idAtivo);

            System.out.println("\nAtivo: " + ativo.getNome() + " | Cotação Atual: " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));

            if (posicoes.isEmpty()) {
                ConsoleUtils.imprimirAviso("Este ativo não está alocado em nenhuma carteira no momento.");
            } else {
                imprimirTabelaAlocacoes(posicoes);
                System.out.println("  Presente em " + posicoes.size() + " alocação(ões).");
            }
        } catch (SQLException e) {
            tratarErroSql("rastrear ativo", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static Carteira selecionarCarteira() throws SQLException {
        ArrayList<Carteira> carteiras = carteiraDAO.listarTodos();
        if (carteiras.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhuma carteira cadastrada. Cadastre uma carteira primeiro!");
            return null;
        }

        System.out.println("\nCarteiras disponíveis:");
        for (Carteira c : carteiras) {
            System.out.printf("  [ID %d] %-30s | Saldo: %s\n",
                    c.getId(), c.getNome(), ConsoleUtils.formatarMoeda(c.getSaldoTotal()));
        }

        Long id = ConsoleUtils.lerLong("Informe o ID da carteira desejada: ", 1L, null);
        Carteira escolhida = carteiraDAO.buscarPorId(id);
        if (escolhida == null) {
            ConsoleUtils.imprimirAviso("Carteira com ID " + id + " não encontrada.");
            return null;
        }
        return escolhida;
    }

    private static Ativo selecionarAtivo() throws SQLException {
        ArrayList<Ativo> ativos = ativoDAO.listarTodos();
        if (ativos.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhum ativo cadastrado. Cadastre ativos primeiro!");
            return null;
        }

        System.out.println("\nAtivos disponíveis no mercado:");
        for (Ativo a : ativos) {
            System.out.printf("  [ID %d] %-35s | Cotação: %s\n",
                    a.getId(), a.getNome(), ConsoleUtils.formatarMoeda(a.getValorAtual()));
        }

        Long id = ConsoleUtils.lerLong("Informe o ID do ativo desejado: ", 1L, null);
        Ativo escolhido = ativoDAO.buscarPorId(id);
        if (escolhido == null) {
            ConsoleUtils.imprimirAviso("Ativo com ID " + id + " não encontrado.");
            return null;
        }
        return escolhido;
    }

    private static void imprimirDetalhesAlocacao(CarteiraAtivo ca) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DA CUSTÓDIA (ID: " + ca.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID Alocação       : " + ca.getId());
        System.out.println("  Carteira          : " + (ca.getCarteira() != null ? ca.getCarteira().getNome() : "ID " + ca.getCarteira().getId()));
        System.out.println("  Ativo             : " + (ca.getAtivo() != null ? ca.getAtivo().getNome() : "ID " + ca.getAtivo().getId()));
        System.out.println("  Quantidade        : " + ca.getQuantidade());
        System.out.println("  Preço Médio       : " + ConsoleUtils.formatarMoeda(ca.getPrecoMedio()));
        System.out.println("  Cotação Mercado   : " + ConsoleUtils.formatarMoeda(ca.getAtivo() != null ? ca.getAtivo().getValorAtual() : 0.0));
        System.out.println("  Valor Atual Total : " + ConsoleUtils.formatarMoeda(ca.calcularValorAtual()));
        System.out.println("  Lucro / Prejuízo  : " + ConsoleUtils.formatarMoeda(ca.calcularLucro()));
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaAlocacoes(ArrayList<CarteiraAtivo> lista) {
        System.out.println("+--------+-----------------------+-----------------------+------------+--------------+--------------+-----------------+");
        System.out.println("| ID     | CARTEIRA              | ATIVO                 | QUANTIDADE | PREÇO MÉDIO  | VALOR ATUAL  | LUCRO / PREJ.   |");
        System.out.println("+--------+-----------------------+-----------------------+------------+--------------+--------------+-----------------+");

        for (CarteiraAtivo ca : lista) {
            String cartNome = ca.getCarteira() != null ? ca.getCarteira().getNome() : "ID " + ca.getCarteira().getId();
            if (cartNome.length() > 21) {
                cartNome = cartNome.substring(0, 18) + "...";
            }
            String ativoNome = ca.getAtivo() != null ? ca.getAtivo().getNome() : "ID " + ca.getAtivo().getId();
            if (ativoNome.length() > 21) {
                ativoNome = ativoNome.substring(0, 18) + "...";
            }

            System.out.printf("| %-6d | %-21s | %-21s | %-10.2f | %-12s | %-12s | %-15s |\n",
                    ca.getId(),
                    cartNome,
                    ativoNome,
                    ca.getQuantidade() != null ? ca.getQuantidade() : 0.0,
                    ConsoleUtils.formatarMoeda(ca.getPrecoMedio()),
                    ConsoleUtils.formatarMoeda(ca.calcularValorAtual()),
                    ConsoleUtils.formatarMoeda(ca.calcularLucro()));
        }

        System.out.println("+--------+-----------------------+-----------------------+------------+--------------+--------------+-----------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
    }
}
