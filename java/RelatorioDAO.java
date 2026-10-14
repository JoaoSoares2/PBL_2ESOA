import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Relatorio (incluindo RelatorioAnual e RelatorioMensal).
 */
public class RelatorioDAO {

    public Relatorio inserir(Relatorio relatorio) throws SQLException {
        if (relatorio.getUsuario() == null || relatorio.getUsuario().getId() == null) {
            throw new IllegalArgumentException("O relatorio precisa estar vinculado a um Usuario com ID.");
        }

        final String sqlProximoId = "SELECT SEQ_RELATORIO.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO RELATORIO (ID, TIPO, DATA_GERACAO, CONTEUDO, ID_USUARIO)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de relatorio.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, relatorio.getTipo());

            Timestamp dataGeracao = relatorio.getDataGeracao() != null
                    ? new Timestamp(relatorio.getDataGeracao().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(3, dataGeracao);

            ps.setString(4, relatorio.getConteudo());
            ps.setLong(5, relatorio.getUsuario().getId());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir relatorio. Linhas afetadas: " + linhasAfetadas);
            }
        }

        relatorio.setId(novoId);
        return relatorio;
    }

    public Relatorio buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, TIPO, DATA_GERACAO, CONTEUDO, ID_USUARIO
                  FROM RELATORIO
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearRelatorio(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Relatorio> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, TIPO, DATA_GERACAO, CONTEUDO, ID_USUARIO
                  FROM RELATORIO
                 ORDER BY ID
                """;

        ArrayList<Relatorio> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearRelatorio(rs));
            }
        }

        return lista;
    }

    public ArrayList<Relatorio> listarPorUsuario(Long idUsuario) throws SQLException {
        final String sql = """
                SELECT ID, TIPO, DATA_GERACAO, CONTEUDO, ID_USUARIO
                  FROM RELATORIO
                 WHERE ID_USUARIO = ?
                 ORDER BY DATA_GERACAO
                """;

        ArrayList<Relatorio> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearRelatorio(rs));
                }
            }
        }

        return lista;
    }

    public boolean atualizar(Relatorio relatorio) throws SQLException {
        if (relatorio.getId() == null) {
            throw new IllegalArgumentException("O relatorio precisa possuir ID para ser atualizado.");
        }
        if (relatorio.getUsuario() == null || relatorio.getUsuario().getId() == null) {
            throw new IllegalArgumentException("O relatorio precisa estar vinculado a um usuario.");
        }

        final String sql = """
                UPDATE RELATORIO
                   SET TIPO = ?,
                       DATA_GERACAO = ?,
                       CONTEUDO = ?,
                       ID_USUARIO = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, relatorio.getTipo());

            Timestamp dataGeracao = relatorio.getDataGeracao() != null
                    ? new Timestamp(relatorio.getDataGeracao().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(2, dataGeracao);

            ps.setString(3, relatorio.getConteudo());
            ps.setLong(4, relatorio.getUsuario().getId());
            ps.setLong(5, relatorio.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM RELATORIO WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Relatorio mapearRelatorio(ResultSet rs) throws SQLException {
        String tipo = rs.getString("TIPO");
        Relatorio relatorio;

        if ("ANUAL".equalsIgnoreCase(tipo)) {
            relatorio = new RelatorioAnual();
        } else if ("MENSAL".equalsIgnoreCase(tipo)) {
            relatorio = new RelatorioMensal();
        } else {
            relatorio = new Relatorio(tipo);
        }

        relatorio.setId(rs.getLong("ID"));
        relatorio.setTipo(tipo);

        Timestamp ts = rs.getTimestamp("DATA_GERACAO");
        if (ts != null) {
            relatorio.setDataGeracao(new java.util.Date(ts.getTime()));
        }

        relatorio.setConteudo(rs.getString("CONTEUDO"));

        long idUsuario = rs.getLong("ID_USUARIO");
        if (!rs.wasNull()) {
            Usuario usuario = new Usuario();
            usuario.setId(idUsuario);
            relatorio.setUsuario(usuario);
        }

        return relatorio;
    }
}
