import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private String cpf;
    private String telefone;

    private Autenticacao autenticacao;
    private PerfilInvestidor perfilInvestidor;
    private List<Relatorio> relatorios;
    private List<Empresa> empresas;

    public Usuario() {
        this.relatorios = new ArrayList<>();
        this.empresas = new ArrayList<>();
    }

    public Usuario(String nome, String email, String senha, String cpf, String telefone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
        this.telefone = telefone;
        this.relatorios = new ArrayList<>();
        this.empresas = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Autenticacao getAutenticacao() { return autenticacao; }
    public void setAutenticacao(Autenticacao autenticacao) { this.autenticacao = autenticacao; }

    public PerfilInvestidor getPerfilInvestidor() { return perfilInvestidor; }
    public void setPerfilInvestidor(PerfilInvestidor perfilInvestidor) { this.perfilInvestidor = perfilInvestidor; }

    public List<Relatorio> getRelatorios() { return relatorios; }
    public void setRelatorios(List<Relatorio> relatorios) { this.relatorios = relatorios; }

    public List<Empresa> getEmpresas() { return empresas; }
    public void setEmpresas(List<Empresa> empresas) { this.empresas = empresas; }

    public void cadastrar() {}

    public boolean autenticar(String email, String senha) {
        if (this.autenticacao != null) {
            return this.autenticacao.validarCredenciais(email, senha);
        }
        return false;
    }

    public void atualizarCadastro(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public List<Carteira> vizualizarCarteira() {
        return empresas.stream()
                .flatMap(empresa -> empresa.listarCarteiras().stream())
                .toList();
    }

    public List<Relatorio> consultarRelatorios() {
        return this.relatorios;
    }

    @Override
    public String toString() {
        return "Usuario{nome='" + nome + "', email='" + email + "', cpf='" + cpf + "'}";
    }
}
