import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade AutenticacaoMultifator.
 */
public class AutenticacaoMultifatorDAO {

    public AutenticacaoMultifator inserir(AutenticacaoMultifator mfa) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_AUT_MFA.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO AUTENTICACAO_MULTIFATOR (ID, CODIGO, EXPIRACAO)
                VALUES (?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de autenticacao multifator.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, mfa.getCodigo());

            if (mfa.getExpiracao() != null) {
                ps.setTimestamp(3, new Timestamp(mfa.getExpiracao().getTime()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir autenticacao multifator. Linhas afetadas: " + linhasAfetadas);
            }
        }

        mfa.setId(novoId);
        return mfa;
    }

    public AutenticacaoMultifator buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, CODIGO, EXPIRACAO
                  FROM AUTENTICACAO_MULTIFATOR
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAutenticacaoMultifator(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<AutenticacaoMultifator> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, CODIGO, EXPIRACAO
                  FROM AUTENTICACAO_MULTIFATOR
                 ORDER BY ID
                """;

        ArrayList<AutenticacaoMultifator> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAutenticacaoMultifator(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(AutenticacaoMultifator mfa) throws SQLException {
        if (mfa.getId() == null) {
            throw new IllegalArgumentException("A autenticacao multifator precisa possuir ID para ser atualizada.");
        }

        final String sql = """
                UPDATE AUTENTICACAO_MULTIFATOR
                   SET CODIGO = ?,
                       EXPIRACAO = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mfa.getCodigo());

            if (mfa.getExpiracao() != null) {
                ps.setTimestamp(2, new Timestamp(mfa.getExpiracao().getTime()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            ps.setLong(3, mfa.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM AUTENTICACAO_MULTIFATOR WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private AutenticacaoMultifator mapearAutenticacaoMultifator(ResultSet rs) throws SQLException {
        AutenticacaoMultifator mfa = new AutenticacaoMultifator();
        mfa.setId(rs.getLong("ID"));
        mfa.setCodigo(rs.getString("CODIGO"));

        Timestamp ts = rs.getTimestamp("EXPIRACAO");
        if (ts != null) {
            mfa.setExpiracao(new java.util.Date(ts.getTime()));
        }

        return mfa;
    }
}
