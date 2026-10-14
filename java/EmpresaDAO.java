import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Empresa.
 */
public class EmpresaDAO {

    public Empresa inserir(Empresa empresa) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_EMPRESA.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO EMPRESA (ID, NOME, CNPJ)
                VALUES (?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de empresa.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, empresa.getNome());
            ps.setString(3, empresa.getCnpj());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir empresa. Linhas afetadas: " + linhasAfetadas);
            }
        }

        empresa.setId(novoId);
        return empresa;
    }

    public Empresa buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, NOME, CNPJ
                  FROM EMPRESA
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        }

        return null;
    }

    public Empresa buscarPorCnpj(String cnpj) throws SQLException {
        final String sql = """
                SELECT ID, NOME, CNPJ
                  FROM EMPRESA
                 WHERE CNPJ = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnpj);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Empresa> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, NOME, CNPJ
                  FROM EMPRESA
                 ORDER BY ID
                """;

        ArrayList<Empresa> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpresa(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(Empresa empresa) throws SQLException {
        if (empresa.getId() == null) {
            throw new IllegalArgumentException("A empresa precisa possuir ID para ser atualizada.");
        }

        final String sql = """
                UPDATE EMPRESA
                   SET NOME = ?,
                       CNPJ = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empresa.getNome());
            ps.setString(2, empresa.getCnpj());
            ps.setLong(3, empresa.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM EMPRESA WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa();
        empresa.setId(rs.getLong("ID"));
        empresa.setNome(rs.getString("NOME"));
        empresa.setCnpj(rs.getString("CNPJ"));
        return empresa;
    }
}
