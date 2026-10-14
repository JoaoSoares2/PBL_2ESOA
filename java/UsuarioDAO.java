import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

/**
 * DAO responsavel pelo CRUD da entidade Usuario.
 * Toda a logica SQL da entidade fica concentrada nesta classe.
 */
public class UsuarioDAO {

    public Usuario inserir(Usuario usuario) throws SQLException {
        final String sqlProximoId = "SELECT SEQ_USUARIO.NEXTVAL FROM DUAL";
        final String sqlInsert = """
                INSERT INTO USUARIO (ID, NOME, EMAIL, SENHA, CPF, TELEFONE, ID_AUTENTICACAO, ID_PERFIL_INVESTIDOR)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Connection conn = ConnectionFactory.getConnection();
        long novoId;

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sqlProximoId)) {

            if (!rs.next()) {
                throw new SQLException("Nao foi possivel obter o proximo ID de usuario.");
            }
            novoId = rs.getLong(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, novoId);
            ps.setString(2, usuario.getNome());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getSenha());
            ps.setString(5, usuario.getCpf());
            ps.setString(6, usuario.getTelefone());

            if (usuario.getAutenticacao() != null && usuario.getAutenticacao().getId() != null) {
                ps.setLong(7, usuario.getAutenticacao().getId());
            } else {
                ps.setNull(7, Types.NUMERIC);
            }

            if (usuario.getPerfilInvestidor() != null && usuario.getPerfilInvestidor().getId() != null) {
                ps.setLong(8, usuario.getPerfilInvestidor().getId());
            } else {
                ps.setNull(8, Types.NUMERIC);
            }

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas != 1) {
                throw new SQLException("Falha ao inserir usuario. Linhas afetadas: " + linhasAfetadas);
            }
        }

        usuario.setId(novoId);
        return usuario;
    }

    public Usuario buscarPorId(Long id) throws SQLException {
        final String sql = """
                SELECT ID, NOME, EMAIL, SENHA, CPF, TELEFONE, ID_AUTENTICACAO, ID_PERFIL_INVESTIDOR
                  FROM USUARIO
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }

        return null;
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        final String sql = """
                SELECT ID, NOME, EMAIL, SENHA, CPF, TELEFONE, ID_AUTENTICACAO, ID_PERFIL_INVESTIDOR
                  FROM USUARIO
                 WHERE LOWER(EMAIL) = LOWER(?)
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }

        return null;
    }

    public Usuario buscarPorCpf(String cpf) throws SQLException {
        final String sql = """
                SELECT ID, NOME, EMAIL, SENHA, CPF, TELEFONE, ID_AUTENTICACAO, ID_PERFIL_INVESTIDOR
                  FROM USUARIO
                 WHERE CPF = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Usuario> listarTodos() throws SQLException {
        final String sql = """
                SELECT ID, NOME, EMAIL, SENHA, CPF, TELEFONE, ID_AUTENTICACAO, ID_PERFIL_INVESTIDOR
                  FROM USUARIO
                 ORDER BY ID
                """;

        ArrayList<Usuario> usuarios = new ArrayList<>();
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    public boolean atualizar(Usuario usuario) throws SQLException {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("O usuario precisa possuir ID para ser atualizado.");
        }

        final String sql = """
                UPDATE USUARIO
                   SET NOME = ?,
                       EMAIL = ?,
                       SENHA = ?,
                       CPF = ?,
                       TELEFONE = ?,
                       ID_AUTENTICACAO = ?,
                       ID_PERFIL_INVESTIDOR = ?
                 WHERE ID = ?
                """;

        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.setString(4, usuario.getCpf());
            ps.setString(5, usuario.getTelefone());

            if (usuario.getAutenticacao() != null && usuario.getAutenticacao().getId() != null) {
                ps.setLong(6, usuario.getAutenticacao().getId());
            } else {
                ps.setNull(6, Types.NUMERIC);
            }

            if (usuario.getPerfilInvestidor() != null && usuario.getPerfilInvestidor().getId() != null) {
                ps.setLong(7, usuario.getPerfilInvestidor().getId());
            } else {
                ps.setNull(7, Types.NUMERIC);
            }

            ps.setLong(8, usuario.getId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        final String sql = "DELETE FROM USUARIO WHERE ID = ?";
        Connection conn = ConnectionFactory.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("ID"));
        usuario.setNome(rs.getString("NOME"));
        usuario.setEmail(rs.getString("EMAIL"));
        usuario.setSenha(rs.getString("SENHA"));
        usuario.setCpf(rs.getString("CPF"));
        usuario.setTelefone(rs.getString("TELEFONE"));

        long idAut = rs.getLong("ID_AUTENTICACAO");
        if (!rs.wasNull()) {
            Autenticacao aut = new Autenticacao();
            aut.setId(idAut);
            usuario.setAutenticacao(aut);
        }

        long idPerfil = rs.getLong("ID_PERFIL_INVESTIDOR");
        if (!rs.wasNull()) {
            PerfilInvestidor perfil = new PerfilInvestidor();
            perfil.setId(idPerfil);
            usuario.setPerfilInvestidor(perfil);
        }

        return usuario;
    }
}
