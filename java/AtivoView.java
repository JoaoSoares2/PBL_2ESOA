import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Ativos de Mercado (Fase 7).
 *
 * Centraliza o gerenciamento do mercado financeiro, incluindo ativos de renda fixa,
 * integracao com as especializacoes (Acoes e Fundos de Investimento) e atualizacao
 * dinamica de cotacoes com calculo de patrimonio.
 *
 * Operacoes:
 * [1] Inclusao de Ativo Geral (Renda Fixa / Titulo Publico)
 * [2] Alteracao de Ativo
 * [3] Exclusao de Ativo
 * [4] Exibicao de Todos os Ativos (via ArrayList)
 * [5] Exibicao de Ativo por ID
 * [6] Submodulo de Acoes (AcaoView)
 * [7] Submodulo de Fundos de Investimento (FundoInvestimentoView)
 * [8] Atualizacao de Cotacao de Mercado
 * [0] Voltar ao Menu Principal
 */
public final class AtivoView {

    private static final AtivoDAO ativoDAO = new AtivoDAO();
    private static final AcaoDAO acaoDAO = new AcaoDAO();
    private static final FundoInvestimentoDAO fundoDAO = new FundoInvestimentoDAO();

    private AtivoView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE ATIVOS E MERCADO FINANCEIRO");
            System.out.println("  --- GESTÃO GERAL DE ATIVOS (RENDA FIXA / BASE) ---");
            System.out.println("  [1] Inclusão de Ativo        - Cadastrar ativo genérico / renda fixa");
            System.out.println("  [2] Alteração de Ativo       - Atualizar dados de um ativo");
            System.out.println("  [3] Exclusão de Ativo        - Remover ativo do banco");
            System.out.println("  [4] Exibição de Todos        - Listar todos os ativos do mercado (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar detalhes de um ativo");
            System.out.println("  --- ESPECIALIZAÇÕES DO MERCADO ---");
            System.out.println("  [6] Submódulo de Ações       - Gestão de ações B3 (Tickers, oscilação)");
            System.out.println("  [7] Submódulo de Fundos      - Gestão de fundos de investimento");
            System.out.println("  --- DINÂMICA DE MERCADO ---");
            System.out.println("  [8] Atualizar Cotação        - Simular variação/cotação de mercado");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 8);

            switch (opcao) {
                case 1 -> incluirAtivo();
                case 2 -> alterarAtivo();
                case 3 -> excluirAtivo();
                case 4 -> exibirTodos();
                case 5 -> exibirPorId();
                case 6 -> AcaoView.exibirMenu();
                case 7 -> FundoInvestimentoView.exibirMenu();
                case 8 -> atualizarCotacaoMercado();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirAtivo() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE ATIVO GERAL (RENDA FIXA / TÍTULO)");

        try {
            String nome = ConsoleUtils.lerTexto("Nome do Ativo (ex: Tesouro Selic 2029, CDB Banco Master): ", true);
            Double valorAtual = ConsoleUtils.lerDouble("Cotação / Preço unitário (R$): ", 0.01, null);
            Double quantidade = ConsoleUtils.lerDouble("Quantidade de títulos / cotas: ", 0.0, null);

            Ativo novoAtivo = new Ativo(nome, valorAtual, quantidade);

            System.out.println("\nDados para confirmação:");
            System.out.println("  Nome        : " + novoAtivo.getNome());
            System.out.println("  Valor Atual : " + ConsoleUtils.formatarMoeda(novoAtivo.getValorAtual()));
            System.out.println("  Quantidade  : " + novoAtivo.getQuantidade());
            System.out.println("  Valor Total : " + ConsoleUtils.formatarMoeda(novoAtivo.calcularValorTotal()));

            if (ConsoleUtils.confirmar("Confirma o cadastro deste ativo no Oracle?")) {
                Ativo salvo = ativoDAO.inserir(novoAtivo);
                ConsoleUtils.imprimirSucesso("Ativo cadastrado com sucesso! ID gerado no Oracle: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Cadastro cancelado.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar ativo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarAtivo() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE ATIVO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do ativo que deseja alterar: ", 1L, null);
            Ativo ativo = ativoDAO.buscarPorId(id);

            if (ativo == null) {
                ConsoleUtils.imprimirAviso("Nenhum ativo encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais do Ativo:");
            System.out.println("  ID          : " + ativo.getId());
            System.out.println("  Nome        : " + ativo.getNome());
            System.out.println("  Valor Atual : " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
            System.out.println("  Quantidade  : " + ativo.getQuantidade());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + ativo.getNome() + "]: ", ativo.getNome());
            Double novoValor = ConsoleUtils.lerDoubleOpcional("Nova Cotação [" + ativo.getValorAtual() + "]: ", ativo.getValorAtual());
            Double novaQtd = ConsoleUtils.lerDoubleOpcional("Nova Quantidade [" + ativo.getQuantidade() + "]: ", ativo.getQuantidade());

            ativo.setNome(novoNome);
            ativo.setValorAtual(novoValor);
            ativo.setQuantidade(novaQtd);

            if (ConsoleUtils.confirmar("Confirma as atualizações no ativo?")) {
                boolean sucesso = ativoDAO.atualizar(ativo);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Ativo atualizado com sucesso no banco Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar o ativo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar ativo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirAtivo() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE ATIVO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do ativo que deseja excluir: ", 1L, null);
            Ativo ativo = ativoDAO.buscarPorId(id);

            if (ativo == null) {
                ConsoleUtils.imprimirAviso("Nenhum ativo encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados do ativo a ser excluído:");
            System.out.println("  ID   : " + ativo.getId());
            System.out.println("  Nome : " + ativo.getNome());
            System.out.println("  Valor: " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));

            if (ConsoleUtils.confirmar("ATENÇÃO: Deseja realmente excluir este ativo do banco?")) {
                // Se for Acao ou Fundo, o DAO de cada especializacao deve ser usado ou o cascade do Oracle
                boolean sucesso = false;
                Acao acao = acaoDAO.buscarPorId(id);
                FundoInvestimento fundo = fundoDAO.buscarPorId(id);

                if (acao != null) {
                    sucesso = acaoDAO.excluir(id);
                } else if (fundo != null) {
                    sucesso = fundoDAO.excluir(id);
                } else {
                    sucesso = ativoDAO.excluir(id);
                }

                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Ativo removido com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir o ativo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir ativo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodos() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM CONSOLIDADA DO MERCADO (ArrayList)");

        try {
            ArrayList<Ativo> ativos = ativoDAO.listarTodos();

            if (ativos.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum ativo cadastrado no momento.");
            } else {
                System.out.println("+--------+------------------------------------------+---------------+--------------+------------+-----------------+");
                System.out.println("| ID     | NOME DO ATIVO                            | CATEGORIA     | VALOR ATUAL  | QUANTIDADE | VALOR TOTAL     |");
                System.out.println("+--------+------------------------------------------+---------------+--------------+------------+-----------------+");

                for (Ativo a : ativos) {
                    String nome = a.getNome();
                    if (nome != null && nome.length() > 40) {
                        nome = nome.substring(0, 37) + "...";
                    }

                    // Identifica se e Acao, Fundo ou Renda Fixa
                    String categoria = "RENDA FIXA";
                    Acao acao = acaoDAO.buscarPorId(a.getId());
                    if (acao != null) {
                        categoria = "AÇÃO (" + acao.getTicker() + ")";
                    } else {
                        FundoInvestimento fundo = fundoDAO.buscarPorId(a.getId());
                        if (fundo != null) {
                            categoria = "FUNDO (" + fundo.getTipoFundo() + ")";
                        }
                    }

                    System.out.printf("| %-6d | %-40s | %-13s | %-12s | %-10.2f | %-15s |\n",
                            a.getId(),
                            nome != null ? nome : "-",
                            categoria,
                            ConsoleUtils.formatarMoeda(a.getValorAtual()),
                            a.getQuantidade() != null ? a.getQuantidade() : 0.0,
                            ConsoleUtils.formatarMoeda(a.calcularValorTotal()));
                }

                System.out.println("+--------+------------------------------------------+---------------+--------------+------------+-----------------+");
                System.out.println("  Total de ativos disponíveis no mercado: " + ativos.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar ativos", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE ATIVO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do ativo: ", 1L, null);

            // Verifica primeiro se e especializacao para exibir detalhes completos
            Acao acao = acaoDAO.buscarPorId(id);
            if (acao != null) {
                imprimirDetalhesAtivoEspecializado(acao, "AÇÃO (RENDA VARIÁVEL)");
                ConsoleUtils.pausar();
                return;
            }

            FundoInvestimento fundo = fundoDAO.buscarPorId(id);
            if (fundo != null) {
                imprimirDetalhesAtivoEspecializado(fundo, "FUNDO DE INVESTIMENTO (" + fundo.getTipoFundo() + ")");
                ConsoleUtils.pausar();
                return;
            }

            Ativo ativo = ativoDAO.buscarPorId(id);
            if (ativo == null) {
                ConsoleUtils.imprimirAviso("Nenhum ativo encontrado com o ID " + id + ".");
            } else {
                System.out.println("\n--------------------------------------------------------------------------------");
                System.out.println("  DETALHES DO ATIVO (ID: " + ativo.getId() + ")");
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("  ID                  : " + ativo.getId());
                System.out.println("  Nome                : " + ativo.getNome());
                System.out.println("  Categoria           : Renda Fixa / Título");
                System.out.println("  Cotação / Preço Un. : " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
                System.out.println("  Quantidade          : " + ativo.getQuantidade());
                System.out.println("  Valor Total         : " + ConsoleUtils.formatarMoeda(ativo.calcularValorTotal()));
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            tratarErroSql("buscar ativo por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void atualizarCotacaoMercado() {
        ConsoleUtils.imprimirSubcabecalho("ATUALIZAÇÃO DE COTAÇÃO DE MERCADO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do ativo para atualizar a cotação: ", 1L, null);
            Ativo ativo = ativoDAO.buscarPorId(id);

            if (ativo == null) {
                ConsoleUtils.imprimirAviso("Ativo não encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nAtivo Selecionado: " + ativo.getNome() + " | Cotação Atual: " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
            System.out.println("  [1] Informar novo preço fixo (R$)");
            System.out.println("  [2] Aplicar variação percentual (%)");

            int tipoAjuste = ConsoleUtils.lerInt("Escolha o método de ajuste: ", 1, 2);

            double valorAntigo = ativo.getValorAtual();

            if (tipoAjuste == 1) {
                Double novoPreco = ConsoleUtils.lerDouble("Novo preço unitário (R$): ", 0.01, null);
                ativo.atualizarCotacao(novoPreco);
            } else {
                Double percentual = ConsoleUtils.lerDouble("Variação percentual (ex: 2.5 para +2.5% ou -1.2 para -1.2%): ", -100.0, 1000.0);
                ativo.atualizarCotacao(percentual, true);
            }

            System.out.println("\nResumo da Atualização:");
            System.out.println("  Preço Anterior: " + ConsoleUtils.formatarMoeda(valorAntigo));
            System.out.println("  Novo Preço    : " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
            System.out.println("  Novo Total    : " + ConsoleUtils.formatarMoeda(ativo.calcularValorTotal()));

            if (ConsoleUtils.confirmar("Deseja gravar a nova cotação no Oracle?")) {
                boolean sucesso = ativoDAO.atualizar(ativo);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Cotação de mercado atualizada com sucesso no banco!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível salvar a nova cotação.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Atualização descartada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar cotação", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static void imprimirDetalhesAtivoEspecializado(Ativo ativo, String tipo) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DO ATIVO (ID: " + ativo.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID                  : " + ativo.getId());
        System.out.println("  Nome                : " + ativo.getNome());
        System.out.println("  Categoria           : " + tipo);
        System.out.println("  Cotação Atual       : " + ConsoleUtils.formatarMoeda(ativo.getValorAtual()));
        System.out.println("  Quantidade          : " + ativo.getQuantidade());
        System.out.println("  Patrimônio Avaliado : " + ConsoleUtils.formatarMoeda(ativo.calcularValorTotal()));
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            ConsoleUtils.imprimirErro("Não é possível excluir este ativo pois ele está vinculado a carteiras de investimento ou transações.");
            ConsoleUtils.imprimirAviso("Remova as transações e as alocações da carteira antes de excluir este ativo.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
