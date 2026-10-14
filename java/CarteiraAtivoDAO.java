import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade CarteiraAtivo (relacionamento N:M entre Carteira e Ativo).
 */
public class CarteiraAtivoDAO {

    public CarteiraAtivo inserir(CarteiraAtivo ca) throws SQLException {
        if (ca.getCarteira() == null || ca.getCarteira().getId() == null) {
            throw new IllegalArgumentException("O registro precisa referenciar uma Carteira com ID.");
        }
        if (ca.getAtivo() == null || ca.getAtivo().getId() == null) {
            throw new IllegalArgumentException("O registro precisa referenciar um Ativo com ID.");
        }

        final String sqlProximoId = "SELECT SEQ_CART_ATIVO.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO CARTEIRA_ATIVO (ID, ID_CARTEIRA, ID_ATIVO, QUANTIDADE, PRECO_MEDIO)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de carteira_ativo.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setLong(2, ca.getCarteira().getId());
            ps.setLong(3, ca.getAtivo().getId());
            ps.setDouble(4, ca.getQuantidade() != null ? ca.getQuantidade() : 0.0);
            ps.setDouble(5, ca.getPrecoMedio() != null ? ca.getPrecoMedio() : 0.0);

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir carteira_ativo. Linhas afetadas: " + linhasAfetadas);
            }
        }

        ca.setId(novoId);
        return ca;
    }

    public CarteiraAtivo buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT CA.ID, CA.ID_CARTEIRA, CA.ID_ATIVO, CA.QUANTIDADE, CA.PRECO_MEDIO,
                       A.NOME AS ATIVO_NOME, A.VALOR_ATUAL AS ATIVO_VALOR_ATUAL, A.QUANTIDADE AS ATIVO_QUANTIDADE,
                       C.NOME AS CARTEIRA_NOME
                  FROM CARTEIRA_ATIVO CA
                  JOIN ATIVO A ON A.ID = CA.ID_ATIVO
                  JOIN CARTEIRA C ON C.ID = CA.ID_CARTEIRA
                 WHERE CA.ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCarteiraAtivoCompleto(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<CarteiraAtivo> listarTodos() throws SQLException {
        final String sql = """
                SELECT CA.ID, CA.ID_CARTEIRA, CA.ID_ATIVO, CA.QUANTIDADE, CA.PRECO_MEDIO,
                       A.NOME AS ATIVO_NOME, A.VALOR_ATUAL AS ATIVO_VALOR_ATUAL, A.QUANTIDADE AS ATIVO_QUANTIDADE,
                       C.NOME AS CARTEIRA_NOME
                  FROM CARTEIRA_ATIVO CA
                  JOIN ATIVO A ON A.ID = CA.ID_ATIVO
                  JOIN CARTEIRA C ON C.ID = CA.ID_CARTEIRA
                 ORDER BY CA.ID
                """;

        ArrayList<CarteiraAtivo> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCarteiraAtivoCompleto(rs));
            }
        }

        return lista;
    }

    public ArrayList<CarteiraAtivo> listarPorCarteira(Long idCarteira) throws SQLException {
        final String sql = """
                SELECT CA.ID, CA.ID_CARTEIRA, CA.ID_ATIVO, CA.QUANTIDADE, CA.PRECO_MEDIO,
                       A.NOME AS ATIVO_NOME, A.VALOR_ATUAL AS ATIVO_VALOR_ATUAL, A.QUANTIDADE AS ATIVO_QUANTIDADE,
                       C.NOME AS CARTEIRA_NOME
                  FROM CARTEIRA_ATIVO CA
                  JOIN ATIVO A ON A.ID = CA.ID_ATIVO
                  JOIN CARTEIRA C ON C.ID = CA.ID_CARTEIRA
                 WHERE CA.ID_CARTEIRA = ?
                 ORDER BY CA.ID
                """;

        ArrayList<CarteiraAtivo> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCarteira);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCarteiraAtivoCompleto(rs));
                }
            }
        }

        return lista;
    }

    public ArrayList<CarteiraAtivo> listarPorAtivo(Long idAtivo) throws SQLException {
        final String sql = """
                SELECT CA.ID, CA.ID_CARTEIRA, CA.ID_ATIVO, CA.QUANTIDADE, CA.PRECO_MEDIO,
                       A.NOME AS ATIVO_NOME, A.VALOR_ATUAL AS ATIVO_VALOR_ATUAL, A.QUANTIDADE AS ATIVO_QUANTIDADE,
                       C.NOME AS CARTEIRA_NOME
                  FROM CARTEIRA_ATIVO CA
                  JOIN ATIVO A ON A.ID = CA.ID_ATIVO
                  JOIN CARTEIRA C ON C.ID = CA.ID_CARTEIRA
                 WHERE CA.ID_ATIVO = ?
                 ORDER BY CA.ID
                """;

        ArrayList<CarteiraAtivo> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idAtivo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCarteiraAtivoCompleto(rs));
                }
            }
        }

        return lista;
    }

    public boolean atualizar(CarteiraAtivo ca) throws SQLException {
        if (ca.getId() == null) {
            throw new IllegalArgumentException("O registro precisa possuir ID para ser atualizado.");
        }

        final String sql = """
                UPDATE CARTEIRA_ATIVO
                   SET QUANTIDADE = ?,
                       PRECO_MEDIO = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, ca.getQuantidade() != null ? ca.getQuantidade() : 0.0);
            ps.setDouble(2, ca.getPrecoMedio() != null ? ca.getPrecoMedio() : 0.0);
            ps.setLong(3, ca.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM CARTEIRA_ATIVO WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private CarteiraAtivo mapearCarteiraAtivoCompleto(ResultSet rs) throws SQLException {
        CarteiraAtivo ca = new CarteiraAtivo();
        ca.setId(rs.getLong("ID"));
        ca.setQuantidade(rs.getDouble("QUANTIDADE"));
        ca.setPrecoMedio(rs.getDouble("PRECO_MEDIO"));

        Carteira c = new Carteira();
        c.setId(rs.getLong("ID_CARTEIRA"));
        c.setNome(rs.getString("CARTEIRA_NOME"));
        ca.setCarteira(c);

        Ativo a = new Ativo();
        a.setId(rs.getLong("ID_ATIVO"));
        a.setNome(rs.getString("ATIVO_NOME"));
        a.setValorAtual(rs.getDouble("ATIVO_VALOR_ATUAL"));
        a.setQuantidade(rs.getDouble("ATIVO_QUANTIDADE"));
        ca.setAtivo(a);

        return ca;
    }
}
