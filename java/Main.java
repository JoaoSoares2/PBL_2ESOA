import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("  Sistema de Gestão de Investimentos");
        System.out.println("========================================\n");

        ArrayList<Empresa> empresas = new ArrayList<>();
        ArrayList<Ativo> ativos = new ArrayList<>();
        HashMap<Long, Usuario> usuarios = new HashMap<>();
        HashMap<Long, Carteira> carteiras = new HashMap<>();

        // ============================
        // ArrayList<Empresa>
        // ============================
        System.out.println("--- [1] ArrayList<Empresa>: Inserção, Listagem e Percurso ---");
        try {
            Empresa xp = new Empresa("XP Investimentos", "33.775.974/0001-04");
            xp.setId(1L);
            Empresa nubank = new Empresa("Nu Investimentos", "18.236.120/0001-58");
            nubank.setId(2L);
            Empresa btg = new Empresa("BTG Pactual", "30.306.294/0001-45");
            btg.setId(3L);

            empresas.add(xp);
            empresas.add(nubank);
            empresas.add(btg);
            System.out.println("Inseridos: " + empresas.size() + " empresas");

            System.out.println("\nListagem (for com índice):");
            for (int i = 0; i < empresas.size(); i++) {
                System.out.println("  [" + i + "] " + empresas.get(i));
            }

            System.out.println("\nPercurso (forEach):");
            empresas.forEach(e -> System.out.println("  -> " + e.getNome() + " | CNPJ: " + e.getCnpj()));
        } catch (Exception e) {
            System.err.println("Erro em ArrayList<Empresa>: " + e.getMessage());
        }

        // ============================
        // ArrayList<Ativo> + Herança + Override
        // ============================
        System.out.println("\n--- [2] ArrayList<Ativo>: Inserção, Listagem, Percurso e Polimorfismo ---");
        try {
            Acao petr4 = new Acao("Petrobras", 35.50, 100.0, "PETR4", "B3");
            petr4.setVariacaoDiaria(2.3);
            Acao vale3 = new Acao("Vale", 68.50, 50.0, "VALE3", "B3");
            FundoInvestimento cdb = new FundoInvestimento("CDB Banco Inter", 1000.0, 5.0, 1.5, "RENDA_FIXA");
            FundoInvestimento multi = new FundoInvestimento("Fundo Multi XP", 500.0, 10.0, 2.0, "MULTIMERCADO");

            ativos.add(petr4);
            ativos.add(vale3);
            ativos.add(cdb);
            ativos.add(multi);
            System.out.println("Inseridos: " + ativos.size() + " ativos");

            petr4.atualizarCotacao(36.00);
            vale3.atualizarCotacao(1.5, true);

            System.out.println("\nListagem (for com índice):");
            for (int i = 0; i < ativos.size(); i++) {
                System.out.println("  [" + i + "] " + ativos.get(i));
            }

            System.out.println("\nPercurso com polimorfismo dinâmico (calcularValorTotal override):");
            ativos.forEach(a -> System.out.printf("  -> %s: R$ %.2f%n", a.getNome(), a.calcularValorTotal()));
        } catch (Exception e) {
            System.err.println("Erro em ArrayList<Ativo>: " + e.getMessage());
        }

        // ============================
        // HashMap<Long, Usuario>
        // ============================
        System.out.println("\n--- [3] HashMap<Long, Usuario>: Inserção, Busca, Atualização e Listagem ---");
        try {
            Usuario ana = new Usuario("Ana Paula", "ana@email.com", "senha123", "123.456.789-00", "(11) 91234-5678");
            ana.setId(1L);
            Usuario joao = new Usuario("João Silva", "joao@email.com", "senha456", "987.654.321-00", "(11) 98765-4321");
            joao.setId(2L);
            Usuario maria = new Usuario("Maria Santos", "maria@email.com", "senha789", "456.789.123-00", "(21) 99876-5432");
            maria.setId(3L);

            usuarios.put(ana.getId(), ana);
            usuarios.put(joao.getId(), joao);
            usuarios.put(maria.getId(), maria);
            System.out.println("Inseridos: " + usuarios.size() + " usuarios");

            Usuario buscado = usuarios.get(2L);
            System.out.println("\nBusca por ID 2: " + buscado);

            buscado.setEmail("joao.silva@newemail.com");
            buscado.setTelefone("(11) 91111-2222");
            System.out.println("Atualizado: " + usuarios.get(2L));

            System.out.println("\nListagem completa:");
            usuarios.forEach((id, u) -> System.out.println("  ID " + id + ": " + u));

            PerfilInvestidor perfil = new PerfilInvestidor("Moderado", "Equilíbrio entre risco e retorno", "MEDIO");
            ana.setPerfilInvestidor(perfil);
            perfil.exibirPerfil();
            System.out.println("Avaliação: " + perfil.avaliarRisco());

            Autenticacao auth = new Autenticacao("ana@email.com", "senha123");
            AutenticacaoMultifator mfa = new AutenticacaoMultifator();
            auth.setAutenticacaoMultifator(mfa);
            ana.setAutenticacao(auth);
            String codigo = mfa.gerarCodigo();
            System.out.println("MFA gerado: " + codigo + " | Válido: " + mfa.validarCodigo(codigo));
        } catch (Exception e) {
            System.err.println("Erro em HashMap<Long, Usuario>: " + e.getMessage());
        }

        // ============================
        // HashMap<Long, Carteira> + CarteiraAtivo + Transacao
        // ============================
        System.out.println("\n--- [4] HashMap<Long, Carteira>: Inserção, Busca, Atualização e Listagem ---");
        try {
            Carteira rf = new Carteira("Renda Fixa");
            rf.setId(1L);
            Carteira rv = new Carteira("Renda Variável");
            rv.setId(2L);

            carteiras.put(rf.getId(), rf);
            carteiras.put(rv.getId(), rv);
            System.out.println("Inseridas: " + carteiras.size() + " carteiras");

            FundoInvestimento cdbXP = new FundoInvestimento("CDB XP", 1000.0, 3.0, 1.2, "RENDA_FIXA");
            Acao petr4 = new Acao("Petrobras", 36.00, 100.0, "PETR4", "B3");

            carteiras.get(1L).adicionarAtivo(cdbXP, 3.0, 980.00);
            carteiras.get(2L).adicionarAtivo(petr4);

            System.out.println("\nBusca ID 1: " + carteiras.get(1L));
            System.out.println("Busca ID 2: " + carteiras.get(2L));

            carteiras.get(1L).setNome("Conservadora - Renda Fixa");
            System.out.println("Atualizado: " + carteiras.get(1L).getNome());

            System.out.println("\nListagem completa:");
            carteiras.forEach((id, c) -> System.out.printf("  ID %d: %s | Saldo: R$ %.2f%n",
                    id, c.getNome(), c.getSaldoTotal()));

            System.out.println("\nCarteiraAtivo (entidade associativa Carteira <-> Ativo):");
            carteiras.forEach((id, c) -> c.getCarteiraAtivos().forEach(ca -> System.out.println("  " + ca)));

            Transacao factory = new Transacao();
            Transacao compra = factory.registrarCompra(petr4);
            Transacao venda = factory.registrarVenda(500.0, "Resgate parcial CDB");
            carteiras.get(2L).registrarTransacao(compra);
            carteiras.get(1L).registrarTransacao(venda);

            System.out.println("\nTransações:");
            carteiras.forEach((id, c) -> {
                if (!c.getTransacoes().isEmpty()) {
                    System.out.println("  " + c.getNome() + ":");
                    factory.consultarTransacoes(c.getTransacoes());
                }
            });
        } catch (Exception e) {
            System.err.println("Erro em HashMap<Long, Carteira>: " + e.getMessage());
        }

        // ============================
        // UsuarioEmpresa (entidade associativa N:M)
        // ============================
        System.out.println("\n--- [5] UsuarioEmpresa: Entidade Associativa Usuario <-> Empresa ---");
        try {
            Usuario ana = usuarios.get(1L);
            ana.vincularEmpresa(empresas.get(0), "Analista de Investimentos");
            ana.vincularEmpresa(empresas.get(1), "Consultora Financeira");

            System.out.println("Vínculos de " + ana.getNome() + ":");
            ana.getVinculosEmpresa().forEach(v -> System.out.println("  -> " + v));

            empresas.get(0).adicionarCarteira(new Carteira("Carteira Ações XP"));
            System.out.println("\nCarteiras visíveis por " + ana.getNome() + ":");
            ana.vizualizarCarteira().forEach(c -> System.out.println("  -> " + c));
        } catch (Exception e) {
            System.err.println("Erro em UsuarioEmpresa: " + e.getMessage());
        }

        // ============================
        // Relatorio: Override (RelatorioAnual, RelatorioMensal)
        // ============================
        System.out.println("\n--- [6] Relatorio: Polimorfismo Dinâmico (Override) e Estático (Overload) ---");
        try {
            RelatorioAnual relAnual = new RelatorioAnual(2025);
            RelatorioMensal relMensal = new RelatorioMensal(6, 2025);

            Relatorio[] relatorios = {relAnual, relMensal};
            for (Relatorio r : relatorios) {
                r.gerarRelatorio();
            }

            System.out.println();
            relAnual.gerarRelatorio("COMPRAS");
            relAnual.gerarRelatorio("VENDAS", new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
        } catch (Exception e) {
            System.err.println("Erro em Relatorio: " + e.getMessage());
        }

        // ============================
        // Manipulação de Arquivos (.txt)
        // ============================
        System.out.println("\n--- [7] Manipulação de Arquivos (.txt) ---");
        try {
            escreverArquivo("empresas.txt", empresas);
            System.out.println("empresas.txt criado (" + empresas.size() + " registros)");

            escreverArquivo("ativos.txt", ativos);
            System.out.println("ativos.txt criado (" + ativos.size() + " registros)");

            empresas.add(new Empresa("Rico Investimentos", "44.555.666/0001-77"));
            escreverArquivo("empresas.txt", empresas);
            System.out.println("empresas.txt atualizado (" + empresas.size() + " registros)");

            System.out.println("\nConteúdo de empresas.txt:");
            lerArquivo("empresas.txt");

            System.out.println("\nConteúdo de ativos.txt:");
            lerArquivo("ativos.txt");
        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        }

        System.out.println("\n========================================");
        System.out.println("  Testes concluídos com sucesso.");
        System.out.println("========================================");
    }

    private static <T> void escreverArquivo(String nomeArquivo, ArrayList<T> lista) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (T item : lista) {
                writer.write(item.toString());
                writer.newLine();
            }
        }
    }

    private static void lerArquivo(String nomeArquivo) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                System.out.println("  " + linha);
            }
        }
    }
}
