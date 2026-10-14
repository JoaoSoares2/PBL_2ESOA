import java.io.Console;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

/**
 * Classe principal da Fase 6 - Missao Tio Patinhas.
 *
 * Demonstra a integracao completa entre o Banco Oracle da FIAP, a camada de
 * conexao ConnectionFactory, a camada DAO de todas as 13 entidades do sistema,
 * com operacoes CRUD completas (INSERT, SELECT, UPDATE, DELETE),
 * conversao de ResultSet em objetos Java, uso de ArrayList e
 * respeito a integridade referencial (chaves estrangeiras).
 *
 * Fluxo de persistencia:
 * Main -> DAO -> ConnectionFactory -> Banco de Dados Oracle
 *
 * Fluxo de recuperacao:
 * Banco de Dados Oracle -> DAO -> Objetos Java -> ArrayList -> Main
 */
public class Main {

    // Instancias dos 13 DAOs
    private static final AutenticacaoMultifatorDAO mfaDAO = new AutenticacaoMultifatorDAO();
    private static final AutenticacaoDAO autenticacaoDAO = new AutenticacaoDAO();
    private static final PerfilInvestidorDAO perfilDAO = new PerfilInvestidorDAO();
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final EmpresaDAO empresaDAO = new EmpresaDAO();
    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final AtivoDAO ativoDAO = new AtivoDAO();
    private static final AcaoDAO acaoDAO = new AcaoDAO();
    private static final FundoInvestimentoDAO fundoDAO = new FundoInvestimentoDAO();
    private static final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private static final RelatorioDAO relatorioDAO = new RelatorioDAO();
    private static final UsuarioEmpresaDAO usuarioEmpresaDAO = new UsuarioEmpresaDAO();
    private static final CarteiraAtivoDAO carteiraAtivoDAO = new CarteiraAtivoDAO();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     FIAP - PBL - FASE 6 - MISSAO TIO PATINHAS - INTEGRACAO BACK-END COMPLETA   ");
        System.out.println("================================================================================");

        // Referencias para armazenar as entidades criadas e testadas
        AutenticacaoMultifator mfaTeste = null;
        Autenticacao autTeste = null;
        PerfilInvestidor perfilTeste = null;
        Usuario usuarioTeste = null;
        Empresa empresaTeste = null;
        Carteira carteiraTeste = null;
        Ativo ativoTeste = null;
        Acao acaoTeste = null;
        FundoInvestimento fundoTeste = null;
        Transacao transacaoTeste = null;
        Relatorio relatorioTeste = null;
        UsuarioEmpresa usuarioEmpresaTeste = null;
        CarteiraAtivo carteiraAtivoTeste = null;

        try {
            // 1. Configuracao e Conexao
            configurarCredenciais(args);
            testarConexao();

            long sufixo = System.currentTimeMillis();

            // -------------------------------------------------------------------------
            // 2. OPERACOES DE INSERT (Respeitando a ordem das Chaves Estrangeiras)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [1/4] INICIANDO TESTES DE INSERT (Gravacao de registros no Oracle)...");

            // 2.1 AutenticacaoMultifator (sem FK)
            mfaTeste = new AutenticacaoMultifator();
            mfaTeste.setCodigo(String.format("%06d", (int)(sufixo % 1_000_000L)));
            mfaTeste.setExpiracao(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
            mfaTeste = mfaDAO.inserir(mfaTeste);
            System.out.println("  [INSERT 1/13] AutenticacaoMultifator cadastrada - ID: " + mfaTeste.getId());

            // 2.2 Autenticacao (FK -> AutenticacaoMultifator)
            autTeste = new Autenticacao("user.fase6." + sufixo + "@fiap.com.br", "senhaSegura123");
            autTeste.setAutenticacaoMultifator(mfaTeste);
            autTeste = autenticacaoDAO.inserir(autTeste);
            System.out.println("  [INSERT 2/13] Autenticacao cadastrada - ID: " + autTeste.getId() + " | Login: " + autTeste.getLogin());

            // 2.3 PerfilInvestidor (sem FK)
            perfilTeste = new PerfilInvestidor("Perfil " + (sufixo % 100000), "Investimentos moderados e diversificados", "MEDIO");
            perfilTeste = perfilDAO.inserir(perfilTeste);
            System.out.println("  [INSERT 3/13] PerfilInvestidor cadastrado - ID: " + perfilTeste.getId() + " | Perfil: " + perfilTeste.getNomePerfil());

            // 2.4 Usuario (FK -> Autenticacao, PerfilInvestidor)
            String cpfFormatado = String.format("%03d.%03d.%03d-%02d",
                    (sufixo / 100000000) % 1000,
                    (sufixo / 100000) % 1000,
                    (sufixo / 100) % 1000,
                    sufixo % 100);
            usuarioTeste = new Usuario(
                    "Investidor Patinhas " + (sufixo % 1000),
                    "patinhas." + sufixo + "@fiap.com.br",
                    "senhaFase6",
                    cpfFormatado,
                    "(11) 98765-4321"
            );
            usuarioTeste.setAutenticacao(autTeste);
            usuarioTeste.setPerfilInvestidor(perfilTeste);
            usuarioTeste = usuarioDAO.inserir(usuarioTeste);
            System.out.println("  [INSERT 4/13] Usuario cadastrado - ID: " + usuarioTeste.getId() + " | Nome: " + usuarioTeste.getNome());

            // 2.5 Empresa (sem FK)
            String cnpjFormatado = String.format("%02d.%03d.%03d/%04d-%02d",
                    (sufixo / 10000000000L) % 100,
                    (sufixo / 10000000) % 1000,
                    (sufixo / 10000) % 1000,
                    1,
                    sufixo % 100);
            empresaTeste = new Empresa("FIAP Asset Management " + (sufixo % 1000), cnpjFormatado);
            empresaTeste = empresaDAO.inserir(empresaTeste);
            System.out.println("  [INSERT 5/13] Empresa cadastrada - ID: " + empresaTeste.getId() + " | Nome: " + empresaTeste.getNome());

            // 2.6 Carteira (FK -> Empresa)
            carteiraTeste = new Carteira("Carteira Global " + (sufixo % 1000), 10000.0, empresaTeste);
            carteiraTeste = carteiraDAO.inserir(carteiraTeste);
            System.out.println("  [INSERT 6/13] Carteira cadastrada - ID: " + carteiraTeste.getId() + " | Nome: " + carteiraTeste.getNome());

            // 2.7 Ativo (tabela base, sem FK)
            ativoTeste = new Ativo("Tesouro Selic 2029", 12500.0, 2.0);
            ativoTeste = ativoDAO.inserir(ativoTeste);
            System.out.println("  [INSERT 7/13] Ativo base cadastrado - ID: " + ativoTeste.getId() + " | Nome: " + ativoTeste.getNome());

            // 2.8 Acao (subclass de Ativo)
            String tickerGerado = "PATI" + ((sufixo % 90) + 10);
            acaoTeste = new Acao("Patinhas Holding S.A.", 42.50, 100.0, tickerGerado, "B3");
            acaoTeste.setVariacaoDiaria(1.85);
            acaoTeste = acaoDAO.inserir(acaoTeste);
            System.out.println("  [INSERT 8/13] Acao (especializacao de Ativo) cadastrada - ID: " + acaoTeste.getId() + " | Ticker: " + acaoTeste.getTicker());

            // 2.9 FundoInvestimento (subclass de Ativo)
            fundoTeste = new FundoInvestimento("Fundo Patinhas Multimercado", 250.0, 40.0, 1.75, "MULTIMERCADO");
            fundoTeste = fundoDAO.inserir(fundoTeste);
            System.out.println("  [INSERT 9/13] FundoInvestimento (especializacao de Ativo) cadastrado - ID: " + fundoTeste.getId() + " | Nome: " + fundoTeste.getNome());

            // 2.10 Transacao (FK -> Carteira, Ativo)
            transacaoTeste = new Transacao("COMPRA", 4250.0, "Compra de lote inicial de acoes", acaoTeste, carteiraTeste);
            transacaoTeste = transacaoDAO.inserir(transacaoTeste);
            System.out.println("  [INSERT 10/13] Transacao cadastrada - ID: " + transacaoTeste.getId() + " | Tipo: " + transacaoTeste.getTipo() + " | Valor: R$ " + transacaoTeste.getValor());

            // 2.11 Relatorio (FK -> Usuario)
            relatorioTeste = new Relatorio("MENSAL", "Relatorio financeiro consolidado de teste da Fase 6.", usuarioTeste);
            relatorioTeste = relatorioDAO.inserir(relatorioTeste);
            System.out.println("  [INSERT 11/13] Relatorio cadastrado - ID: " + relatorioTeste.getId() + " | Tipo: " + relatorioTeste.getTipo());

            // 2.12 UsuarioEmpresa (FK -> Usuario, Empresa)
            usuarioEmpresaTeste = new UsuarioEmpresa(usuarioTeste, empresaTeste, "Gestor de Carteiras");
            usuarioEmpresaTeste = usuarioEmpresaDAO.inserir(usuarioEmpresaTeste);
            System.out.println("  [INSERT 12/13] UsuarioEmpresa cadastrado - ID: " + usuarioEmpresaTeste.getId() + " | Cargo: " + usuarioEmpresaTeste.getCargo());

            // 2.13 CarteiraAtivo (FK -> Carteira, Ativo)
            carteiraAtivoTeste = new CarteiraAtivo(carteiraTeste, acaoTeste, 100.0, 41.20);
            carteiraAtivoTeste = carteiraAtivoDAO.inserir(carteiraAtivoTeste);
            System.out.println("  [INSERT 13/13] CarteiraAtivo cadastrado - ID: " + carteiraAtivoTeste.getId() + " | Qtd: " + carteiraAtivoTeste.getQuantidade() + " | Preco Medio: R$ " + carteiraAtivoTeste.getPrecoMedio());

            // -------------------------------------------------------------------------
            // 3. OPERACOES DE SELECT (Consulta por ID e Listagem em ArrayList)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [2/4] INICIANDO TESTES DE SELECT (Conversao ResultSet -> Objetos Java -> ArrayList)...");

            testarSelectAutenticacaoMultifator(mfaTeste.getId());
            testarSelectAutenticacao(autTeste.getId());
            testarSelectPerfilInvestidor(perfilTeste.getId());
            testarSelectUsuario(usuarioTeste.getId());
            testarSelectEmpresa(empresaTeste.getId());
            testarSelectCarteira(carteiraTeste.getId());
            testarSelectAtivo(ativoTeste.getId());
            testarSelectAcao(acaoTeste.getId());
            testarSelectFundoInvestimento(fundoTeste.getId());
            testarSelectTransacao(transacaoTeste.getId());
            testarSelectRelatorio(relatorioTeste.getId());
            testarSelectUsuarioEmpresa(usuarioEmpresaTeste.getId());
            testarSelectCarteiraAtivo(carteiraAtivoTeste.getId());

            // -------------------------------------------------------------------------
            // 4. OPERACOES DE UPDATE (Atualizacao dos registros cadastrados)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [3/4] INICIANDO TESTES DE UPDATE (Atualizacao de dados existentes)...");

            testarUpdateAutenticacaoMultifator(mfaTeste);
            testarUpdateAutenticacao(autTeste);
            testarUpdatePerfilInvestidor(perfilTeste);
            testarUpdateUsuario(usuarioTeste);
            testarUpdateEmpresa(empresaTeste);
            testarUpdateCarteira(carteiraTeste);
            testarUpdateAtivo(ativoTeste);
            testarUpdateAcao(acaoTeste);
            testarUpdateFundoInvestimento(fundoTeste);
            testarUpdateTransacao(transacaoTeste);
            testarUpdateRelatorio(relatorioTeste);
            testarUpdateUsuarioEmpresa(usuarioEmpresaTeste);
            testarUpdateCarteiraAtivo(carteiraAtivoTeste);

            // -------------------------------------------------------------------------
            // 5. OPERACOES DE DELETE (Ordem inversa para nao violar Foreign Keys)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [4/4] INICIANDO TESTES DE DELETE (Exclusao respeitando chaves estrangeiras)...");

            // Exclusao de tabelas associativas e dependentes primeiro
            testarDeleteCarteiraAtivo(carteiraAtivoTeste.getId());
            testarDeleteUsuarioEmpresa(usuarioEmpresaTeste.getId());
            testarDeleteRelatorio(relatorioTeste.getId());
            testarDeleteTransacao(transacaoTeste.getId());

            // Exclusao de especializacoes e ativos
            testarDeleteFundoInvestimento(fundoTeste.getId());
            testarDeleteAcao(acaoTeste.getId());
            testarDeleteAtivo(ativoTeste.getId());

            // Exclusao de carteira e empresa
            testarDeleteCarteira(carteiraTeste.getId());
            testarDeleteEmpresa(empresaTeste.getId());

            // Exclusao de usuario e suas referencias
            testarDeleteUsuario(usuarioTeste.getId());
            testarDeletePerfilInvestidor(perfilTeste.getId());
            testarDeleteAutenticacao(autTeste.getId());
            testarDeleteAutenticacaoMultifator(mfaTeste.getId());

            System.out.println("\n================================================================================");
            System.out.println("  SUCESSO TOTAL! Todas as operacoes CRUD das 13 entidades foram validadas!");
            System.out.println("================================================================================");

        } catch (SQLException e) {
            System.err.println("\n[ERRO DE BANCO DE DADOS ORACLE]");
            System.err.println("  Mensagem: " + e.getMessage());
            System.err.println("  Codigo de erro: " + e.getErrorCode());
            System.err.println("  SQLState: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n[ERRO INESPERADO]");
            System.err.println("  Mensagem: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.closeConnection();
            System.out.println("Conexao com o banco encerrada de forma segura.");
        }
    }

    // =========================================================================
    // METODOS DE CONFIGURACAO E CONEXAO
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
        System.out.println("\nTestando conexao JDBC com o Oracle da FIAP...");
        Connection conn = ConnectionFactory.getConnection();
        System.out.println("  Conexao estabelecida com sucesso! Status: " + (!conn.isClosed() ? "ATIVA" : "FECHADA"));
    }

    // =========================================================================
    // METODOS DE TESTE SELECT
    // =========================================================================

    private static void testarSelectAutenticacaoMultifator(Long id) throws SQLException {
        AutenticacaoMultifator mfa = mfaDAO.buscarPorId(id);
        ArrayList<AutenticacaoMultifator> todos = mfaDAO.listarTodos();
        System.out.println("  [SELECT 1/13] AutenticacaoMultifator encontrada: " + mfa + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectAutenticacao(Long id) throws SQLException {
        Autenticacao aut = autenticacaoDAO.buscarPorId(id);
        ArrayList<Autenticacao> todos = autenticacaoDAO.listarTodos();
        System.out.println("  [SELECT 2/13] Autenticacao encontrada: " + aut + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectPerfilInvestidor(Long id) throws SQLException {
        PerfilInvestidor perfil = perfilDAO.buscarPorId(id);
        ArrayList<PerfilInvestidor> todos = perfilDAO.listarTodos();
        System.out.println("  [SELECT 3/13] PerfilInvestidor encontrado: " + perfil + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectUsuario(Long id) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        ArrayList<Usuario> todos = usuarioDAO.listarTodos();
        System.out.println("  [SELECT 4/13] Usuario encontrado: " + usuario + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectEmpresa(Long id) throws SQLException {
        Empresa empresa = empresaDAO.buscarPorId(id);
        ArrayList<Empresa> todas = empresaDAO.listarTodos();
        System.out.println("  [SELECT 5/13] Empresa encontrada: " + empresa + " | Total cadastrado: " + todas.size());
    }

    private static void testarSelectCarteira(Long id) throws SQLException {
        Carteira carteira = carteiraDAO.buscarPorId(id);
        ArrayList<Carteira> todas = carteiraDAO.listarTodos();
        System.out.println("  [SELECT 6/13] Carteira encontrada: " + carteira + " | Total cadastrado: " + todas.size());
    }

    private static void testarSelectAtivo(Long id) throws SQLException {
        Ativo ativo = ativoDAO.buscarPorId(id);
        ArrayList<Ativo> todos = ativoDAO.listarTodos();
        System.out.println("  [SELECT 7/13] Ativo encontrado: " + ativo + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectAcao(Long id) throws SQLException {
        Acao acao = acaoDAO.buscarPorId(id);
        ArrayList<Acao> todas = acaoDAO.listarTodos();
        System.out.println("  [SELECT 8/13] Acao encontrada: " + acao + " | Total cadastrado: " + todas.size());
    }

    private static void testarSelectFundoInvestimento(Long id) throws SQLException {
        FundoInvestimento fundo = fundoDAO.buscarPorId(id);
        ArrayList<FundoInvestimento> todos = fundoDAO.listarTodos();
        System.out.println("  [SELECT 9/13] FundoInvestimento encontrado: " + fundo + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectTransacao(Long id) throws SQLException {
        Transacao transacao = transacaoDAO.buscarPorId(id);
        ArrayList<Transacao> todas = transacaoDAO.listarTodos();
        System.out.println("  [SELECT 10/13] Transacao encontrada: " + transacao + " | Total cadastrado: " + todas.size());
    }

    private static void testarSelectRelatorio(Long id) throws SQLException {
        Relatorio relatorio = relatorioDAO.buscarPorId(id);
        ArrayList<Relatorio> todos = relatorioDAO.listarTodos();
        System.out.println("  [SELECT 11/13] Relatorio encontrado: " + relatorio + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectUsuarioEmpresa(Long id) throws SQLException {
        UsuarioEmpresa ue = usuarioEmpresaDAO.buscarPorId(id);
        ArrayList<UsuarioEmpresa> todos = usuarioEmpresaDAO.listarTodos();
        System.out.println("  [SELECT 12/13] UsuarioEmpresa encontrado: ID " + (ue != null ? ue.getId() : "null")
                + " | Cargo: " + (ue != null ? ue.getCargo() : "null") + " | Total cadastrado: " + todos.size());
    }

    private static void testarSelectCarteiraAtivo(Long id) throws SQLException {
        CarteiraAtivo ca = carteiraAtivoDAO.buscarPorId(id);
        ArrayList<CarteiraAtivo> todos = carteiraAtivoDAO.listarTodos();
        System.out.println("  [SELECT 13/13] CarteiraAtivo encontrado: " + ca + " | Total cadastrado: " + todos.size());
    }

    // =========================================================================
    // METODOS DE TESTE UPDATE
    // =========================================================================

    private static void testarUpdateAutenticacaoMultifator(AutenticacaoMultifator mfa) throws SQLException {
        mfa.setCodigo("999888");
        boolean ok = mfaDAO.atualizar(mfa);
        System.out.println("  [UPDATE 1/13] AutenticacaoMultifator atualizada: " + ok + " | Novo codigo: " + mfa.getCodigo());
    }

    private static void testarUpdateAutenticacao(Autenticacao aut) throws SQLException {
        aut.setSenha("novaSenhaFase6_456");
        boolean ok = autenticacaoDAO.atualizar(aut);
        System.out.println("  [UPDATE 2/13] Autenticacao atualizada: " + ok);
    }

    private static void testarUpdatePerfilInvestidor(PerfilInvestidor perfil) throws SQLException {
        perfil.setDescricao("Descricao de perfil atualizada durante teste da Fase 6");
        perfil.setNivelRisco("ALTO");
        boolean ok = perfilDAO.atualizar(perfil);
        System.out.println("  [UPDATE 3/13] PerfilInvestidor atualizado: " + ok + " | Novo risco: " + perfil.getNivelRisco());
    }

    private static void testarUpdateUsuario(Usuario usuario) throws SQLException {
        usuario.setNome(usuario.getNome() + " [ATUALIZADO]");
        usuario.setTelefone("(11) 91111-2222");
        boolean ok = usuarioDAO.atualizar(usuario);
        System.out.println("  [UPDATE 4/13] Usuario atualizado: " + ok + " | Novo nome: " + usuario.getNome());
    }

    private static void testarUpdateEmpresa(Empresa empresa) throws SQLException {
        empresa.setNome(empresa.getNome() + " S.A.");
        boolean ok = empresaDAO.atualizar(empresa);
        System.out.println("  [UPDATE 5/13] Empresa atualizada: " + ok + " | Novo nome: " + empresa.getNome());
    }

    private static void testarUpdateCarteira(Carteira carteira) throws SQLException {
        carteira.setNome(carteira.getNome() + " Premium");
        carteira.setSaldoTotal(15000.0);
        boolean ok = carteiraDAO.atualizar(carteira);
        System.out.println("  [UPDATE 6/13] Carteira atualizada: " + ok + " | Novo saldo: R$ " + carteira.getSaldoTotal());
    }

    private static void testarUpdateAtivo(Ativo ativo) throws SQLException {
        ativo.setValorAtual(13000.0);
        ativo.setQuantidade(3.0);
        boolean ok = ativoDAO.atualizar(ativo);
        System.out.println("  [UPDATE 7/13] Ativo base atualizado: " + ok + " | Novo valor: R$ " + ativo.getValorAtual());
    }

    private static void testarUpdateAcao(Acao acao) throws SQLException {
        acao.setValorAtual(44.20);
        acao.setVariacaoDiaria(3.10);
        boolean ok = acaoDAO.atualizar(acao);
        System.out.println("  [UPDATE 8/13] Acao atualizada: " + ok + " | Nova cotacao: R$ " + acao.getValorAtual() + " (" + acao.getVariacaoDiaria() + "%)");
    }

    private static void testarUpdateFundoInvestimento(FundoInvestimento fundo) throws SQLException {
        fundo.setTaxaAdministracao(1.50);
        fundo.setValorAtual(260.0);
        boolean ok = fundoDAO.atualizar(fundo);
        System.out.println("  [UPDATE 9/13] FundoInvestimento atualizado: " + ok + " | Nova taxa: " + fundo.getTaxaAdministracao() + "%");
    }

    private static void testarUpdateTransacao(Transacao transacao) throws SQLException {
        transacao.setDescricao(transacao.getDescricao() + " - Aprovada e liquidada");
        boolean ok = transacaoDAO.atualizar(transacao);
        System.out.println("  [UPDATE 10/13] Transacao atualizada: " + ok + " | Nova desc: " + transacao.getDescricao());
    }

    private static void testarUpdateRelatorio(Relatorio relatorio) throws SQLException {
        relatorio.setConteudo(relatorio.getConteudo() + " [REVISADO]");
        boolean ok = relatorioDAO.atualizar(relatorio);
        System.out.println("  [UPDATE 11/13] Relatorio atualizado: " + ok);
    }

    private static void testarUpdateUsuarioEmpresa(UsuarioEmpresa ue) throws SQLException {
        ue.setCargo("Diretor de Investimentos");
        boolean ok = usuarioEmpresaDAO.atualizar(ue);
        System.out.println("  [UPDATE 12/13] UsuarioEmpresa atualizado: " + ok + " | Novo cargo: " + ue.getCargo());
    }

    private static void testarUpdateCarteiraAtivo(CarteiraAtivo ca) throws SQLException {
        ca.setQuantidade(150.0);
        ca.setPrecoMedio(41.80);
        boolean ok = carteiraAtivoDAO.atualizar(ca);
        System.out.println("  [UPDATE 13/13] CarteiraAtivo atualizado: " + ok + " | Nova qtd: " + ca.getQuantidade());
    }

    // =========================================================================
    // METODOS DE TESTE DELETE
    // =========================================================================

    private static void testarDeleteCarteiraAtivo(Long id) throws SQLException {
        boolean ok = carteiraAtivoDAO.excluir(id);
        boolean naoExiste = carteiraAtivoDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 1/13] CarteiraAtivo excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteUsuarioEmpresa(Long id) throws SQLException {
        boolean ok = usuarioEmpresaDAO.excluir(id);
        boolean naoExiste = usuarioEmpresaDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 2/13] UsuarioEmpresa excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteRelatorio(Long id) throws SQLException {
        boolean ok = relatorioDAO.excluir(id);
        boolean naoExiste = relatorioDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 3/13] Relatorio excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteTransacao(Long id) throws SQLException {
        boolean ok = transacaoDAO.excluir(id);
        boolean naoExiste = transacaoDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 4/13] Transacao excluida: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteFundoInvestimento(Long id) throws SQLException {
        boolean ok = fundoDAO.excluir(id);
        boolean naoExiste = fundoDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 5/13] FundoInvestimento (e base ATIVO) excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteAcao(Long id) throws SQLException {
        boolean ok = acaoDAO.excluir(id);
        boolean naoExiste = acaoDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 6/13] Acao (e base ATIVO) excluida: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteAtivo(Long id) throws SQLException {
        boolean ok = ativoDAO.excluir(id);
        boolean naoExiste = ativoDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 7/13] Ativo base excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteCarteira(Long id) throws SQLException {
        boolean ok = carteiraDAO.excluir(id);
        boolean naoExiste = carteiraDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 8/13] Carteira excluida: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteEmpresa(Long id) throws SQLException {
        boolean ok = empresaDAO.excluir(id);
        boolean naoExiste = empresaDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 9/13] Empresa excluida: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteUsuario(Long id) throws SQLException {
        boolean ok = usuarioDAO.excluir(id);
        boolean naoExiste = usuarioDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 10/13] Usuario excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeletePerfilInvestidor(Long id) throws SQLException {
        boolean ok = perfilDAO.excluir(id);
        boolean naoExiste = perfilDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 11/13] PerfilInvestidor excluido: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteAutenticacao(Long id) throws SQLException {
        boolean ok = autenticacaoDAO.excluir(id);
        boolean naoExiste = autenticacaoDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 12/13] Autenticacao excluida: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }

    private static void testarDeleteAutenticacaoMultifator(Long id) throws SQLException {
        boolean ok = mfaDAO.excluir(id);
        boolean naoExiste = mfaDAO.buscarPorId(id) == null;
        System.out.println("  [DELETE 13/13] AutenticacaoMultifator excluida: " + ok + " | Verificacao SELECT null: " + naoExiste);
    }
}
