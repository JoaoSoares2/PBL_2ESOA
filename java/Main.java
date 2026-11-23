import java.io.Console;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        exibirBannerInicial();

        try {
            // 1. Inicializacao e autenticacao no Oracle
            configurarCredenciais(args);
            testarConexao();

            // 2. Loop principal da aplicacao interativa
            boolean executando = true;

            while (executando) {
                exibirMenuPrincipal();
                int opcao = ConsoleUtils.lerInt("Escolha o módulo desejado: ", 0, 10);

                try {
                    switch (opcao) {
                        case 1 -> UsuarioView.exibirMenu();
                        case 2 -> EmpresaView.exibirMenu();
                        case 3 -> PerfilInvestidorView.exibirMenu();
                        case 4 -> CarteiraView.exibirMenu();
                        case 5 -> AtivoView.exibirMenu();
                        case 6 -> TransacaoView.exibirMenu();
                        case 7 -> RelatorioView.exibirMenu();
                        case 8 -> UsuarioEmpresaView.exibirMenu();
                        case 9 -> diagnosticarConexaoOracle();
                        case 10 -> executarBateriaTestesAutomatizados();
                        case 0 -> {
                            executando = false;
                            exibirMensagemEncerramento();
                        }
                    }
                } catch (Exception e) {
                    ConsoleUtils.imprimirErro("Ocorreu um erro durante a execução da operação: " + e.getMessage());
                    ConsoleUtils.pausar();
                }
            }

        } catch (SQLException e) {
            System.err.println("\n[ERRO CRÍTICO DE CONEXÃO COM O ORACLE]");
            System.err.println("  Mensagem: " + e.getMessage());
            System.err.println("  Código de Erro: ORA-" + e.getErrorCode());
            System.err.println("  SQLState: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n[ERRO CRÍTICO INESPERADO]");
            System.err.println("  Mensagem: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.closeConnection();
            System.out.println("\n[SISTEMA] Conexão com o Oracle finalizada de forma segura. Até logo!");
        }
    }

    // =========================================================================
    // MENUS E ELEMENTOS VISUAIS DE NAVEGACAO
    // =========================================================================

    private static void exibirBannerInicial() {
        System.out.println("================================================================================");
        System.out.println("             FIAP - PBL - FASE 7 - MISSÃO TIO PATINHAS                          ");
        System.out.println("    SISTEMA INTEGRADO DE GESTÃO DE INVESTIMENTOS E MERCADO FINANCEIRO           ");
        System.out.println("================================================================================");
        System.out.println("  Desenvolvido em Java com Integração Completa ao Banco de Dados Oracle FIAP   ");
        System.out.println("  Camada DAO, ConnectionFactory JDBC, Collections (ArrayList) e Validações     ");
        System.out.println("================================================================================");
    }

    private static void exibirMenuPrincipal() {
        ConsoleUtils.imprimirCabecalho("MENU PRINCIPAL - SISTEMA TIO PATINHAS");
        System.out.println("  --- MÓDULOS DE NEGÓCIO DO SISTEMA ---");
        System.out.println("  [1] Módulo Usuários & Investidores     (Usuários, Perfis, Acesso & MFA)");
        System.out.println("  [2] Módulo Empresas Gestoras           (Empresas / Corretoras / Assets)");
        System.out.println("  [3] Módulo Perfis de Investidor        (Conservador, Moderado, Agressivo)");
        System.out.println("  [4] Módulo Carteiras de Investimento   (Carteiras, Saldos, Alocações)");
        System.out.println("  [5] Módulo Ativos & Mercado Financeiro (Ações B3, Fundos, Renda Fixa)");
        System.out.println("  [6] Módulo Transações Financeiras      (Ordens de Compra & Venda, Extratos)");
        System.out.println("  [7] Módulo Relatórios Financeiros      (Mensais, Anuais e Consolidados)");
        System.out.println("  [8] Módulo Vínculos Corporativos       (Governança Usuário - Empresa)");
        System.out.println("  --- UTILITÁRIOS E DIAGNÓSTICO ---");
        System.out.println("  [9] Diagnóstico de Conexão Oracle      (Status, metadados e ping no banco)");
        System.out.println("  [10] Executar Bateria de Testes (Fase 6)(Suíte automatizada de testes CRUD)");
        System.out.println("  [0] Encerrar o Programa");
        ConsoleUtils.imprimirLinhaDivisoria();
    }

    private static void exibirMensagemEncerramento() {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("         ENCERRANDO A APLICAÇÃO - OBRIGADO POR UTILIZAR O SISTEMA!              ");
        System.out.println("================================================================================");
    }

    // =========================================================================
    // CONFIGURACAO DE CONEXAO E DIAGNOSTICO
    // =========================================================================

    private static void configurarCredenciais(String[] args) {
        if (args != null && args.length >= 2) {
            ConnectionFactory.setCredenciais(args[0].trim(), args[1]);
            System.out.println("Credenciais recebidas por argumento de linha de comando.");
            return;
        }

        String usuario = obterConfiguracao("DB_USER");
        String senha = obterConfiguracao("DB_PASSWORD");

        if (!estaVazio(usuario) && !estaVazio(senha)) {
            ConnectionFactory.setCredenciais(usuario.trim(), senha);
            System.out.println("Credenciais do Oracle carregadas da configuracao do ambiente/propriedade.");
            return;
        }

        System.out.println("\nCredenciais do Oracle da FIAP");
        System.out.println("As credenciais sao usadas apenas para esta execucao e nao sao gravadas no codigo.");

        Console console = System.console();
        Scanner scanner = console == null ? new Scanner(System.in) : null;

        if (estaVazio(usuario)) {
            if (console != null) {
                usuario = console.readLine("Informe seu RM/usuario Oracle (ex: rm12345): ");
            } else {
                System.out.print("Informe seu RM/usuario Oracle (ex: rm12345): ");
                usuario = scanner.nextLine();
            }
        }

        if (estaVazio(senha)) {
            if (console != null) {
                char[] senhaDigitada = console.readPassword("Informe sua senha Oracle: ");
                senha = senhaDigitada == null ? null : new String(senhaDigitada);
                if (senhaDigitada != null) {
                    Arrays.fill(senhaDigitada, '\0');
                }
            } else {
                System.out.print("Informe sua senha Oracle: ");
                senha = scanner.nextLine();
            }
        }

        if (estaVazio(usuario) || estaVazio(senha)) {
            throw new IllegalArgumentException("RM/usuario e senha do Oracle sao obrigatorios.");
        }

        ConnectionFactory.setCredenciais(usuario.trim(), senha);
    }

    private static String obterConfiguracao(String chave) {
        String propriedade = System.getProperty(chave);
        if (!estaVazio(propriedade)) {
            return propriedade;
        }
        String ambiente = System.getenv(chave);
        if (!estaVazio(ambiente)) {
            return ambiente;
        }
        return null;
    }

    private static boolean estaVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static void testarConexao() throws SQLException {
        System.out.println("\nConectando ao banco de dados Oracle da FIAP...");
        long inicio = System.currentTimeMillis();
        Connection conn = ConnectionFactory.getConnection();
        long tempo = System.currentTimeMillis() - inicio;
        System.out.println("  Conexao estabelecida com sucesso em " + tempo + "ms! Status: " + (!conn.isClosed() ? "ATIVA" : "FECHADA"));
    }

    private static void diagnosticarConexaoOracle() {
        ConsoleUtils.imprimirSubcabecalho("DIAGNÓSTICO E STATUS DA CONEXÃO ORACLE");

        try {
            long inicio = System.currentTimeMillis();
            Connection conn = ConnectionFactory.getConnection();
            long tempoConexao = System.currentTimeMillis() - inicio;

            System.out.println("  Status da Conexão  : " + (!conn.isClosed() ? "ATIVA (ONLINE)" : "FECHADA"));
            System.out.println("  URL de Conexão     : " + ConnectionFactory.getUrl());
            System.out.println("  Tempo de Resposta  : " + tempoConexao + " ms");
            System.out.println("  Banco de Dados     : " + conn.getMetaData().getDatabaseProductName());
            System.out.println("  Versão do Oracle   : " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("  Driver JDBC        : " + conn.getMetaData().getDriverName() + " (" + conn.getMetaData().getDriverVersion() + ")");

            // Executa consulta de ping para testar comunicacao real
            long inicioQuery = System.currentTimeMillis();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT USER, TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS') AS AGORA FROM DUAL")) {
                if (rs.next()) {
                    long tempoQuery = System.currentTimeMillis() - inicioQuery;
                    System.out.println("  Usuário Conectado  : " + rs.getString("USER"));
                    System.out.println("  Data/Hora do Banco : " + rs.getString("AGORA"));
                    System.out.println("  Latência da Query  : " + tempoQuery + " ms");
                }
            }

            ConsoleUtils.imprimirSucesso("Comunicação com o Oracle validada com sucesso!");
        } catch (SQLException e) {
            ConsoleUtils.imprimirErro("Falha ao comunicar com o Oracle: " + e.getMessage() + " (Código: ORA-" + e.getErrorCode() + ")");
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // SUITE DE TESTES AUTOMATIZADOS (HERDADA DA FASE 6)
    // =========================================================================

    private static void executarBateriaTestesAutomatizados() {
        ConsoleUtils.imprimirSubcabecalho("BATERIA DE TESTES AUTOMATIZADOS CRUD (FASE 6)");
        System.out.println("Executando fluxo completo de INSERT, SELECT, UPDATE e DELETE nas 13 entidades...");

        // Instancias dos 13 DAOs
        AutenticacaoMultifatorDAO mfaDAO = new AutenticacaoMultifatorDAO();
        AutenticacaoDAO autenticacaoDAO = new AutenticacaoDAO();
        PerfilInvestidorDAO perfilDAO = new PerfilInvestidorDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();
        CarteiraDAO carteiraDAO = new CarteiraDAO();
        AtivoDAO ativoDAO = new AtivoDAO();
        AcaoDAO acaoDAO = new AcaoDAO();
        FundoInvestimentoDAO fundoDAO = new FundoInvestimentoDAO();
        TransacaoDAO transacaoDAO = new TransacaoDAO();
        RelatorioDAO relatorioDAO = new RelatorioDAO();
        UsuarioEmpresaDAO usuarioEmpresaDAO = new UsuarioEmpresaDAO();
        CarteiraAtivoDAO carteiraAtivoDAO = new CarteiraAtivoDAO();

        try {
            long sufixo = System.currentTimeMillis();

            // 1. INSERTS
            System.out.println("\n>>> [1/4] INSERTS...");
            AutenticacaoMultifator mfaTeste = new AutenticacaoMultifator();
            mfaTeste.setCodigo(String.format("%06d", (int)(sufixo % 1_000_000L)));
            mfaTeste.setExpiracao(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
            mfaTeste = mfaDAO.inserir(mfaTeste);

            Autenticacao autTeste = new Autenticacao("user.teste." + sufixo + "@fiap.com.br", "senhaSegura123");
            autTeste.setAutenticacaoMultifator(mfaTeste);
            autTeste = autenticacaoDAO.inserir(autTeste);

            PerfilInvestidor perfilTeste = new PerfilInvestidor("Perfil " + (sufixo % 100000), "Moderado Teste", "MEDIO");
            perfilTeste = perfilDAO.inserir(perfilTeste);

            String cpfFormatado = String.format("%03d.%03d.%03d-%02d",
                    (sufixo / 100000000) % 1000, (sufixo / 100000) % 1000, (sufixo / 100) % 1000, sufixo % 100);
            Usuario usuarioTeste = new Usuario("Patinhas Teste " + (sufixo % 1000), "patinhas." + sufixo + "@fiap.com.br", "senha123", cpfFormatado, "(11) 98765-4321");
            usuarioTeste.setAutenticacao(autTeste);
            usuarioTeste.setPerfilInvestidor(perfilTeste);
            usuarioTeste = usuarioDAO.inserir(usuarioTeste);

            String cnpjFormatado = String.format("%02d.%03d.%03d/%04d-%02d",
                    (sufixo / 10000000000L) % 100, (sufixo / 10000000) % 1000, (sufixo / 10000) % 1000, 1, sufixo % 100);
            Empresa empresaTeste = new Empresa("FIAP Asset " + (sufixo % 1000), cnpjFormatado);
            empresaTeste = empresaDAO.inserir(empresaTeste);

            Carteira carteiraTeste = new Carteira("Carteira Global " + (sufixo % 1000), 10000.0, empresaTeste);
            carteiraTeste = carteiraDAO.inserir(carteiraTeste);

            Ativo ativoTeste = new Ativo("Tesouro Selic 2029", 12500.0, 2.0);
            ativoTeste = ativoDAO.inserir(ativoTeste);

            String tickerGerado = "PATI" + ((sufixo % 90) + 10);
            Acao acaoTeste = new Acao("Patinhas Holding S.A.", 42.50, 100.0, tickerGerado, "B3");
            acaoTeste.setVariacaoDiaria(1.85);
            acaoTeste = acaoDAO.inserir(acaoTeste);

            FundoInvestimento fundoTeste = new FundoInvestimento("Fundo Multimercado", 250.0, 40.0, 1.75, "MULTIMERCADO");
            fundoTeste = fundoDAO.inserir(fundoTeste);

            Transacao transacaoTeste = new Transacao("COMPRA", 4250.0, "Compra lote inicial", acaoTeste, carteiraTeste);
            transacaoTeste = transacaoDAO.inserir(transacaoTeste);

            Relatorio relatorioTeste = new Relatorio("MENSAL", "Relatorio consolidado de teste", usuarioTeste);
            relatorioTeste = relatorioDAO.inserir(relatorioTeste);

            UsuarioEmpresa usuarioEmpresaTeste = new UsuarioEmpresa(usuarioTeste, empresaTeste, "Gestor de Carteiras");
            usuarioEmpresaTeste = usuarioEmpresaDAO.inserir(usuarioEmpresaTeste);

            CarteiraAtivo carteiraAtivoTeste = new CarteiraAtivo(carteiraTeste, acaoTeste, 100.0, 41.20);
            carteiraAtivoTeste = carteiraAtivoDAO.inserir(carteiraAtivoTeste);

            ConsoleUtils.imprimirSucesso("Todos os 13 INSERTs concluídos com êxito!");

            // 2. SELECTS
            System.out.println("\n>>> [2/4] SELECTS...");
            mfaDAO.buscarPorId(mfaTeste.getId());
            autenticacaoDAO.buscarPorId(autTeste.getId());
            perfilDAO.buscarPorId(perfilTeste.getId());
            usuarioDAO.buscarPorId(usuarioTeste.getId());
            empresaDAO.buscarPorId(empresaTeste.getId());
            carteiraDAO.buscarPorId(carteiraTeste.getId());
            ativoDAO.buscarPorId(ativoTeste.getId());
            acaoDAO.buscarPorId(acaoTeste.getId());
            fundoDAO.buscarPorId(fundoTeste.getId());
            transacaoDAO.buscarPorId(transacaoTeste.getId());
            relatorioDAO.buscarPorId(relatorioTeste.getId());
            usuarioEmpresaDAO.buscarPorId(usuarioEmpresaTeste.getId());
            carteiraAtivoDAO.buscarPorId(carteiraAtivoTeste.getId());
            ConsoleUtils.imprimirSucesso("Todos os 13 SELECTs por ID validados com sucesso!");

            // 3. UPDATES
            System.out.println("\n>>> [3/4] UPDATES...");
            mfaTeste.setCodigo("999888");
            mfaDAO.atualizar(mfaTeste);

            autTeste.setSenha("senhaAtualizada");
            autenticacaoDAO.atualizar(autTeste);

            perfilTeste.setNomePerfil("Perfil Atualizado " + (sufixo % 1000));
            perfilDAO.atualizar(perfilTeste);

            usuarioTeste.setNome("Nome Atualizado");
            usuarioDAO.atualizar(usuarioTeste);

            empresaTeste.setNome("Empresa Atualizada");
            empresaDAO.atualizar(empresaTeste);

            carteiraTeste.setNome("Carteira Atualizada");
            carteiraDAO.atualizar(carteiraTeste);

            ativoTeste.setValorAtual(13000.0);
            ativoDAO.atualizar(ativoTeste);

            acaoTeste.setVariacaoDiaria(3.20);
            acaoDAO.atualizar(acaoTeste);

            fundoTeste.setTaxaAdministracao(2.0);
            fundoDAO.atualizar(fundoTeste);

            transacaoTeste.setDescricao("Descricao Atualizada");
            transacaoDAO.atualizar(transacaoTeste);

            relatorioTeste.setConteudo("Conteudo Atualizado");
            relatorioDAO.atualizar(relatorioTeste);

            usuarioEmpresaTeste.setCargo("Diretor de Riscos");
            usuarioEmpresaDAO.atualizar(usuarioEmpresaTeste);

            carteiraAtivoTeste.setQuantidade(150.0);
            carteiraAtivoDAO.atualizar(carteiraAtivoTeste);
            ConsoleUtils.imprimirSucesso("Todos os 13 UPDATEs persistidos com sucesso!");

            // 4. DELETES
            System.out.println("\n>>> [4/4] DELETES (Ordem inversa de dependência de Foreign Keys)...");
            carteiraAtivoDAO.excluir(carteiraAtivoTeste.getId());
            usuarioEmpresaDAO.excluir(usuarioEmpresaTeste.getId());
            relatorioDAO.excluir(relatorioTeste.getId());
            transacaoDAO.excluir(transacaoTeste.getId());
            fundoDAO.excluir(fundoTeste.getId());
            acaoDAO.excluir(acaoTeste.getId());
            ativoDAO.excluir(ativoTeste.getId());
            carteiraDAO.excluir(carteiraTeste.getId());
            empresaDAO.excluir(empresaTeste.getId());
            usuarioDAO.excluir(usuarioTeste.getId());
            perfilDAO.excluir(perfilTeste.getId());
            autenticacaoDAO.excluir(autTeste.getId());
            mfaDAO.excluir(mfaTeste.getId());
            ConsoleUtils.imprimirSucesso("Todos os 13 DELETEs validados sem violar chaves estrangeiras!");

            System.out.println("\n================================================================================");
            ConsoleUtils.imprimirSucesso("BATERIA DE TESTES AUTOMATIZADA CONCLUÍDA COM 100% DE SUCESSO!");
            System.out.println("================================================================================");
        } catch (SQLException e) {
            ConsoleUtils.imprimirErro("Erro na bateria de testes: " + e.getMessage() + " (Código: ORA-" + e.getErrorCode() + ")");
        }

        ConsoleUtils.pausar();
    }
}
