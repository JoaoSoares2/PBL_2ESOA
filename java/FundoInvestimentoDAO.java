import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade FundoInvestimento.
 * Gerencia a integracao entre a tabela base ATIVO e a especializacao FUNDO_INVESTIMENTO.
 */
public class FundoInvestimentoDAO {

    public FundoInvestimento inserir(FundoInvestimento fundo) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_ATIVO.NEXTVAL FROM DUAL";
        final String sqlInsertAtivo = """
                INSERT INTO ATIVO (ID, NOME, VALOR_ATUAL, QUANTIDADE)
                VALUES (?, ?, ?, ?)
                """;
        final String sqlInsertFundo = """
                INSERT INTO FUNDO_INVESTIMENTO (ID_ATIVO, TAXA_ADMINISTRACAO, TIPO_FUNDO)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID para fundo de investimento.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement psAtivo = conn.prepareStatement(sqlInsertAtivo)) {
            psAtivo.setLong(1, novoId);
            psAtivo.setString(2, fundo.getNome());
            psAtivo.setDouble(3, fundo.getValorAtual() != null ? fundo.getValorAtual() : 0.0);
            psAtivo.setDouble(4, fundo.getQuantidade() != null ? fundo.getQuantidade() : 0.0);
            psAtivo.executeUpdate();
        }

        try (PreparedStatement psFundo = conn.prepareStatement(sqlInsertFundo)) {
            psFundo.setLong(1, novoId);
            psFundo.setDouble(2, fundo.getTaxaAdministracao() != null ? fundo.getTaxaAdministracao() : 0.0);
            psFundo.setString(3, fundo.getTipoFundo());
            psFundo.executeUpdate();
        }

        fundo.setId(novoId);
        return fundo;
    }

    public FundoInvestimento buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT A.ID, A.NOME, A.VALOR_ATUAL, A.QUANTIDADE,
                       F.TAXA_ADMINISTRACAO, F.TIPO_FUNDO
                  FROM ATIVO A
                  JOIN FUNDO_INVESTIMENTO F ON F.ID_ATIVO = A.ID
                 WHERE A.ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFundo(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<FundoInvestimento> listarTodos() throws SQLException {
        final String sql = """
                SELECT A.ID, A.NOME, A.VALOR_ATUAL, A.QUANTIDADE,
                       F.TAXA_ADMINISTRACAO, F.TIPO_FUNDO
                  FROM ATIVO A
                  JOIN FUNDO_INVESTIMENTO F ON F.ID_ATIVO = A.ID
                 ORDER BY A.ID
                """;

        ArrayList<FundoInvestimento> lista = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFundo(rs));
            }
        }

        return lista;
    }

    public boolean atualizar(FundoInvestimento fundo) throws SQLException {
        if (fundo.getId() == null) {
            throw new IllegalArgumentException("O fundo de investimento precisa possuir ID para ser atualizado.");
        }

        final String sqlAtivo = """
                UPDATE ATIVO
                   SET NOME = ?,
                       VALOR_ATUAL = ?,
                       QUANTIDADE = ?
                 WHERE ID = ?
                """;

        final String sqlFundo = """
                UPDATE FUNDO_INVESTIMENTO
                   SET TAXA_ADMINISTRACAO = ?,
                       TIPO_FUNDO = ?
                 WHERE ID_ATIVO = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        int linhasAtivo;
        try (PreparedStatement psAtivo = conn.prepareStatement(sqlAtivo)) {
            psAtivo.setString(1, fundo.getNome());
            psAtivo.setDouble(2, fundo.getValorAtual() != null ? fundo.getValorAtual() : 0.0);
            psAtivo.setDouble(3, fundo.getQuantidade() != null ? fundo.getQuantidade() : 0.0);
            psAtivo.setLong(4, fundo.getId());
            linhasAtivo = psAtivo.executeUpdate();
        }

        int linhasFundo;
        try (PreparedStatement psFundo = conn.prepareStatement(sqlFundo)) {
            psFundo.setDouble(1, fundo.getTaxaAdministracao() != null ? fundo.getTaxaAdministracao() : 0.0);
            psFundo.setString(2, fundo.getTipoFundo());
            psFundo.setLong(3, fundo.getId());
            linhasFundo = psFundo.executeUpdate();
        }

        return linhasAtivo == 1 && linhasFundo == 1;
    }

    public boolean excluir(Long id) throws SQLException {
        final String sqlFundo = "DELETE FROM FUNDO_INVESTIMENTO WHERE ID_ATIVO = ?";
        final String sqlAtivo = "DELETE FROM ATIVO WHERE ID = ?";

        Connection conn = ConnectionFactory.getConnection();

        int linhasFundo;
        try (PreparedStatement psFundo = conn.prepareStatement(sqlFundo)) {
            psFundo.setLong(1, id);
            linhasFundo = psFundo.executeUpdate();
        }

        int linhasAtivo;
        try (PreparedStatement psAtivo = conn.prepareStatement(sqlAtivo)) {
            psAtivo.setLong(1, id);
            linhasAtivo = psAtivo.executeUpdate();
        }

        return linhasFundo == 1 && linhasAtivo == 1;
    }

    private FundoInvestimento mapearFundo(ResultSet rs) throws SQLException {
        FundoInvestimento fundo = new FundoInvestimento();
        fundo.setId(rs.getLong("ID"));
        fundo.setNome(rs.getString("NOME"));
        fundo.setValorAtual(rs.getDouble("VALOR_ATUAL"));
        fundo.setQuantidade(rs.getDouble("QUANTIDADE"));
        fundo.setTaxaAdministracao(rs.getDouble("TAXA_ADMINISTRACAO"));
        fundo.setTipoFundo(rs.getString("TIPO_FUNDO"));
        return fundo;
    }
}
