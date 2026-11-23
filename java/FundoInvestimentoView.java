import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Fundos de Investimento (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD da especializacao FundoInvestimento:
 * [1] Inclusao (Nome, Valor Cota, Quantidade Cotas, Taxa Administracao, Tipo de Fundo)
 * [2] Alteracao
 * [3] Exclusao
 * [4] Exibicao de todos os fundos (via ArrayList)
 * [5] Exibicao por ID
 * [6] Simulacao de Valor Liquido apos Taxa de Administracao
 * [0] Voltar ao menu de ativos
 */
public final class FundoInvestimentoView {

    private static final FundoInvestimentoDAO fundoDAO = new FundoInvestimentoDAO();

    private FundoInvestimentoView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("SUBMÓDULO: GESTÃO DE FUNDOS DE INVESTIMENTO");
            System.out.println("  [1] Inclusão de Fundo        - Cadastrar novo fundo");
            System.out.println("  [2] Alteração de Fundo       - Atualizar dados/taxa do fundo");
            System.out.println("  [3] Exclusão de Fundo        - Remover fundo do banco");
            System.out.println("  [4] Exibição de Todos        - Listar todos os fundos (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar detalhes por ID");
            System.out.println("  [6] Simulação de Rendimento  - Calcular valor líquido pós-taxa adm.");
            System.out.println("  [0] Voltar ao Menu de Ativos");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 6);

            switch (opcao) {
                case 1 -> incluirFundo();
                case 2 -> alterarFundo();
                case 3 -> excluirFundo();
                case 4 -> exibirTodos();
                case 5 -> exibirPorId();
                case 6 -> simularRendimento();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirFundo() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE FUNDO DE INVESTIMENTO");

        try {
            String nome = ConsoleUtils.lerTexto("Nome do Fundo de Investimento: ", true);
            Double valorCota = ConsoleUtils.lerDouble("Valor da cota atual (R$): ", 0.01, null);
            Double cotas = ConsoleUtils.lerDouble("Quantidade de cotas: ", 0.0, null);
            Double taxaAdm = ConsoleUtils.lerDouble("Taxa de Administração (% ao ano, ex: 1.75): ", 0.0, 100.0);
            String tipoFundo = ConsoleUtils.lerTexto("Tipo do Fundo (ex: MULTIMERCADO, RENDA_FIXA, ACOES): ", true).toUpperCase();

            FundoInvestimento novoFundo = new FundoInvestimento(nome, valorCota, cotas, taxaAdm, tipoFundo);

            System.out.println("\nDados do Fundo para confirmação:");
            System.out.println("  Nome         : " + novoFundo.getNome());
            System.out.println("  Tipo         : " + novoFundo.getTipoFundo());
            System.out.println("  Valor Cota   : " + ConsoleUtils.formatarMoeda(novoFundo.getValorAtual()));
            System.out.println("  Qtd. Cotas   : " + novoFundo.getQuantidade());
            System.out.printf("  Taxa Adm.    : %.2f%% a.a.\n", novoFundo.getTaxaAdministracao());
            System.out.println("  Patrimônio   : " + ConsoleUtils.formatarMoeda(novoFundo.calcularValorTotal()));

            if (ConsoleUtils.confirmar("Confirma a gravação deste fundo no Oracle?")) {
                FundoInvestimento salvo = fundoDAO.inserir(novoFundo);
                ConsoleUtils.imprimirSucesso("Fundo cadastrado com sucesso! ID gerado no Oracle: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Cadastro cancelado.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar fundo de investimento", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarFundo() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE FUNDO DE INVESTIMENTO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do fundo que deseja alterar: ", 1L, null);
            FundoInvestimento fundo = fundoDAO.buscarPorId(id);

            if (fundo == null) {
                ConsoleUtils.imprimirAviso("Nenhum fundo encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais do Fundo:");
            System.out.println("  ID          : " + fundo.getId());
            System.out.println("  Nome        : " + fundo.getNome());
            System.out.println("  Tipo        : " + fundo.getTipoFundo());
            System.out.println("  Valor Cota  : " + ConsoleUtils.formatarMoeda(fundo.getValorAtual()));
            System.out.println("  Qtd. Cotas  : " + fundo.getQuantidade());
            System.out.printf("  Taxa Adm.   : %.2f%% a.a.\n", fundo.getTaxaAdministracao());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + fundo.getNome() + "]: ", fundo.getNome());
            String novoTipo = ConsoleUtils.lerTextoOpcional("Novo Tipo [" + fundo.getTipoFundo() + "]: ", fundo.getTipoFundo()).toUpperCase();
            Double novoValor = ConsoleUtils.lerDoubleOpcional("Novo Valor da Cota [" + fundo.getValorAtual() + "]: ", fundo.getValorAtual());
            Double novasCotas = ConsoleUtils.lerDoubleOpcional("Nova Qtd. Cotas [" + fundo.getQuantidade() + "]: ", fundo.getQuantidade());
            Double novaTaxa = ConsoleUtils.lerDoubleOpcional("Nova Taxa Adm. (%) [" + fundo.getTaxaAdministracao() + "]: ", fundo.getTaxaAdministracao());

            fundo.setNome(novoNome);
            fundo.setTipoFundo(novoTipo);
            fundo.setValorAtual(novoValor);
            fundo.setQuantidade(novasCotas);
            fundo.setTaxaAdministracao(novaTaxa);

            if (ConsoleUtils.confirmar("Confirma as atualizações neste fundo?")) {
                boolean sucesso = fundoDAO.atualizar(fundo);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Fundo de investimento atualizado com sucesso no Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar os dados do fundo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar fundo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirFundo() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE FUNDO DE INVESTIMENTO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do fundo que deseja excluir: ", 1L, null);
            FundoInvestimento fundo = fundoDAO.buscarPorId(id);

            if (fundo == null) {
                ConsoleUtils.imprimirAviso("Nenhum fundo encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados do fundo a ser excluído:");
            System.out.println("  ID   : " + fundo.getId());
            System.out.println("  Nome : " + fundo.getNome());
            System.out.println("  Tipo : " + fundo.getTipoFundo());

            if (ConsoleUtils.confirmar("ATENÇÃO: Deseja realmente excluir este fundo do banco?")) {
                boolean sucesso = fundoDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Fundo de investimento removido com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir o fundo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir fundo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodos() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODOS OS FUNDOS DE INVESTIMENTO (ArrayList)");

        try {
            ArrayList<FundoInvestimento> fundos = fundoDAO.listarTodos();

            if (fundos.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum fundo cadastrado no momento.");
            } else {
                imprimirTabelaFundos(fundos);
                System.out.println("  Total de fundos cadastrados: " + fundos.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar fundos", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE FUNDO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do fundo: ", 1L, null);
            FundoInvestimento fundo = fundoDAO.buscarPorId(id);

            if (fundo == null) {
                ConsoleUtils.imprimirAviso("Nenhum fundo encontrado com o ID " + id + ".");
            } else {
                imprimirDetalhesFundo(fundo);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar fundo por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void simularRendimento() {
        ConsoleUtils.imprimirSubcabecalho("SIMULAÇÃO DE RENDIMENTO E TAXA DE ADMINISTRAÇÃO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do fundo para simulação: ", 1L, null);
            FundoInvestimento fundo = fundoDAO.buscarPorId(id);

            if (fundo == null) {
                ConsoleUtils.imprimirAviso("Fundo não encontrado.");
                ConsoleUtils.pausar();
                return;
            }

            double valorBruto = fundo.getValorAtual() * fundo.getQuantidade();
            double valorLiquido = fundo.calcularValorTotal();
            double taxaMensal = fundo.getTaxaAdministracao() / 12;

            System.out.println("\nDiagnóstico de Custo e Retorno Líquido:");
            System.out.println("  Fundo                : " + fundo.getNome() + " (" + fundo.getTipoFundo() + ")");
            System.out.println("  Patrimônio Bruto     : " + ConsoleUtils.formatarMoeda(valorBruto));
            System.out.printf("  Taxa Anual           : %.2f%% a.a.\n", fundo.getTaxaAdministracao());
            System.out.printf("  Desconto Mensal Est. : %.4f%% ao mês\n", taxaMensal);
            System.out.println("  Patrimônio Líquido   : " + ConsoleUtils.formatarMoeda(valorLiquido));
        } catch (SQLException e) {
            tratarErroSql("simular rendimento", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static void imprimirDetalhesFundo(FundoInvestimento fundo) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DO FUNDO (ID: " + fundo.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID                  : " + fundo.getId());
        System.out.println("  Nome do Fundo       : " + fundo.getNome());
        System.out.println("  Classificação/Tipo  : " + fundo.getTipoFundo());
        System.out.println("  Cotação Unitária    : " + ConsoleUtils.formatarMoeda(fundo.getValorAtual()));
        System.out.println("  Cotas em Carteira   : " + fundo.getQuantidade());
        System.out.printf("  Taxa Administração  : %.2f%% a.a.\n", fundo.getTaxaAdministracao());
        System.out.println("  Valor Total Líquido : " + ConsoleUtils.formatarMoeda(fundo.calcularValorTotal()));
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaFundos(ArrayList<FundoInvestimento> fundos) {
        System.out.println("+--------+------------------------------+---------------+--------------+------------+------------+-----------------+");
        System.out.println("| ID     | NOME                         | TIPO          | VALOR COTA   | COTAS      | TAXA ADM.  | PATRIMÔNIO LÍQ. |");
        System.out.println("+--------+------------------------------+---------------+--------------+------------+------------+-----------------+");

        for (FundoInvestimento f : fundos) {
            String nome = f.getNome();
            if (nome != null && nome.length() > 28) {
                nome = nome.substring(0, 25) + "...";
            }

            System.out.printf("| %-6d | %-28s | %-13s | %-12s | %-10.2f | %-9.2f%% | %-15s |\n",
                    f.getId(),
                    nome != null ? nome : "-",
                    f.getTipoFundo() != null ? f.getTipoFundo() : "-",
                    ConsoleUtils.formatarMoeda(f.getValorAtual()),
                    f.getQuantidade() != null ? f.getQuantidade() : 0.0,
                    f.getTaxaAdministracao() != null ? f.getTaxaAdministracao() : 0.0,
                    ConsoleUtils.formatarMoeda(f.calcularValorTotal()));
        }

        System.out.println("+--------+------------------------------+---------------+--------------+------------+------------+-----------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            ConsoleUtils.imprimirErro("Não é possível excluir este fundo pois ele está vinculado a carteiras ou transações.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
