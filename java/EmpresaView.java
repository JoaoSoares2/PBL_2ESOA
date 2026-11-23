import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Empresas (Fase 7).
 *
 * Implementa o submenu com as operacoes completas de CRUD:
 * [1] Inclusao
 * [2] Alteracao
 * [3] Exclusao
 * [4] Exibicao de todos (via ArrayList)
 * [5] Exibicao de um unico registro (por ID)
 * [6] Busca por CNPJ
 * [0] Voltar ao menu anterior
 */
public final class EmpresaView {

    private static final EmpresaDAO empresaDAO = new EmpresaDAO();

    private EmpresaView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: GESTÃO DE EMPRESAS");
            System.out.println("  [1] Inclusão             - Cadastrar nova empresa");
            System.out.println("  [2] Alteração            - Atualizar empresa existente");
            System.out.println("  [3] Exclusão             - Remover empresa do banco");
            System.out.println("  [4] Exibição de Todos    - Listar todas as empresas (ArrayList)");
            System.out.println("  [5] Exibição de Registro - Consultar detalhes por ID");
            System.out.println("  [6] Consulta por CNPJ    - Localizar empresa pelo CNPJ");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 6);

            switch (opcao) {
                case 1 -> incluirEmpresa();
                case 2 -> alterarEmpresa();
                case 3 -> excluirEmpresa();
                case 4 -> exibirTodas();
                case 5 -> exibirPorId();
                case 6 -> buscarPorCnpj();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirEmpresa() {
        ConsoleUtils.imprimirSubcabecalho("INCLUSÃO DE NOVA EMPRESA");

        try {
            String nome = ConsoleUtils.lerTexto("Nome / Razão Social: ", true);
            String cnpj = Validador.lerCnpj("CNPJ (14 dígitos): ", true);

            // Valida duplicidade de CNPJ antes do insert
            Empresa existente = empresaDAO.buscarPorCnpj(cnpj);
            if (existente != null) {
                ConsoleUtils.imprimirAviso("Já existe uma empresa cadastrada com o CNPJ " + cnpj + " (ID: " + existente.getId() + " - " + existente.getNome() + ").");
                ConsoleUtils.pausar();
                return;
            }

            Empresa novaEmpresa = new Empresa(nome, cnpj);

            System.out.println("\nDados para confirmação:");
            System.out.println("  Nome: " + novaEmpresa.getNome());
            System.out.println("  CNPJ: " + novaEmpresa.getCnpj());

            if (ConsoleUtils.confirmar("Confirma o cadastro desta empresa?")) {
                Empresa salva = empresaDAO.inserir(novaEmpresa);
                ConsoleUtils.imprimirSucesso("Empresa cadastrada com sucesso! ID gerado no Oracle: " + salva.getId());
            } else {
                ConsoleUtils.imprimirAviso("Operação de cadastro cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("cadastrar empresa", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarEmpresa() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE DADOS DA EMPRESA");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da empresa que deseja alterar: ", 1L, null);
            Empresa empresa = empresaDAO.buscarPorId(id);

            if (empresa == null) {
                ConsoleUtils.imprimirAviso("Nenhuma empresa encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais da empresa:");
            System.out.println("  ID  : " + empresa.getId());
            System.out.println("  Nome: " + empresa.getNome());
            System.out.println("  CNPJ: " + empresa.getCnpj());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o valor atual)");

            String novoNome = ConsoleUtils.lerTextoOpcional("Novo Nome [" + empresa.getNome() + "]: ", empresa.getNome());
            String novoCnpj = Validador.lerCnpjOpcional("Novo CNPJ [" + empresa.getCnpj() + "]: ", empresa.getCnpj());

            // Se o CNPJ foi alterado, verifica se ja nao pertence a outra empresa
            if (!novoCnpj.equals(empresa.getCnpj())) {
                Empresa comMesmoCnpj = empresaDAO.buscarPorCnpj(novoCnpj);
                if (comMesmoCnpj != null && !comMesmoCnpj.getId().equals(empresa.getId())) {
                    ConsoleUtils.imprimirAviso("O CNPJ " + novoCnpj + " já está em uso pela empresa ID " + comMesmoCnpj.getId() + ".");
                    ConsoleUtils.pausar();
                    return;
                }
            }

            empresa.setNome(novoNome);
            empresa.setCnpj(novoCnpj);

            if (ConsoleUtils.confirmar("Confirma as alterações?")) {
                boolean sucesso = empresaDAO.atualizar(empresa);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Empresa atualizada com sucesso no banco de dados Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar a empresa.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar empresa", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirEmpresa() {
        ConsoleUtils.imprimirSubcabecalho("EXCLUSÃO DE EMPRESA");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da empresa que deseja excluir: ", 1L, null);
            Empresa empresa = empresaDAO.buscarPorId(id);

            if (empresa == null) {
                ConsoleUtils.imprimirAviso("Nenhuma empresa encontrada com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados da empresa a ser excluída:");
            System.out.println("  ID  : " + empresa.getId());
            System.out.println("  Nome: " + empresa.getNome());
            System.out.println("  CNPJ: " + empresa.getCnpj());

            if (ConsoleUtils.confirmar("ATENÇÃO: Confirma a exclusão definitiva desta empresa?")) {
                boolean sucesso = empresaDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Empresa removida com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível excluir a empresa.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Exclusão cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir empresa", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodas() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODAS AS EMPRESAS");

        try {
            ArrayList<Empresa> empresas = empresaDAO.listarTodos();

            if (empresas.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhuma empresa cadastrada no momento.");
            } else {
                imprimirTabelaEmpresas(empresas);
                System.out.println("  Total de empresas cadastradas: " + empresas.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar empresas", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE EMPRESA POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID da empresa: ", 1L, null);
            Empresa empresa = empresaDAO.buscarPorId(id);

            if (empresa == null) {
                ConsoleUtils.imprimirAviso("Nenhuma empresa encontrada com o ID " + id + ".");
            } else {
                imprimirDetalhesEmpresa(empresa);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar empresa por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorCnpj() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE EMPRESA POR CNPJ");

        try {
            String cnpj = Validador.lerCnpj("Informe o CNPJ: ", true);
            Empresa empresa = empresaDAO.buscarPorCnpj(cnpj);

            if (empresa == null) {
                ConsoleUtils.imprimirAviso("Nenhuma empresa encontrada com o CNPJ " + cnpj + ".");
            } else {
                imprimirDetalhesEmpresa(empresa);
            }
        } catch (SQLException e) {
            tratarErroSql("buscar empresa por CNPJ", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static void imprimirDetalhesEmpresa(Empresa empresa) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  DETALHES DA EMPRESA (ID: " + empresa.getId() + ")");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  ID                  : " + empresa.getId());
        System.out.println("  Razão Social / Nome : " + empresa.getNome());
        System.out.println("  CNPJ                : " + empresa.getCnpj());
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirTabelaEmpresas(ArrayList<Empresa> empresas) {
        System.out.println("+--------+------------------------------------------+--------------------+");
        System.out.println("| ID     | NOME / RAZÃO SOCIAL                      | CNPJ               |");
        System.out.println("+--------+------------------------------------------+--------------------+");

        for (Empresa e : empresas) {
            String nome = e.getNome();
            if (nome != null && nome.length() > 40) {
                nome = nome.substring(0, 37) + "...";
            }
            System.out.printf("| %-6d | %-40s | %-18s |\n",
                    e.getId(),
                    nome != null ? nome : "-",
                    e.getCnpj() != null ? e.getCnpj() : "-");
        }

        System.out.println("+--------+------------------------------------------+--------------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        if (e.getErrorCode() == 2292) {
            // ORA-02292: integrity constraint violated - child record found
            ConsoleUtils.imprimirErro("Não é possível excluir esta empresa pois existem carteiras ou vínculos cadastrados associados a ela.");
            ConsoleUtils.imprimirAviso("Remova primeiro os registros dependentes antes de excluir a empresa.");
        } else if (e.getErrorCode() == 1) {
            // ORA-00001: unique constraint violated
            ConsoleUtils.imprimirErro("Já existe uma empresa com este CNPJ cadastrada no banco de dados.");
        } else {
            ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
        }
    }
}
