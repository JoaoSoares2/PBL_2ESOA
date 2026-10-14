import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Transacao.
 */
public class TransacaoDAO {

    public Transacao inserir(Transacao transacao) throws SQLException {
        if (transacao.getCarteira() == null || transacao.getCarteira().getId() == null) {
            throw new IllegalArgumentException("A transacao precisa estar vinculada a uma Carteira com ID.");
        }

        final String sqlProximoId = "SELECT SEQ_TRANSACAO.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO TRANSACAO (ID, TIPO, VALOR, DATA_TRANSACAO, DESCRICAO, ID_ATIVO, ID_CARTEIRA)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de transacao.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, transacao.getTipo());
            ps.setDouble(3, transacao.getValor() != null ? transacao.getValor() : 0.0);

            Timestamp dataTransacao = transacao.getData() != null
                    ? new Timestamp(transacao.getData().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(4, dataTransacao);

            ps.setString(5, transacao.getDescricao());

            if (transacao.getAtivo() != null && transacao.getAtivo().getId() != null) {
                ps.setLong(6, transacao.getAtivo().getId());
            } else {
                ps.setNull(6, Types.NUMERIC);
            }

            ps.setLong(7, transacao.getCarteira().getId());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir transacao. Linhas afetadas: " + linhasAfetadas);
            }
        }

        transacao.setId(novoId);
        return transacao;
    }

    public Transacao buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, TIPO, VALOR, DATA_TRANSACAO, DESCRICAO, ID_ATIVO, ID_CARTEIRA
                  FROM TRANSACAO
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTransacao(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Transacao> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, TIPO, VALOR, DATA_TRANSACAO, DESCRICAO, ID_ATIVO, ID_CARTEIRA
                  FROM TRANSACAO
                 ORDER BY ID
                """;

        ArrayList<Transacao> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearTransacao(rs));
            }
        }

        return lista;
    }

    public ArrayList<Transacao> listarPorCarteira(Long idCarteira) throws SQLException {
        final String sql = """
                SELECT ID, TIPO, VALOR, DATA_TRANSACAO, DESCRICAO, ID_ATIVO, ID_CARTEIRA
                  FROM TRANSACAO
                 WHERE ID_CARTEIRA = ?
                 ORDER BY DATA_TRANSACAO
                """;

        ArrayList<Transacao> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCarteira);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearTransacao(rs));
                }
            }
        }

        return lista;
    }

    public boolean atualizar(Transacao transacao) throws SQLException {
        if (transacao.getId() == null) {
            throw new IllegalArgumentException("A transacao precisa possuir ID para ser atualizada.");
        }
        if (transacao.getCarteira() == null || transacao.getCarteira().getId() == null) {
            throw new IllegalArgumentException("A transacao precisa estar associada a uma carteira.");
        }

        final String sql = """
                UPDATE TRANSACAO
                   SET TIPO = ?,
                       VALOR = ?,
                       DATA_TRANSACAO = ?,
                       DESCRICAO = ?,
                       ID_ATIVO = ?,
                       ID_CARTEIRA = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transacao.getTipo());
            ps.setDouble(2, transacao.getValor() != null ? transacao.getValor() : 0.0);

            Timestamp dataTransacao = transacao.getData() != null
                    ? new Timestamp(transacao.getData().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(3, dataTransacao);

            ps.setString(4, transacao.getDescricao());

            if (transacao.getAtivo() != null && transacao.getAtivo().getId() != null) {
                ps.setLong(5, transacao.getAtivo().getId());
            } else {
                ps.setNull(5, Types.NUMERIC);
            }

            ps.setLong(6, transacao.getCarteira().getId());
            ps.setLong(7, transacao.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM TRANSACAO WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Transacao mapearTransacao(ResultSet rs) throws SQLException {
        Transacao t = new Transacao();
        t.setId(rs.getLong("ID"));
        t.setTipo(rs.getString("TIPO"));
        t.setValor(rs.getDouble("VALOR"));

        Timestamp ts = rs.getTimestamp("DATA_TRANSACAO");
        if (ts != null) {
            t.setData(new java.util.Date(ts.getTime()));
        }

        t.setDescricao(rs.getString("DESCRICAO"));

        long idAtivo = rs.getLong("ID_ATIVO");
        if (!rs.wasNull()) {
            Ativo ativo = new Ativo();
            ativo.setId(idAtivo);
            t.setAtivo(ativo);
        }

        long idCarteira = rs.getLong("ID_CARTEIRA");
        if (!rs.wasNull()) {
            Carteira carteira = new Carteira();
            carteira.setId(idCarteira);
            t.setCarteira(carteira);
        }

        return t;
    }
}
