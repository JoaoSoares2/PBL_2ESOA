public class Autenticacao {

    private Long id;
    private String login;
    private String senha;

    // Relacionamento
    private AutenticacaoMultifator autenticacaoMultifator; // 1--1

    public Autenticacao() {}

    public Autenticacao(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    public boolean validarCredenciais(String login, String senha) {
        return this.login.equals(login) && this.senha.equals(senha);
    }

    public void encerrarSessao() {
        // Lógica de invalidação de token/sessão
    }
}