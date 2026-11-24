import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Carteiras de Investimento (Fase 7).
 *
 * Gerencia as carteiras de investimento corporativas e individuais vinculadas a empresas,
 * com saldo total consolidado, extrato de composicao e integracao direta com alocacao
 * de ativos e custodia.
 *
 * Operacoes:
 * [1] Inclusao de Carteira (com selecao interativa de Empresa)
 * [2] Alteracao de Carteira
 * [3] Exclusao de Carteira
 * [4] Exibicao de Todas as Carteiras (via ArrayList)
 * [5] Exibicao de Carteira por ID (com composicao de ativos)
 * [6] Consulta de Carteiras por Empresa
 * [7] Recalculo de Saldo Consolidado (atualiza saldo no Oracle com base nos ativos alocados)
 * [8] Submodulo de Alocacao de Ativos (CarteiraAtivoView)
 * [0] Voltar ao Menu Principal
 */
public final class CarteiraView {

    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final EmpresaDAO empresaDAO = new EmpresaDAO();
    private static final CarteiraAtivoDAO carteiraAtivoDAO = new CarteiraAtivoDAO();

    private CarteiraView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE CARTEIRAS DE INVESTIMENTO");
            System.out.println("  [1] Inclusão de Carteira     - Cadastrar nova carteira de investimentos");
            System.out.println("  [2] Alteração de Carteira    - Atualizar nome/empresa da carteira");
            System.out.println("  [3] Exclusão de Carteira     - Remover carteira do banco");
            System.out.println("  [4] Exibição de Todas        - Listar todas as carteiras (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar detalhes e saldo da carteira");
            System.out.println("  [6] Consulta por Empresa     - Filtrar carteiras de uma empresa gestora");
            System.out.println("  [7] Recalcular Saldo Total   - Atualizar saldo a mercado via ativos");
            System.out.println("  [8] Gestão de Custódia       - Submódulo de Alocação de Ativos (Ativos/Carteira)");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 8);

            switch (opcao) {
                case 1 -> incluirCarteira();
                case 2 -> alterarCarteira();
                case 3 -> excluirCarteira();
                case 4 -> exibirTodas();
                case 5 -> exibirPorId();
                case 6 -> buscarPorEmpresa();
                case 7 -> recalcularSaldoTotal();
                case 8 -> CarteiraAtivoView.exibirMenu();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirCarteira() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE CARTEIRA DE INVESTIMENTOS");

        try {
            // Seleciona obrigatoriamente uma Empresa gestora
            Empresa empresa = selecionarEmpresa();
            if (empresa == null) {
                ConsoleUtils.pausar();
                return;
            }

            String nome = ConsoleUtils.lerTexto("Nome da Carteira (ex: Carteira Conservadora Global, FIAP Tech): ", true);
            Double saldoInicial = ConsoleUtils.lerDouble("Saldo / Aporte inicial (R$): ", 0.0, null);

            Carteira novaCarteira = new Carteira(nome, saldoInicial, empresa);

            System.out.println("\nDados para confirmação:");
            System.out.println("  Nome da Carteira : " + novaCarteira.getNome());
            System.out.println("  Empresa Gestora  : " + empresa.getNome() + " (CNPJ: " + empresa.getCnpj() + ")");
            System.out.println("  Saldo Inicial    : " + ConsoleUtils.formatarMoeda(novaCarteira.getSaldoTotal()));

            if (ConsoleUtils.confirmar("Confirma o cadastro desta carteira no Oracle?")) {
                Carteira salva = carteiraDAO.inserir(novaCarteira);
                ConsoleUtils.imprimirSucesso("Carteira cadastrada com sucesso! ID gerado no Oracle: " + salva.getId());
            } else {
                ConsoleUtils.imprimirAviso("Cadastro cancelado.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar carteira", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarCarteira() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE CARTEIRA");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da carteira que deseja alterar: ", 1L, null);
            Carteira carteira = carteiraDAO.buscarPorId(id);

            if (carteira == null) {
                ConsoleUtils.imprimirAviso("Nenhuma carteira encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            Empresa empresaAtual = null;
            if (carteira.getEmpresa() != null && carteira.getEmpresa().getId() != null) {
                empresaAtual = empresaDAO.buscarPorId(carteira.getEmpresa().getId());
            }

            System.out.println("\nDados atuais da Carteira:");
            System.out.println("  ID             : " + carteira.getId());
            System.out.println("  Nome           : " + carteira.getNome());
            System.out.println("  Saldo Atual    : " + ConsoleUtils.formatarMoeda(carteira.getSaldoTotal()));
            System.out.println("  Empresa        : " + (empresaAtual != null ? empresaAtual.getNome() : "ID " + carteira.getEmpresa().getId()));
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + carteira.getNome() + "]: ", carteira.getNome());
            Double novoSaldo = ConsoleUtils.lerDoubleOpcional("Novo Saldo [" + carteira.getSaldoTotal() + "]: ", carteira.getSaldoTotal());

            carteira.setNome(novoNome);
            carteira.setSaldoTotal(novoSaldo);

            if (ConsoleUtils.confirmar("Deseja alterar a Empresa vinculada a esta carteira?")) {
                Empresa novaEmpresa = selecionarEmpresa();
                if (novaEmpresa != null) {
                    carteira.setEmpresa(novaEmpresa);
                }
            }

            if (ConsoleUtils.confirmar("Confirma as atualizações da carteira?")) {
                boolean sucesso = carteiraDAO.atualizar(carteira);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Carteira atualizada com sucesso no banco de dados Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar a carteira.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar carteira", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirCarteira() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE CARTEIRA");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da carteira que deseja excluir: ", 1L, null);
            Carteira carteira = carteiraDAO.buscarPorId(id);

            if (carteira == null) {
                ConsoleUtils.imprimirAviso("Nenhuma carteira encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados da carteira a ser excluída:");
            System.out.println("  ID    : " + carteira.getId());
            System.out.println("  Nome  : " + carteira.getNome());
            System.out.println("  Saldo : " + ConsoleUtils.formatarMoeda(carteira.getSaldoTotal()));

            if (ConsoleUtils.confirmar("ATENÇÃO: Deseja realmente excluir esta carteira do banco?")) {
                boolean sucesso = carteiraDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Carteira removida com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir a carteira.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir carteira", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodas() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODAS AS CARTEIRAS (ArrayList)");

        try {
            ArrayList<Carteira> carteiras = carteiraDAO.listarTodos();

            if (carteiras.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma carteira cadastrada no momento.");
            } else {
                imprimirTabelaCarteiras(carteiras);
                System.out.println("  Total de carteiras cadastradas: " + carteiras.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar carteiras", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE CARTEIRA POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da carteira: ", 1L, null);
            Carteira carteira = carteiraDAO.buscarPorId(id);

            if (carteira == null) {
                ConsoleUtils.imprimirAviso("Nenhuma carteira encontrada com o ID " + id + ".");
            } else {
                imprimirDetalhesCarteira(carteira);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar carteira por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorEmpresa() {
        ConsoleUtils.imprimirSubcabecalho("FILTRO DE CARTEIRAS POR EMPRESA");

        try {
            Long idEmpresa = ConsoleUtils.lerLong("Informe o ID da Empresa: ", 1L, null);
            Empresa emp = empresaDAO.buscarPorId(idEmpresa);

            if (emp == null) {
                ConsoleUtils.imprimirAviso("Nenhuma empresa cadastrada com o ID " + idEmpresa + ".");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<Carteira> lista = carteiraDAO.listarPorEmpresa(idEmpresa);

            System.out.println("\nEmpresa: " + emp.getNome() + " (CNPJ: " + emp.getCnpj() + ")");

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Esta empresa não possui nenhuma carteira vinculada.");
            } else {
                imprimirTabelaCarteiras(lista);
                System.out.println("  Total de carteiras vinculadas à empresa: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar carteiras por empresa", e);
        }

        ConsoleUtils.pausar();
    }

    private static void recalcularSaldoTotal() {
        ConsoleUtils.imprimirSubcabecalho("RECÁLCULO DO SALDO TOTAL A MERCADO");

        try {
            Long idCarteira = ConsoleUtils.lerLong("Informe o ID da carteira para recalcular: ", 1L, null);
            Carteira carteira = carteiraDAO.buscarPorId(idCarteira);

            if (carteira == null) {
                ConsoleUtils.imprimirAviso("Carteira com ID " + idCarteira + " não encontrada.");
                ConsoleUtils.pausar();
                return;
            }

            // Carrega os ativos associados a esta carteira
            ArrayList<CarteiraAtivo> ativosAlocados = carteiraAtivoDAO.listarPorCarteira(idCarteira);
            carteira.setCarteiraAtivos(ativosAlocados);

            double saldoAnterior = carteira.getSaldoTotal();
            // Executa o metodo de dominio da classe Carteira
            double novoSaldo = carteira.calcularSaldo();

            System.out.println("\nCarteira: " + carteira.getNome());
            System.out.println("  Saldo Anterior : " + ConsoleUtils.formatarMoeda(saldoAnterior));
            System.out.println("  Ativos Alocados: " + ativosAlocados.size() + " ativo(s)");
            System.out.println("  Novo Saldo     : " + ConsoleUtils.formatarMoeda(novoSaldo));

            if (ConsoleUtils.confirmar("Deseja salvar o novo saldo consolidado no banco de dados Oracle?")) {
                boolean sucesso = carteiraDAO.atualizar(carteira);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Saldo da carteira atualizado com sucesso no Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível persistir o novo saldo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Atualização de saldo cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("recalcular saldo", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static Empresa selecionarEmpresa() throws SQLException {
        ArrayList<Empresa> empresas = empresaDAO.listarTodos();

        if (empresas.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhuma Empresa cadastrada no banco de dados.");
            ConsoleUtils.imprimirAviso("Cadastre uma empresa primeiro no Módulo de Empresas antes de criar uma carteira!");
            return null;
        }

        System.out.println("\nEmpresas cadastradas no sistema:");
        for (Empresa e : empresas) {
            System.out.printf("  [ID %d] %-35s (CNPJ: %s)\n", e.getId(), e.getNome(), e.getCnpj());
        }

        Long idEmpresa = ConsoleUtils.lerLong("Informe o ID da Empresa gestora: ", 1L, null);
        Empresa escolhida = empresaDAO.buscarPorId(idEmpresa);

        if (escolhida == null) {
            ConsoleUtils.imprimirAviso("Empresa com ID " + idEmpresa + " não localizada.");
            return null;
        }

        return escolhida;
    }

    private static void imprimirDetalhesCarteira(Carteira carteira) throws SQLException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DA CARTEIRA (ID: " + carteira.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID             : " + carteira.getId());
        System.out.println("  Nome           : " + carteira.getNome());
        System.out.println("  Saldo Total    : " + ConsoleUtils.formatarMoeda(carteira.getSaldoTotal()));
        System.out.println("  Data Criação   : " + ConsoleUtils.formatarData(carteira.getDataCriacao()));

        if (carteira.getEmpresa() != null && carteira.getEmpresa().getId() != null) {
            Empresa emp = empresaDAO.buscarPorId(carteira.getEmpresa().getId());
            if (emp != null) {
                System.out.println("  Empresa Gestora: " + emp.getNome() + " (CNPJ: " + emp.getCnpj() + ")");
            } else {
                System.out.println("  Empresa Gestora: ID " + carteira.getEmpresa().getId());
            }
        }

        // Exibe quantidade de ativos alocados
        ArrayList<CarteiraAtivo> ativos = carteiraAtivoDAO.listarPorCarteira(carteira.getId());
        System.out.println("  Ativos Alocados: " + ativos.size() + " posição(ões) sob custódia");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaCarteiras(ArrayList<Carteira> carteiras) throws SQLException {
        System.out.println("+--------+--------------------------------+-----------------+---------------------------+------------+");
        System.out.println("| ID     | NOME DA CARTEIRA               | SALDO TOTAL     | EMPRESA GESTORA           | CRIADA EM  |");
        System.out.println("+--------+--------------------------------+-----------------+---------------------------+------------+");

        for (Carteira c : carteiras) {
            String nome = c.getNome();
            if (nome != null && nome.length() > 30) {
                nome = nome.substring(0, 27) + "...";
            }

            String nomeEmpresa = "-";
            if (c.getEmpresa() != null && c.getEmpresa().getId() != null) {
                Empresa emp = empresaDAO.buscarPorId(c.getEmpresa().getId());
                if (emp != null) {
                    nomeEmpresa = emp.getNome();
                    if (nomeEmpresa.length() > 25) {
                        nomeEmpresa = nomeEmpresa.substring(0, 22) + "...";
                    }
                } else {
                    nomeEmpresa = "ID " + c.getEmpresa().getId();
                }
            }

            System.out.printf("| %-6d | %-30s | %-15s | %-25s | %-10s |\n",
                    c.getId(),
                    nome != null ? nome : "-",
                    ConsoleUtils.formatarMoeda(c.getSaldoTotal()),
                    nomeEmpresa,
                    ConsoleUtils.formatarData(c.getDataCriacao()));
        }

        System.out.println("+--------+--------------------------------+-----------------+---------------------------+------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            ConsoleUtils.imprimirErro("Não é possível excluir esta carteira pois ela possui ativos alocados ou transações registradas.");
            ConsoleUtils.imprimirAviso("Remova as transações e as alocações da carteira antes de excluí-la.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
