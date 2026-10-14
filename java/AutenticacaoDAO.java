import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Autenticacao.
 */
public class AutenticacaoDAO {

    public Autenticacao inserir(Autenticacao aut) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_AUTENTICACAO.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO AUTENTICACAO (ID, LOGIN, SENHA, ID_AUTENTICACAO_MFA)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de autenticacao.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, aut.getLogin());
            ps.setString(3, aut.getSenha());

            if (aut.getAutenticacaoMultifator() != null && aut.getAutenticacaoMultifator().getId() != null) {
                ps.setLong(4, aut.getAutenticacaoMultifator().getId());
            } else {
                ps.setNull(4, Types.NUMERIC);
            }

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir autenticacao. Linhas afetadas: " + linhasAfetadas);
            }
        }

        aut.setId(novoId);
        return aut;
    }

    public Autenticacao buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, LOGIN, SENHA, ID_AUTENTICACAO_MFA
                  FROM AUTENTICACAO
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAutenticacao(rs);
                }
            }
        }

        return null;
    }

    public Autenticacao buscarPorLogin(String login) throws SQLException {
        final String sql = """
                SELECT ID, LOGIN, SENHA, ID_AUTENTICACAO_MFA
                  FROM AUTENTICACAO
                 WHERE LOWER(LOGIN) = LOWER(?)
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAutenticacao(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Autenticacao> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, LOGIN, SENHA, ID_AUTENTICACAO_MFA
                  FROM AUTENTICACAO
                 ORDER BY ID
                """;

        ArrayList<Autenticacao> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAutenticacao(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(Autenticacao aut) throws SQLException {
        if (aut.getId() == null) {
            throw new IllegalArgumentException("A autenticacao precisa possuir ID para ser atualizada.");
        }

        final String sql = """
                UPDATE AUTENTICACAO
                   SET LOGIN = ?,
                       SENHA = ?,
                       ID_AUTENTICACAO_MFA = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, aut.getLogin());
            ps.setString(2, aut.getSenha());

            if (aut.getAutenticacaoMultifator() != null && aut.getAutenticacaoMultifator().getId() != null) {
                ps.setLong(3, aut.getAutenticacaoMultifator().getId());
            } else {
                ps.setNull(3, Types.NUMERIC);
            }

            ps.setLong(4, aut.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM AUTENTICACAO WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Autenticacao mapearAutenticacao(ResultSet rs) throws SQLException {
        Autenticacao aut = new Autenticacao();
        aut.setId(rs.getLong("ID"));
        aut.setLogin(rs.getString("LOGIN"));
        aut.setSenha(rs.getString("SENHA"));

        long idMfa = rs.getLong("ID_AUTENTICACAO_MFA");
        if (!rs.wasNull()) {
            AutenticacaoMultifator mfa = new AutenticacaoMultifator();
            mfa.setId(idMfa);
            aut.setAutenticacaoMultifator(mfa);
        }

        return aut;
    }
}
