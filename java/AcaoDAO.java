import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Acao.
 * Gerencia a integracao entre a tabela base ATIVO e a especializacao ACAO.
 */
public class AcaoDAO {

    public Acao inserir(Acao acao) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_ATIVO.NEXTVAL FROM DUAL";
        final String sqlInsertAtivo = """
                INSERT INTO ATIVO (ID, NOME, VALOR_ATUAL, QUANTIDADE)
                VALUES (?, ?, ?, ?)
                """;
        final String sqlInsertAcao = """
                INSERT INTO ACAO (ID_ATIVO, TICKER, BOLSA, VARIACAO_DIARIA)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID para acao.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement psAtivo = conn.prepareStatement(sqlInsertAtivo)) {
            psAtivo.setLong(1, novoId);
            psAtivo.setString(2, acao.getNome());
            psAtivo.setDouble(3, acao.getValorAtual() != null ? acao.getValorAtual() : 0.0);
            psAtivo.setDouble(4, acao.getQuantidade() != null ? acao.getQuantidade() : 0.0);
            psAtivo.executeUpdate();
        }

        try (PreparedStatement psAcao = conn.prepareStatement(sqlInsertAcao)) {
            psAcao.setLong(1, novoId);
            psAcao.setString(2, acao.getTicker());
            psAcao.setString(3, acao.getBolsa());
            psAcao.setDouble(4, acao.getVariacaoDiaria() != null ? acao.getVariacaoDiaria() : 0.0);
            psAcao.executeUpdate();
        }

        acao.setId(novoId);
        return acao;
    }

    public Acao buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT A.ID, A.NOME, A.VALOR_ATUAL, A.QUANTIDADE,
                       AC.TICKER, AC.BOLSA, AC.VARIACAO_DIARIA
                  FROM ATIVO A
                  JOIN ACAO AC ON AC.ID_ATIVO = A.ID
                 WHERE A.ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAcao(rs);
                }
            }
        }

        return null;
    }

    public Acao buscarPorTicker(String ticker) throws SQLException {
        final String sql = """
                SELECT A.ID, A.NOME, A.VALOR_ATUAL, A.QUANTIDADE,
                       AC.TICKER, AC.BOLSA, AC.VARIACAO_DIARIA
                  FROM ATIVO A
                  JOIN ACAO AC ON AC.ID_ATIVO = A.ID
                 WHERE UPPER(AC.TICKER) = UPPER(?)
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticker);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAcao(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Acao> listarTodos() throws SQLException {
        final String sql = """
                SELECT A.ID, A.NOME, A.VALOR_ATUAL, A.QUANTIDADE,
                       AC.TICKER, AC.BOLSA, AC.VARIACAO_DIARIA
                  FROM ATIVO A
                  JOIN ACAO AC ON AC.ID_ATIVO = A.ID
                 ORDER BY A.ID
                """;

        ArrayList<Acao> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAcao(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(Acao acao) throws SQLException {
        if (acao.getId() == null) {
            throw new IllegalArgumentException("A acao precisa possuir ID para ser atualizada.");
        }

        final String sqlAtivo = """
                UPDATE ATIVO
                   SET NOME = ?,
                       VALOR_ATUAL = ?,
                       QUANTIDADE = ?
                 WHERE ID = ?
                """;

        final String sqlAcao = """
                UPDATE ACAO
                   SET TICKER = ?,
                       BOLSA = ?,
                       VARIACAO_DIARIA = ?
                 WHERE ID_ATIVO = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        int linhasAtivo;
        try (PreparedStatement psAtivo = conn.prepareStatement(sqlAtivo)) {
            psAtivo.setString(1, acao.getNome());
            psAtivo.setDouble(2, acao.getValorAtual() != null ? acao.getValorAtual() : 0.0);
            psAtivo.setDouble(3, acao.getQuantidade() != null ? acao.getQuantidade() : 0.0);
            psAtivo.setLong(4, acao.getId());
            linhasAtivo = psAtivo.executeUpdate();
        }

        int linhasAcao;
        try (PreparedStatement psAcao = conn.prepareStatement(sqlAcao)) {
            psAcao.setString(1, acao.getTicker());
            psAcao.setString(2, acao.getBolsa());
            psAcao.setDouble(3, acao.getVariacaoDiaria() != null ? acao.getVariacaoDiaria() : 0.0);
            psAcao.setLong(4, acao.getId());
            linhasAcao = psAcao.executeUpdate();
        }

        return linhasAtivo == 1 && linhasAcao == 1;
    }

    public boolean excluir(Long id) throws SQLException {
        final String sqlAcao = "DELETE FROM ACAO WHERE ID_ATIVO = ?";
        final String sqlAtivo = "DELETE FROM ATIVO WHERE ID = ?";

        Connection conn = ConnectionFactory.getConnection();

        int linhasAcao;
        try (PreparedStatement psAcao = conn.prepareStatement(sqlAcao)) {
            psAcao.setLong(1, id);
            linhasAcao = psAcao.executeUpdate();
        }

        int linhasAtivo;
        try (PreparedStatement psAtivo = conn.prepareStatement(sqlAtivo)) {
            psAtivo.setLong(1, id);
            linhasAtivo = psAtivo.executeUpdate();
        }

        return linhasAcao == 1 && linhasAtivo == 1;
    }

    private Acao mapearAcao(ResultSet rs) throws SQLException {
        Acao acao = new Acao();
        acao.setId(rs.getLong("ID"));
        acao.setNome(rs.getString("NOME"));
        acao.setValorAtual(rs.getDouble("VALOR_ATUAL"));
        acao.setQuantidade(rs.getDouble("QUANTIDADE"));
        acao.setTicker(rs.getString("TICKER"));
        acao.setBolsa(rs.getString("BOLSA"));
        acao.setVariacaoDiaria(rs.getDouble("VARIACAO_DIARIA"));
        return acao;
    }
}
