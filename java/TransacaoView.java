import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Transacoes Financeiras (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD de Transacoes:
 * [1] Inclusao (Registrar Compra ou Venda em Carteira)
 * [2] Alteracao
 * [3] Exclusao
 * [4] Exibicao de Todas as Transacoes (via ArrayList)
 * [5] Exibicao por ID
 * [6] Extrato Financeiro por Carteira
 * [7] Registro Rapido de Compra/Venda via Ativo
 * [0] Voltar ao menu anterior
 */
public final class TransacaoView {

    private static final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final AtivoDAO ativoDAO = new AtivoDAO();

    private TransacaoView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE TRANSAÇÕES FINANCEIRAS");
            System.out.println("  [1] Nova Transação           - Registrar operação de COMPRA ou VENDA");
            System.out.println("  [2] Alteração de Transação   - Atualizar dados de uma transação");
            System.out.println("  [3] Exclusão de Transação    - Remover registro de transação");
            System.out.println("  [4] Exibição de Todas        - Listar todas as transações (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar comprovante da transação");
            System.out.println("  [6] Extrato por Carteira     - Ver histórico de transações de uma carteira");
            System.out.println("  [7] Registro Rápido por Lote - Operar compra/venda direta por ativo");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirTransacao();
                case 2 -> alterarTransacao();
                case 3 -> excluirTransacao();
                case 4 -> exibirTodas();
                case 5 -> exibirPorId();
                case 6 -> exibirExtratoPorCarteira();
                case 7 -> registrarTransacaoRapida();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirTransacao() {
        ConsoleUtils.imprimirSubcabecalho("NOVA TRANSAÇÃO FINANCEIRA");

        try {
            Carteira carteira = selecionarCarteira();
            if (carteira == null) {
                ConsoleUtils.pausar();
                return;
            }

            Ativo ativo = selecionarAtivoOpcional();

            String tipo = Validador.lerTipoTransacao("Tipo da Operação (COMPRA ou VENDA): ", null);
            Double valor = ConsoleUtils.lerDouble("Valor total da operação (R$): ", 0.01, null);
            String descricao = ConsoleUtils.lerTexto("Descrição / Justificativa da transação: ", false);

            Transacao novaTransacao = new Transacao(tipo, valor, descricao, ativo, carteira);

            System.out.println("\nDados para confirmação:");
            System.out.println("  Carteira     : " + carteira.getNome() + " (ID: " + carteira.getId() + ")");
            System.out.println("  Ativo        : " + (ativo != null ? ativo.getNome() : "Sem ativo vinculado"));
            System.out.println("  Tipo         : " + novaTransacao.getTipo());
            System.out.println("  Valor Total  : " + ConsoleUtils.formatarMoeda(novaTransacao.getValor()));
            System.out.println("  Descrição    : " + (novaTransacao.getDescricao() != null ? novaTransacao.getDescricao() : "-"));

            if (ConsoleUtils.confirmar("Confirma o registro desta transação no Oracle?")) {
                Transacao salva = transacaoDAO.inserir(novaTransacao);
                ConsoleUtils.imprimirSucesso("Transação registrada com sucesso! ID gerado no Oracle: " + salva.getId());
            } else {
                ConsoleUtils.imprimirAviso("Operação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("registrar transação", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarTransacao() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE TRANSAÇÃO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da transação que deseja alterar: ", 1L, null);
            Transacao t = transacaoDAO.buscarPorId(id);

            if (t == null) {
                ConsoleUtils.imprimirAviso("Nenhuma transação encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais da Transação:");
            System.out.println("  ID        : " + t.getId());
            System.out.println("  Tipo      : " + t.getTipo());
            System.out.println("  Valor     : " + ConsoleUtils.formatarMoeda(t.getValor()));
            System.out.println("  Descrição : " + (t.getDescricao() != null ? t.getDescricao() : "-"));
            System.out.println("  Data      : " + ConsoleUtils.formatarDataHora(t.getData()));
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoTipo = Validador.lerTipoTransacao("Novo Tipo (COMPRA/VENDA) [" + t.getTipo() + "]: ", t.getTipo());
            Double novoValor = ConsoleUtils.lerDoubleOpcional("Novo Valor (R$) [" + t.getValor() + "]: ", t.getValor());
            String novaDescricao = ConsoleUtils.lerTextoOpcional("Nova Descrição [" + (t.getDescricao() != null ? t.getDescricao() : "") + "]: ", t.getDescricao());

            t.setTipo(novoTipo);
            t.setValor(novoValor);
            t.setDescricao(novaDescricao);

            if (ConsoleUtils.confirmar("Deseja alterar a Carteira vinculada a esta transação?")) {
                Carteira novaCarteira = selecionarCarteira();
                if (novaCarteira != null) {
                    t.setCarteira(novaCarteira);
                }
            }

            if (ConsoleUtils.confirmar("Confirma as atualizações da transação?")) {
                boolean sucesso = transacaoDAO.atualizar(t);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Transação atualizada com sucesso no banco Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar a transação.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar transação", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirTransacao() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE TRANSAÇÃO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da transação que deseja excluir: ", 1L, null);
            Transacao t = transacaoDAO.buscarPorId(id);

            if (t == null) {
                ConsoleUtils.imprimirAviso("Nenhuma transação encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados da transação a ser excluída:");
            System.out.println("  ID    : " + t.getId());
            System.out.println("  Tipo  : " + t.getTipo());
            System.out.println("  Valor : " + ConsoleUtils.formatarMoeda(t.getValor()));
            System.out.println("  Data  : " + ConsoleUtils.formatarDataHora(t.getData()));

            if (ConsoleUtils.confirmar("Confirma a exclusão definitiva desta transação?")) {
                boolean sucesso = transacaoDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Transação excluída com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir a transação.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir transação", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodas() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODAS AS TRANSAÇÕES (ArrayList)");

        try {
            ArrayList<Transacao> lista = transacaoDAO.listarTodos();

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma transação registrada no sistema.");
            } else {
                imprimirTabelaTransacoes(lista);
                System.out.println("  Total de transações registradas: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar transações", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("COMPROVANTE DE TRANSAÇÃO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da transação: ", 1L, null);
            Transacao t = transacaoDAO.buscarPorId(id);

            if (t == null) {
                ConsoleUtils.imprimirAviso("Nenhuma transação encontrada com o ID " + id + ".");
            } else {
                imprimirDetalhesTransacao(t);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar transação por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirExtratoPorCarteira() {
        ConsoleUtils.imprimirSubcabecalho("EXTRATO FINANCEIRO POR CARTEIRA");

        try {
            Long idCarteira = ConsoleUtils.lerLong("Informe o ID da Carteira: ", 1L, null);
            Carteira carteira = carteiraDAO.buscarPorId(idCarteira);

            if (carteira == null) {
                ConsoleUtils.imprimirAviso("Carteira não encontrada com o ID " + idCarteira + ".");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<Transacao> extrato = transacaoDAO.listarPorCarteira(idCarteira);

            System.out.println("\nExtrato da Carteira: " + carteira.getNome() + " (ID: " + carteira.getId() + ")");
            System.out.println("Saldo Registrado: " + ConsoleUtils.formatarMoeda(carteira.getSaldoTotal()));

            if (extrato.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma transação registrada para esta carteira.");
            } else {
                imprimirTabelaTransacoes(extrato);

                double totalCompras = 0.0;
                double totalVendas = 0.0;

                for (Transacao t : extrato) {
                    if ("COMPRA".equalsIgnoreCase(t.getTipo())) {
                        totalCompras += t.getValor();
                    } else if ("VENDA".equalsIgnoreCase(t.getTipo())) {
                        totalVendas += t.getValor();
                    }
                }

                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("  Total de Compras : " + ConsoleUtils.formatarMoeda(totalCompras));
                System.out.println("  Total de Vendas  : " + ConsoleUtils.formatarMoeda(totalVendas));
                System.out.println("  Fluxo Líquido    : " + ConsoleUtils.formatarMoeda(totalVendas - totalCompras));
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            tratarErroSql("gerar extrato", e);
        }

        ConsoleUtils.pausar();
    }

    private static void registrarTransacaoRapida() {
        ConsoleUtils.imprimirSubcabecalho("REGISTRO RÁPIDO DE OPERAÇÃO POR ATIVO");

        try {
            Carteira carteira = selecionarCarteira();
            if (carteira == null) {
                ConsoleUtils.pausar();
                return;
            }

            Ativo ativo = selecionarAtivoOpcional();
            if (ativo == null) {
                ConsoleUtils.imprimirAviso("Para operação rápida, a seleção de um ativo é necessária.");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nAtivo Selecionado: " + ativo.getNome() + " | Cotação: " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
            System.out.println("  [1] Registrar COMPRA pelo lote do ativo");
            System.out.println("  [2] Registrar VENDA pelo lote do ativo");

            int operacao = ConsoleUtils.lerInt("Escolha a operação: ", 1, 2);

            Transacao t;
            if (operacao == 1) {
                t = new Transacao().registrarCompra(ativo);
            } else {
                t = new Transacao().registrarVenda(ativo);
            }

            t.setCarteira(carteira);

            System.out.println("\nTransação Gerada pelo Modelo de Negócio:");
            System.out.println("  Tipo      : " + t.getTipo());
            System.out.println("  Valor     : " + ConsoleUtils.formatarMoeda(t.getValor()));
            System.out.println("  Descrição : " + t.getDescricao());
            System.out.println("  Carteira  : " + carteira.getNome());

            if (ConsoleUtils.confirmar("Deseja efetivar esta transação no Oracle?")) {
                Transacao salva = transacaoDAO.inserir(t);
                ConsoleUtils.imprimirSucesso("Operação rápida persistida com sucesso! ID: " + salva.getId());
            } else {
                ConsoleUtils.imprimirAviso("Operação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("executar operação rápida", e);
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

        Long id = ConsoleUtils.lerLong("Informe o ID da carteira: ", 1L, null);
        Carteira escolhida = carteiraDAO.buscarPorId(id);
        if (escolhida == null) {
            ConsoleUtils.imprimirAviso("Carteira com ID " + id + " não encontrada.");
            return null;
        }
        return escolhida;
    }

    private static Ativo selecionarAtivoOpcional() throws SQLException {
        ArrayList<Ativo> ativos = ativoDAO.listarTodos();
        if (ativos.isEmpty()) {
            return null;
        }

        System.out.println("\nAtivos disponíveis (opcional):");
        System.out.println("  [ID 0] Sem ativo específico vinculado");
        for (Ativo a : ativos) {
            System.out.printf("  [ID %d] %-35s | Cotação: %s\n",
                    a.getId(), a.getNome(), ConsoleUtils.formatarMoeda(a.getValorAtual()));
        }

        Long id = ConsoleUtils.lerLong("Informe o ID do ativo desejado (0 para nenhum): ", 0L, null);
        if (id == 0L) {
            return null;
        }

        return ativoDAO.buscarPorId(id);
    }

    private static void imprimirDetalhesTransacao(Transacao t) throws SQLException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  COMPROVANTE DE TRANSAÇÃO (ID: " + t.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID Transação : " + t.getId());
        System.out.println("  Tipo         : " + t.getTipo());
        System.out.println("  Valor Total  : " + ConsoleUtils.formatarMoeda(t.getValor()));
        System.out.println("  Data/Hora    : " + ConsoleUtils.formatarDataHora(t.getData()));
        System.out.println("  Descrição    : " + (t.getDescricao() != null ? t.getDescricao() : "-"));

        if (t.getCarteira() != null && t.getCarteira().getId() != null) {
            Carteira c = carteiraDAO.buscarPorId(t.getCarteira().getId());
            System.out.println("  Carteira     : " + (c != null ? c.getNome() + " (ID: " + c.getId() + ")" : "ID " + t.getCarteira().getId()));
        }

        if (t.getAtivo() != null && t.getAtivo().getId() != null) {
            Ativo a = ativoDAO.buscarPorId(t.getAtivo().getId());
            System.out.println("  Ativo        : " + (a != null ? a.getNome() + " (ID: " + a.getId() + ")" : "ID " + t.getAtivo().getId()));
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaTransacoes(ArrayList<Transacao> lista) {
        System.out.println("+--------+--------+--------------+---------------------+-------------------+--------------------------------+");
        System.out.println("| ID     | TIPO   | VALOR        | DATA/HORA           | CARTEIRA          | DESCRIÇÃO                      |");
        System.out.println("+--------+--------+--------------+---------------------+-------------------+--------------------------------+");

        for (Transacao t : lista) {
            String desc = t.getDescricao();
            if (desc != null && desc.length() > 30) {
                desc = desc.substring(0, 27) + "...";
            }
            String cartId = t.getCarteira() != null ? "Carteira #" + t.getCarteira().getId() : "-";

            System.out.printf("| %-6d | %-6s | %-12s | %-19s | %-17s | %-30s |\n",
                    t.getId(),
                    t.getTipo(),
                    ConsoleUtils.formatarMoeda(t.getValor()),
                    ConsoleUtils.formatarDataHora(t.getData()),
                    cartId,
                    desc != null ? desc : "-");
        }

        System.out.println("+--------+--------+--------------+---------------------+-------------------+--------------------------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
    }
}
