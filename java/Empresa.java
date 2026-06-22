import java.util.ArrayList;
import java.util.List;

public class Empresa {

    private Long id;
    private String nome;
    private String cnpj;

    private List<Carteira> carteiras;

    public Empresa() {
        this.carteiras = new ArrayList<>();
    }

    public Empresa(String nome, String cnpj) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.carteiras = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public List<Carteira> getCarteiras() { return carteiras; }
    public void setCarteiras(List<Carteira> carteiras) { this.carteiras = carteiras; }

    public void cadastrarEmpresa() {}

    public void atualizarEmpresa(String nome) {
        this.nome = nome;
    }

    public void atualizarEmpresa(String nome, String cnpj) {
        this.nome = nome;
        this.cnpj = cnpj;
    }

    public List<Carteira> listarCarteiras() {
        return this.carteiras;
    }

    public void adicionarCarteira(Carteira carteira) {
        this.carteiras.add(carteira);
    }

    @Override
    public String toString() {
        return "Empresa{nome='" + nome + "', cnpj='" + cnpj + "', carteiras=" + carteiras.size() + "}";
    }
}
