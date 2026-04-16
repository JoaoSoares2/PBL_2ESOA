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

    public void cadastrarEmpresa() {
        // Lógica de cadastro de empresa
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
}