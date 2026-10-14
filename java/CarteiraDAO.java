import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Carteira.
 */
public class CarteiraDAO {

    public Carteira inserir(Carteira carteira) throws SQLException {
        if (carteira.getEmpresa() == null || carteira.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("A carteira precisa estar vinculada a uma Empresa com ID.");
        }

        final String sqlProximoId = "SELECT SEQ_CARTEIRA.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO CARTEIRA (ID, NOME, SALDO_TOTAL, DATA_CRIACAO, ID_EMPRESA)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de carteira.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, carteira.getNome());
            ps.setDouble(3, carteira.getSaldoTotal() != null ? carteira.getSaldoTotal() : 0.0);

            Timestamp dataCriacao = carteira.getDataCriacao() != null
                    ? new Timestamp(carteira.getDataCriacao().getTime())
                    : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(4, dataCriacao);

            ps.setLong(5, carteira.getEmpresa().getId());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir carteira. Linhas afetadas: " + linhasAfetadas);
            }
        }

        carteira.setId(novoId);
        return carteira;
    }

    public Carteira buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, NOME, SALDO_TOTAL, DATA_CRIACAO, ID_EMPRESA
                  FROM CARTEIRA
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCarteira(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Carteira> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, NOME, SALDO_TOTAL, DATA_CRIACAO, ID_EMPRESA
                  FROM CARTEIRA
                 ORDER BY ID
                """;

        ArrayList<Carteira> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCarteira(rs));
            }
        }

        return lista;
    }

    public ArrayList<Carteira> listarPorEmpresa(Long idEmpresa) throws SQLException {
        final String sql = """
                SELECT ID, NOME, SALDO_TOTAL, DATA_CRIACAO, ID_EMPRESA
                  FROM CARTEIRA
                 WHERE ID_EMPRESA = ?
                 ORDER BY ID
                """;

        ArrayList<Carteira> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCarteira(rs));
                }
            }
        }

        return lista;
    }

    public boolean atualizar(Carteira carteira) throws SQLException {
        if (carteira.getId() == null) {
            throw new IllegalArgumentException("A carteira precisa possuir ID para ser atualizada.");
        }
        if (carteira.getEmpresa() == null || carteira.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("A carteira precisa estar associada a uma empresa.");
        }

        final String sql = """
                UPDATE CARTEIRA
                   SET NOME = ?,
                       SALDO_TOTAL = ?,
                       ID_EMPRESA = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, carteira.getNome());
            ps.setDouble(2, carteira.getSaldoTotal() != null ? carteira.getSaldoTotal() : 0.0);
            ps.setLong(3, carteira.getEmpresa().getId());
            ps.setLong(4, carteira.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM CARTEIRA WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Carteira mapearCarteira(ResultSet rs) throws SQLException {
        Carteira carteira = new Carteira();
        carteira.setId(rs.getLong("ID"));
        carteira.setNome(rs.getString("NOME"));
        carteira.setSaldoTotal(rs.getDouble("SALDO_TOTAL"));

        Timestamp ts = rs.getTimestamp("DATA_CRIACAO");
        if (ts != null) {
            carteira.setDataCriacao(new java.util.Date(ts.getTime()));
        }

        long idEmpresa = rs.getLong("ID_EMPRESA");
        if (!rs.wasNull()) {
            Empresa emp = new Empresa();
            emp.setId(idEmpresa);
            carteira.setEmpresa(emp);
        }

        return carteira;
    }
}
