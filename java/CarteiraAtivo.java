public class CarteiraAtivo {

    private Long id;
    private Carteira carteira;
    private Ativo ativo;
    private Double quantidade;
    private Double precoMedio;

    public CarteiraAtivo() {}

    public CarteiraAtivo(Carteira carteira, Ativo ativo, Double quantidade, Double precoMedio) {
        this.carteira = carteira;
        this.ativo = ativo;
        this.quantidade = quantidade;
        this.precoMedio = precoMedio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Carteira getCarteira() { return carteira; }
    public void setCarteira(Carteira carteira) { this.carteira = carteira; }

    public Ativo getAtivo() { return ativo; }
    public void setAtivo(Ativo ativo) { this.ativo = ativo; }

    public Double getQuantidade() { return quantidade; }
    public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }

    public Double getPrecoMedio() { return precoMedio; }
    public void setPrecoMedio(Double precoMedio) { this.precoMedio = precoMedio; }

    public Double calcularValorAtual() {
        return ativo.getValorAtual() * quantidade;
    }

    public Double calcularLucro() {
        return (ativo.getValorAtual() - precoMedio) * quantidade;
    }

    @Override
    public String toString() {
        return "CarteiraAtivo{ativo='" + ativo.getNome() + "', qtd=" + quantidade
                + ", precoMedio=R$" + String.format("%.2f", precoMedio)
                + ", valorAtual=R$" + String.format("%.2f", calcularValorAtual()) + "}";
    }
}
