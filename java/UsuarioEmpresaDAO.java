import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade UsuarioEmpresa (relacionamento N:M entre Usuario e Empresa).
 */
public class UsuarioEmpresaDAO {

    public UsuarioEmpresa inserir(UsuarioEmpresa ue) throws SQLException {
        if (ue.getUsuario() == null || ue.getUsuario().getId() == null) {
            throw new IllegalArgumentException("O vinculo precisa referenciar um Usuario com ID.");
        }
        if (ue.getEmpresa() == null || ue.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("O vinculo precisa referenciar uma Empresa com ID.");
        }

        final String sqlProximoId = "SELECT SEQ_USU_EMP.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO USUARIO_EMPRESA (ID, ID_USUARIO, ID_EMPRESA, CARGO, DATA_VINCULO)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de usuario_empresa.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setLong(2, ue.getUsuario().getId());
            ps.setLong(3, ue.getEmpresa().getId());
            ps.setString(4, ue.getCargo());

            Timestamp dataVinculo = ue.getDataVinculo() != null
                    ? new Timestamp(ue.getDataVinculo().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(5, dataVinculo);

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir vinculo usuario-empresa. Linhas afetadas: " + linhasAfetadas);
            }
        }

        ue.setId(novoId);
        return ue;
    }

    public UsuarioEmpresa buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, ID_USUARIO, ID_EMPRESA, CARGO, DATA_VINCULO
                  FROM USUARIO_EMPRESA
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuarioEmpresa(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<UsuarioEmpresa> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, ID_USUARIO, ID_EMPRESA, CARGO, DATA_VINCULO
                  FROM USUARIO_EMPRESA
                 ORDER BY ID
                """;

        ArrayList<UsuarioEmpresa> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuarioEmpresa(rs));
            }
        }

        return lista;
    }

    public ArrayList<UsuarioEmpresa> listarPorUsuario(Long idUsuario) throws SQLException {
        final String sql = """
                SELECT ID, ID_USUARIO, ID_EMPRESA, CARGO, DATA_VINCULO
                  FROM USUARIO_EMPRESA
                 WHERE ID_USUARIO = ?
                 ORDER BY ID
                """;

        ArrayList<UsuarioEmpresa> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearUsuarioEmpresa(rs));
                }
            }
        }

        return lista;
    }

    public ArrayList<UsuarioEmpresa> listarPorEmpresa(Long idEmpresa) throws SQLException {
        final String sql = """
                SELECT ID, ID_USUARIO, ID_EMPRESA, CARGO, DATA_VINCULO
                  FROM USUARIO_EMPRESA
                 WHERE ID_EMPRESA = ?
                 ORDER BY ID
                """;

        ArrayList<UsuarioEmpresa> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearUsuarioEmpresa(rs));
                }
            }
        }

        return lista;
    }

    public boolean atualizar(UsuarioEmpresa ue) throws SQLException {
        if (ue.getId() == null) {
            throw new IllegalArgumentException("O vinculo precisa possuir ID para ser atualizado.");
        }

        final String sql = """
                UPDATE USUARIO_EMPRESA
                   SET CARGO = ?,
                       DATA_VINCULO = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ue.getCargo());

            Timestamp dataVinculo = ue.getDataVinculo() != null
                    ? new Timestamp(ue.getDataVinculo().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(2, dataVinculo);

            ps.setLong(3, ue.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM USUARIO_EMPRESA WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private UsuarioEmpresa mapearUsuarioEmpresa(ResultSet rs) throws SQLException {
        UsuarioEmpresa ue = new UsuarioEmpresa();
        ue.setId(rs.getLong("ID"));

        long idUsuario = rs.getLong("ID_USUARIO");
        if (!rs.wasNull()) {
            Usuario u = new Usuario();
            u.setId(idUsuario);
            ue.setUsuario(u);
        }

        long idEmpresa = rs.getLong("ID_EMPRESA");
        if (!rs.wasNull()) {
            Empresa e = new Empresa();
            e.setId(idEmpresa);
            ue.setEmpresa(e);
        }

        ue.setCargo(rs.getString("CARGO"));

        Timestamp ts = rs.getTimestamp("DATA_VINCULO");
        if (ts != null) {
            ue.setDataVinculo(new java.util.Date(ts.getTime()));
        }

        return ue;
    }
}
