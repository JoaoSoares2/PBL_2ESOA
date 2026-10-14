import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade PerfilInvestidor.
 */
public class PerfilInvestidorDAO {

    public PerfilInvestidor inserir(PerfilInvestidor perfil) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_PERFIL_INV.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO PERFIL_INVESTIDOR (ID, NOME_PERFIL, DESCRICAO, NIVEL_RISCO)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de perfil do investidor.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, perfil.getNomePerfil());
            ps.setString(3, perfil.getDescricao());
            ps.setString(4, perfil.getNivelRisco());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir perfil do investidor. Linhas afetadas: " + linhasAfetadas);
            }
        }

        perfil.setId(novoId);
        return perfil;
    }

    public PerfilInvestidor buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, NOME_PERFIL, DESCRICAO, NIVEL_RISCO
                  FROM PERFIL_INVESTIDOR
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPerfilInvestidor(rs);
                }
            }
        }

        return null;
    }

    public PerfilInvestidor buscarPorNome(String nomePerfil) throws SQLException {
        final String sql = """
                SELECT ID, NOME_PERFIL, DESCRICAO, NIVEL_RISCO
                  FROM PERFIL_INVESTIDOR
                 WHERE LOWER(NOME_PERFIL) = LOWER(?)
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomePerfil);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPerfilInvestidor(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<PerfilInvestidor> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, NOME_PERFIL, DESCRICAO, NIVEL_RISCO
                  FROM PERFIL_INVESTIDOR
                 ORDER BY ID
                """;

        ArrayList<PerfilInvestidor> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPerfilInvestidor(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(PerfilInvestidor perfil) throws SQLException {
        if (perfil.getId() == null) {
            throw new IllegalArgumentException("O perfil precisa possuir ID para ser atualizado.");
        }

        final String sql = """
                UPDATE PERFIL_INVESTIDOR
                   SET NOME_PERFIL = ?,
                       DESCRICAO = ?,
                       NIVEL_RISCO = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perfil.getNomePerfil());
            ps.setString(2, perfil.getDescricao());
            ps.setString(3, perfil.getNivelRisco());
            ps.setLong(4, perfil.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM PERFIL_INVESTIDOR WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private PerfilInvestidor mapearPerfilInvestidor(ResultSet rs) throws SQLException {
        PerfilInvestidor perfil = new PerfilInvestidor();
        perfil.setId(rs.getLong("ID"));
        perfil.setNomePerfil(rs.getString("NOME_PERFIL"));
        perfil.setDescricao(rs.getString("DESCRICAO"));
        perfil.setNivelRisco(rs.getString("NIVEL_RISCO"));
        return perfil;
    }
}
