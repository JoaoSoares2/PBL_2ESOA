public class Autenticacao {

    private Long id;
    private String login;
    private String senha;

    private AutenticacaoMultifator autenticacaoMultifator;

    public Autenticacao() {}

    public Autenticacao(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public AutenticacaoMultifator getAutenticacaoMultifator() { return autenticacaoMultifator; }
    public void setAutenticacaoMultifator(AutenticacaoMultifator autenticacaoMultifator) {
        this.autenticacaoMultifator = autenticacaoMultifator;
    }

    public boolean validarCredenciais(String login, String senha) {
        return this.login.equals(login) && this.senha.equals(senha);
    }

    public void encerrarSessao() {}

    @Override
    public String toString() {
        return "Autenticacao{login='" + login + "', mfaAtiva=" + (autenticacaoMultifator != null) + "}";
    }
}
