import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexoes com o banco Oracle da FIAP.
 *
 * A classe mantem uma unica conexao reutilizavel pelas classes DAO.
 * As credenciais podem ser informadas por variaveis de ambiente ou
 * propriedades da JVM, evitando gravar usuario e senha no codigo-fonte.
 */
public final class ConnectionFactory {

    private static final String DEFAULT_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL";

    private static Connection connection;
    private static String customUrl;
    private static String customUsuario;
    private static String customSenha;

    private ConnectionFactory() {
        // Impede instanciacao.
    }

    /**
     * Configura manualmente as credenciais para conexao.
     */
    public static void setCredenciais(String usuario, String senha) {
        customUsuario = usuario;
        customSenha = senha;
        if (usuario != null) {
            System.setProperty("DB_USER", usuario);
        }
        if (senha != null) {
            System.setProperty("DB_PASSWORD", senha);
        }
    }

    /**
     * Configura a URL do banco Oracle da FIAP.
     */
    public static void setUrl(String url) {
        customUrl = url;
        if (url != null) {
            System.setProperty("DB_URL", url);
        }
    }

    /**
     * Configura URL, usuario e senha simultaneamente.
     */
    public static void setConfiguracao(String url, String usuario, String senha) {
        setUrl(url);
        setCredenciais(usuario, senha);
    }

    public static String getUrl() {
        if (customUrl != null && !customUrl.isBlank()) {
            return customUrl;
        }
        return getConfiguracao("DB_URL", DEFAULT_URL);
    }

    public static String getUsuario() {
        if (customUsuario != null && !customUsuario.isBlank()) {
            return customUsuario;
        }
        return getConfiguracao("DB_USER", "SEU_USUARIO_FIAP");
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            carregarDriverOracle();

            String url = getUrl();
            String usuario = getUsuario();
            String senha = (customSenha != null && !customSenha.isBlank())
                    ? customSenha
                    : getConfiguracao("DB_PASSWORD", "SUA_SENHA_FIAP");

            if ("SEU_USUARIO_FIAP".equals(usuario) || "SUA_SENHA_FIAP".equals(senha)) {
                throw new SQLException(
                        "Configure DB_USER e DB_PASSWORD antes de conectar ao Oracle da FIAP."
                );
            }

            try {
                connection = DriverManager.getConnection(url, usuario, senha);
            } catch (SQLException e) {
                throw new SQLException(
                        "Falha ao estabelecer conexao JDBC com o Oracle da FIAP (" + url + "): " + e.getMessage(),
                        e
                );
            }
        }

        return connection;
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexao: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    private static void carregarDriverOracle() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Driver JDBC do Oracle nao encontrado. Adicione o ojdbc ao classpath do projeto.",
                    e
            );
        }
    }

    private static String getConfiguracao(String chave, String valorPadrao) {
        String propriedade = System.getProperty(chave);
        if (propriedade != null && !propriedade.isBlank()) {
            return propriedade;
        }

        String ambiente = System.getenv(chave);
        if (ambiente != null && !ambiente.isBlank()) {
            return ambiente;
        }

        return valorPadrao;
    }
}
