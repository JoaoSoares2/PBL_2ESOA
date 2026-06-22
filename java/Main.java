import java.util.Date;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("  Sistema de Gestão de Investimentos");
        System.out.println("========================================\n");

        System.out.println("--- [1] Ativos: Herança e Polimorfismo Dinâmico (Override) ---");
        try {
            AtivoRendaFixa tesouro   = new AtivoRendaFixa("Tesouro Selic 2026", 1000.0, 5.0, 6.5);
            AtivoRendaFixa cdb       = new AtivoRendaFixa("CDB XP Investimentos", 500.0, 10.0, 12.5);
            AtivoRendaVariavel petr4 = new AtivoRendaVariavel("Petrobras", 35.50, 100.0, "PETR4");

            petr4.setVariacaoDiaria(2.3);
            System.out.println("Ticker via getter: " + petr4.getTicker());
            System.out.println("Taxa CDB via getter: " + cdb.getTaxaJurosAnual() + "% a.a.");

            tesouro.atualizarCotacao(1010.0);
            petr4.atualizarCotacao(1.5, true);

            System.out.println("\n[Polimorfismo dinâmico - calcularValorTotal()]");
            Ativo[] ativos = {tesouro, cdb, petr4};
            for (Ativo a : ativos) {
                System.out.println(a);
                System.out.printf("  -> Valor Total: R$ %.2f%n", a.calcularValorTotal());
            }
        } catch (NullPointerException e) {
            System.err.println("Erro: campo nulo em Ativo - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado em Ativo: " + e.getMessage());
        }

        System.out.println("\n--- [2] Usuario (Getters/Setters) ---");
        try {
            Usuario usuario = new Usuario("Ana Paula", "ana@email.com", "senha@456",
                                          "987.654.321-00", "(11) 98765-4321");
            usuario.setId(1L);

            PerfilInvestidor perfil = new PerfilInvestidor(
                    "Moderado", "Equilíbrio entre risco e retorno", "MEDIO");
            usuario.setPerfilInvestidor(perfil);

            Autenticacao auth = new Autenticacao("ana@email.com", "senha@456");
            AutenticacaoMultifator mfa = new AutenticacaoMultifator();
            auth.setAutenticacaoMultifator(mfa);
            usuario.setAutenticacao(auth);

            System.out.println(usuario);
            System.out.println("Nome via getter: " + usuario.getNome());

            usuario.setEmail("ana.paula@email.com");
            System.out.println("Email atualizado via setter: " + usuario.getEmail());

            System.out.println("Autenticação: "
                    + (usuario.autenticar("ana@email.com", "senha@456") ? "aprovada" : "negada"));
        } catch (NullPointerException e) {
            System.err.println("Erro: referência nula em Usuario - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao instanciar Usuario: " + e.getMessage());
        }

        System.out.println("\n--- [3] PerfilInvestidor (Getters/Setters) ---");
        try {
            PerfilInvestidor conservador = new PerfilInvestidor();
            conservador.setNomePerfil("Conservador");
            conservador.setDescricao("Prioridade à segurança do capital investido.");
            conservador.setNivelRisco("BAIXO");

            conservador.exibirPerfil();
            System.out.println("Avaliação de risco: " + conservador.avaliarRisco());
        } catch (NullPointerException e) {
            System.err.println("Erro: nulo em PerfilInvestidor - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro em PerfilInvestidor: " + e.getMessage());
        }

        System.out.println("\n--- [4] Autenticacao e MFA ---");
        try {
            Autenticacao auth = new Autenticacao("joao@email.com", "minhaSenha123");
            AutenticacaoMultifator mfa = new AutenticacaoMultifator();
            auth.setAutenticacaoMultifator(mfa);

            String codigoGerado = mfa.gerarCodigo();
            System.out.println(auth);
            System.out.println("Código MFA gerado: " + codigoGerado);
            System.out.println("Credenciais válidas: "
                    + auth.validarCredenciais("joao@email.com", "minhaSenha123"));
            System.out.println("MFA com código correto: " + mfa.validarCodigo(codigoGerado));
            System.out.println("MFA com código errado:  " + mfa.validarCodigo("000000"));
        } catch (Exception e) {
            System.err.println("Erro em Autenticacao: " + e.getMessage());
        }

        System.out.println("\n--- [5] Carteira: Polimorfismo Estático (Overload) ---");
        try {
            Carteira carteira = new Carteira("Carteira Diversificada");

            AtivoRendaFixa lft = new AtivoRendaFixa("LFT 2026", 1000.0, 3.0, 10.75);
            carteira.adicionarAtivo(lft);
            carteira.adicionarAtivo("VALE3", 68.50, 50.0);
            carteira.adicionarAtivo("Ativo Temporário", 100.0, 1.0);
            carteira.removerAtivo("Ativo Temporário");

            System.out.println(carteira);
            System.out.printf("Saldo total: R$ %.2f%n", carteira.calcularSaldo());
        } catch (NullPointerException e) {
            System.err.println("Erro: nulo em Carteira - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro em Carteira: " + e.getMessage());
        }

        System.out.println("\n--- [6] Transacao: Entidade Associativa + Overload ---");
        try {
            AtivoRendaVariavel vale = new AtivoRendaVariavel("Vale", 68.50, 20.0, "VALE3");
            AtivoRendaFixa cdbInter = new AtivoRendaFixa("CDB Banco Inter", 1000.0, 5.0, 11.0);

            Transacao factory = new Transacao();

            Transacao compra1 = factory.registrarCompra(vale);
            Transacao compra2 = factory.registrarCompra(2000.00, "Compra Tesouro Direto");
            Transacao venda1  = factory.registrarVenda(cdbInter);
            Transacao venda2  = factory.registrarVenda(800.00, "Venda parcial PETR4");

            Carteira historico = new Carteira("Histórico de Transações");
            historico.registrarTransacao(compra1);
            historico.registrarTransacao(compra2);
            historico.registrarTransacao(venda1);
            historico.registrarTransacao(venda2);

            System.out.println("Extrato:");
            factory.consultarTransacoes(historico.getTransacoes());
        } catch (NullPointerException e) {
            System.err.println("Erro: nulo em Transacao - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro em Transacao: " + e.getMessage());
        }

        System.out.println("\n--- [7] Empresa e Relatorio: Overload + Override ---");
        try {
            Empresa empresa = new Empresa("XP Investimentos", "33.775.974/0001-04");
            empresa.adicionarCarteira(new Carteira("Renda Fixa"));
            empresa.adicionarCarteira(new Carteira("Renda Variável"));
            System.out.println(empresa);

            empresa.atualizarEmpresa("XP Investimentos S.A.");
            System.out.println("Nome atualizado: " + empresa.getNome());

            empresa.atualizarEmpresa("XP Investimentos S.A.", "33.775.974/0001-04");
            System.out.println("CNPJ via getter: " + empresa.getCnpj());

            System.out.println("\n[Polimorfismo dinâmico - gerarRelatorio()]");
            RelatorioAnual  relAnual  = new RelatorioAnual(2025);
            RelatorioMensal relMensal = new RelatorioMensal(5, 2025);

            Relatorio[] relatorios = {relAnual, relMensal};
            for (Relatorio r : relatorios) {
                r.gerarRelatorio();
            }

            System.out.println();
            relAnual.gerarRelatorio("COMPRAS");
            relAnual.gerarRelatorio("VENDAS",
                    new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
        } catch (Exception e) {
            System.err.println("Erro em Empresa/Relatorio: " + e.getMessage());
        }

        System.out.println("\n========================================");
        System.out.println("  Testes concluídos com sucesso.");
        System.out.println("========================================");
    }
}
