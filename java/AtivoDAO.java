import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Ativo (tabela base).
 */
public class AtivoDAO {

    public Ativo inserir(Ativo ativo) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_ATIVO.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO ATIVO (ID, NOME, VALOR_ATUAL, QUANTIDADE)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de ativo.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, ativo.getNome());
            ps.setDouble(3, ativo.getValorAtual() != null ? ativo.getValorAtual() : 0.0);
            ps.setDouble(4, ativo.getQuantidade() != null ? ativo.getQuantidade() : 0.0);

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir ativo. Linhas afetadas: " + linhasAfetadas);
            }
        }

        ativo.setId(novoId);
        return ativo;
    }

    public Ativo buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, NOME, VALOR_ATUAL, QUANTIDADE
                  FROM ATIVO
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAtivo(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Ativo> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, NOME, VALOR_ATUAL, QUANTIDADE
                  FROM ATIVO
                 ORDER BY ID
                """;

        ArrayList<Ativo> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAtivo(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(Ativo ativo) throws SQLException {
        if (ativo.getId() == null) {
            throw new IllegalArgumentException("O ativo precisa possuir ID para ser atualizado.");
        }

        final String sql = """
                UPDATE ATIVO
                   SET NOME = ?,
                       VALOR_ATUAL = ?,
                       QUANTIDADE = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ativo.getNome());
            ps.setDouble(2, ativo.getValorAtual() != null ? ativo.getValorAtual() : 0.0);
            ps.setDouble(3, ativo.getQuantidade() != null ? ativo.getQuantidade() : 0.0);
            ps.setLong(4, ativo.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM ATIVO WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Ativo mapearAtivo(ResultSet rs) throws SQLException {
        Ativo ativo = new Ativo();
        ativo.setId(rs.getLong("ID"));
        ativo.setNome(rs.getString("NOME"));
        ativo.setValorAtual(rs.getDouble("VALOR_ATUAL"));
        ativo.setQuantidade(rs.getDouble("QUANTIDADE"));
        return ativo;
    }
}
