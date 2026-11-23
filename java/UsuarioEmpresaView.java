import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Camada de visualizacao e controle para o Modulo de Vinculos Corporativos (UsuarioEmpresa) - Fase 7.
 *
 * Gerencia a associacao N:M entre Usuarios e Empresas gestoras/corretoras,
 * registrando cargos corporativos (ex: Diretor de Risco, Gestor de Carteiras, Analista).
 *
 * Operacoes:
 * [1] Inclusao (Vincular usuario a empresa com cargo)
 * [2] Alteracao (Atualizar cargo)
 * [3] Exclusao (Remover vinculo corporativo)
 * [4] Exibicao de Todos os Vinculos (ArrayList)
 * [5] Exibicao por ID
 * [6] Consulta de Vinculos por Usuario
 * [7] Quadro de Funcionarios por Empresa
 * [0] Voltar ao Menu Principal
 */
public final class UsuarioEmpresaView {

    private static final UsuarioEmpresaDAO usuarioEmpresaDAO = new UsuarioEmpresaDAO();
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final EmpresaDAO empresaDAO = new EmpresaDAO();

    private UsuarioEmpresaView() {
        // Construtor privado para impedir instanciacao
    }

    public static void exibirMenu() {
        boolean voltar = false;

        while (!voltar) {
            ConsoleUtils.imprimirCabecalho("MÓDULO: VÍNCULOS CORPORATIVOS (USUÁRIO - EMPRESA)");
            System.out.println("  [1] Novo Vínculo Corporativo - Associar usuário a uma empresa gestora");
            System.out.println("  [2] Alteração de Cargo       - Atualizar cargo/função do usuário");
            System.out.println("  [3] Remover Vínculo          - Desvincular usuário da empresa");
            System.out.println("  [4] Exibição de Todos        - Listar todos os vínculos (ArrayList)");
            System.out.println("  [5] Exibição por ID          - Consultar detalhes de um vínculo");
            System.out.println("  [6] Vínculos por Usuário     - Consultar empresas de um usuário");
            System.out.println("  [7] Quadro por Empresa       - Listar colaboradores de uma empresa");
            System.out.println("  [0] Voltar ao Menu Principal");
            ConsoleUtils.imprimirLinhaDivisoria();

            int opcao = ConsoleUtils.lerInt("Escolha uma opção: ", 0, 7);

            switch (opcao) {
                case 1 -> incluirVinculo();
                case 2 -> alterarVinculo();
                case 3 -> excluirVinculo();
                case 4 -> exibirTodos();
                case 5 -> exibirPorId();
                case 6 -> buscarPorUsuario();
                case 7 -> buscarPorEmpresa();
                case 0 -> voltar = true;
            }
        }
    }

    // =========================================================================
    // OPERACOES CRUD
    // =========================================================================

    private static void incluirVinculo() {
        ConsoleUtils.imprimirSubcabecalho("NOVO VÍNCULO CORPORATIVO");

        try {
            Usuario usuario = selecionarUsuario();
            if (usuario == null) {
                ConsoleUtils.pausar();
                return;
            }

            Empresa empresa = selecionarEmpresa();
            if (empresa == null) {
                ConsoleUtils.pausar();
                return;
            }

            String cargo = ConsoleUtils.lerTexto("Cargo corporativo (ex: Gestor de Carteiras, Analista de Investimentos): ", true);

            UsuarioEmpresa ue = new UsuarioEmpresa(usuario, empresa, cargo);

            System.out.println("\nDados para confirmação do vínculo:");
            System.out.println("  Usuário : " + usuario.getNome() + " (CPF: " + usuario.getCpf() + ")");
            System.out.println("  Empresa : " + empresa.getNome() + " (CNPJ: " + empresa.getCnpj() + ")");
            System.out.println("  Cargo   : " + ue.getCargo());

            if (ConsoleUtils.confirmar("Confirma o registro deste vínculo no banco Oracle?")) {
                UsuarioEmpresa salvo = usuarioEmpresaDAO.inserir(ue);
                ConsoleUtils.imprimirSucesso("Vínculo corporativo registrado com sucesso! ID gerado no Oracle: " + salvo.getId());
            } else {
                ConsoleUtils.imprimirAviso("Operação cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("vincular usuário a empresa", e);
        }

        ConsoleUtils.pausar();
    }

    private static void alterarVinculo() {
        ConsoleUtils.imprimirSubcabecalho("ALTERAÇÃO DE CARGO CORPORATIVO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do vínculo corporativo: ", 1L, null);
            UsuarioEmpresa ue = usuarioEmpresaDAO.buscarPorId(id);

            if (ue == null) {
                ConsoleUtils.imprimirAviso("Nenhum vínculo encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados atuais do vínculo:");
            System.out.println("  ID      : " + ue.getId());
            System.out.println("  Usuário : " + (ue.getUsuario() != null ? ue.getUsuario().getNome() : "ID " + ue.getUsuario().getId()));
            System.out.println("  Empresa : " + (ue.getEmpresa() != null ? ue.getEmpresa().getNome() : "ID " + ue.getEmpresa().getId()));
            System.out.println("  Cargo   : " + ue.getCargo());
            System.out.println("\n(Dica: pressione ENTER sem digitar nada para manter o cargo atual)");

            String novoCargo = ConsoleUtils.lerTextoOpcional("Novo Cargo [" + ue.getCargo() + "]: ", ue.getCargo());
            ue.setCargo(novoCargo);

            if (ConsoleUtils.confirmar("Confirma a alteração do cargo corporativo?")) {
                boolean sucesso = usuarioEmpresaDAO.atualizar(ue);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Cargo atualizado com sucesso no Oracle!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível atualizar o vínculo corporativo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Alteração cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("atualizar cargo corporativo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void excluirVinculo() {
        ConsoleUtils.imprimirSubcabecalho("REMOÇÃO DE VÍNCULO CORPORATIVO");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do vínculo que deseja remover: ", 1L, null);
            UsuarioEmpresa ue = usuarioEmpresaDAO.buscarPorId(id);

            if (ue == null) {
                ConsoleUtils.imprimirAviso("Nenhum vínculo encontrado com o ID " + id + ".");
                ConsoleUtils.pausar();
                return;
            }

            System.out.println("\nDados do vínculo a ser removido:");
            System.out.println("  ID      : " + ue.getId());
            System.out.println("  Usuário : " + (ue.getUsuario() != null ? ue.getUsuario().getNome() : "ID " + ue.getUsuario().getId()));
            System.out.println("  Empresa : " + (ue.getEmpresa() != null ? ue.getEmpresa().getNome() : "ID " + ue.getEmpresa().getId()));
            System.out.println("  Cargo   : " + ue.getCargo());

            if (ConsoleUtils.confirmar("Confirma a desvinculação deste colaborador?")) {
                boolean sucesso = usuarioEmpresaDAO.excluir(id);
                if (sucesso) {
                    ConsoleUtils.imprimirSucesso("Vínculo corporativo removido com sucesso!");
                } else {
                    ConsoleUtils.imprimirErro("Não foi possível remover o vínculo.");
                }
            } else {
                ConsoleUtils.imprimirAviso("Remoção cancelada.");
            }
        } catch (SQLException e) {
            tratarErroSql("excluir vínculo corporativo", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirTodos() {
        ConsoleUtils.imprimirSubcabecalho("LISTAGEM DE TODOS OS VÍNCULOS CORPORATIVOS (ArrayList)");

        try {
            ArrayList<UsuarioEmpresa> lista = usuarioEmpresaDAO.listarTodos();

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum vínculo corporativo cadastrado.");
            } else {
                imprimirTabelaVinculos(lista);
                System.out.println("  Total de vínculos ativos: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar vínculos", e);
        }

        ConsoleUtils.pausar();
    }

    private static void exibirPorId() {
        ConsoleUtils.imprimirSubcabecalho("CONSULTA DE VÍNCULO POR ID");

        try {
            Long id = ConsoleUtils.lerLong("Informe o ID do vínculo: ", 1L, null);
            UsuarioEmpresa ue = usuarioEmpresaDAO.buscarPorId(id);

            if (ue == null) {
                ConsoleUtils.imprimirAviso("Nenhum vínculo encontrado com o ID " + id + ".");
            } else {
                System.out.println("\n--------------------------------------------------------------------------------");
                System.out.println("  DETALHES DO VÍNCULO CORPORATIVO (ID: " + ue.getId() + ")");
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("  ID Vínculo     : " + ue.getId());
                System.out.println("  Colaborador    : " + (ue.getUsuario() != null ? ue.getUsuario().getNome() : "ID " + ue.getUsuario().getId()));
                System.out.println("  Empresa        : " + (ue.getEmpresa() != null ? ue.getEmpresa().getNome() : "ID " + ue.getEmpresa().getId()));
                System.out.println("  Cargo / Função : " + ue.getCargo());
                System.out.println("  Data de Início : " + ConsoleUtils.formatarData(ue.getDataVinculo()));
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            tratarErroSql("buscar vínculo por ID", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorUsuario() {
        ConsoleUtils.imprimirSubcabecalho("VÍNCULOS CORPORATIVOS DE UM USUÁRIO");

        try {
            Long idUsuario = ConsoleUtils.lerLong("Informe o ID do usuário: ", 1L, null);
            Usuario u = usuarioDAO.buscarPorId(idUsuario);

            if (u == null) {
                ConsoleUtils.imprimirAviso("Usuário com ID " + idUsuario + " não encontrado.");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<UsuarioEmpresa> lista = usuarioEmpresaDAO.listarPorUsuario(idUsuario);

            System.out.println("\nColaborador: " + u.getNome() + " (CPF: " + u.getCpf() + ")");

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Este usuário não possui vínculos corporativos registrados.");
            } else {
                imprimirTabelaVinculos(lista);
                System.out.println("  Total de empresas vinculadas: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar vínculos por usuário", e);
        }

        ConsoleUtils.pausar();
    }

    private static void buscarPorEmpresa() {
        ConsoleUtils.imprimirSubcabecalho("QUADRO DE COLABORADORES POR EMPRESA");

        try {
            Long idEmpresa = ConsoleUtils.lerLong("Informe o ID da Empresa: ", 1L, null);
            Empresa emp = empresaDAO.buscarPorId(idEmpresa);

            if (emp == null) {
                ConsoleUtils.imprimirAviso("Empresa com ID " + idEmpresa + " não encontrada.");
                ConsoleUtils.pausar();
                return;
            }

            ArrayList<UsuarioEmpresa> lista = usuarioEmpresaDAO.listarPorEmpresa(idEmpresa);

            System.out.println("\nEmpresa: " + emp.getNome() + " (CNPJ: " + emp.getCnpj() + ")");

            if (lista.isEmpty()) {
                ConsoleUtils.imprimirAviso("Nenhum colaborador registrado nesta empresa.");
            } else {
                imprimirTabelaVinculos(lista);
                System.out.println("  Total de colaboradores no quadro: " + lista.size());
            }
        } catch (SQLException e) {
            tratarErroSql("listar quadro por empresa", e);
        }

        ConsoleUtils.pausar();
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    private static Usuario selecionarUsuario() throws SQLException {
        ArrayList<Usuario> usuarios = usuarioDAO.listarTodos();
        if (usuarios.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhum usuário cadastrado. Cadastre usuários primeiro!");
            return null;
        }

        System.out.println("\nUsuários cadastrados no sistema:");
        for (Usuario u : usuarios) {
            System.out.printf("  [ID %d] %-30s | CPF: %s\n", u.getId(), u.getNome(), u.getCpf());
        }

        Long id = ConsoleUtils.lerLong("Informe o ID do usuário: ", 1L, null);
        Usuario escolhido = usuarioDAO.buscarPorId(id);
        if (escolhido == null) {
            ConsoleUtils.imprimirAviso("Usuário com ID " + id + " não encontrado.");
            return null;
        }
        return escolhido;
    }

    private static Empresa selecionarEmpresa() throws SQLException {
        ArrayList<Empresa> empresas = empresaDAO.listarTodos();
        if (empresas.isEmpty()) {
            ConsoleUtils.imprimirAviso("Nenhuma empresa cadastrada. Cadastre empresas primeiro!");
            return null;
        }

        System.out.println("\nEmpresas cadastradas no sistema:");
        for (Empresa e : empresas) {
            System.out.printf("  [ID %d] %-35s (CNPJ: %s)\n", e.getId(), e.getNome(), e.getCnpj());
        }

        Long id = ConsoleUtils.lerLong("Informe o ID da empresa: ", 1L, null);
        Empresa escolhida = empresaDAO.buscarPorId(id);
        if (escolhida == null) {
            ConsoleUtils.imprimirAviso("Empresa com ID " + id + " não encontrada.");
            return null;
        }
        return escolhida;
    }

    private static void imprimirTabelaVinculos(ArrayList<UsuarioEmpresa> lista) {
        System.out.println("+--------+---------------------------+---------------------------+---------------------------+------------+");
        System.out.println("| ID     | COLABORADOR               | EMPRESA                   | CARGO / FUNÇÃO            | DATA INÍCIO|");
        System.out.println("+--------+---------------------------+---------------------------+---------------------------+------------+");

        for (UsuarioEmpresa ue : lista) {
            String usuNome = ue.getUsuario() != null ? ue.getUsuario().getNome() : "ID " + ue.getUsuario().getId();
            if (usuNome.length() > 25) {
                usuNome = usuNome.substring(0, 22) + "...";
            }

            String empNome = ue.getEmpresa() != null ? ue.getEmpresa().getNome() : "ID " + ue.getEmpresa().getId();
            if (empNome.length() > 25) {
                empNome = empNome.substring(0, 22) + "...";
            }

            String cargo = ue.getCargo() != null ? ue.getCargo() : "-";
            if (cargo.length() > 25) {
                cargo = cargo.substring(0, 22) + "...";
            }

            System.out.printf("| %-6d | %-25s | %-25s | %-25s | %-10s |\n",
                    ue.getId(),
                    usuNome,
                    empNome,
                    cargo,
                    ConsoleUtils.formatarData(ue.getDataVinculo()));
        }

        System.out.println("+--------+---------------------------+---------------------------+---------------------------+------------+");
    }

    private static void tratarErroSql(String operacao, SQLException e) {
        ConsoleUtils.imprimirErro("Falha ao " + operacao + ": " + e.getMessage() + " (Código Oracle: ORA-" + e.getErrorCode() + ")");
    }
}
