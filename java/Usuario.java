import java.util.List;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private String cpf;
    private String telefone;

    // Relacionamentos
    private Autenticacao autenticacao;          // 1--1
    private PerfilInvestidor perfilInvestidor;  // 1--1
    private List<Relatorio> relatorios;         // 1--0..*
    private List<Empresa> empresas;             // 1--0..*

    public Usuario() {}

    public Usuario(String nome, String email, String senha, String cpf, String telefone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    public void cadastrar() {
        // Lógica de cadastro de usuário
    }

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
}