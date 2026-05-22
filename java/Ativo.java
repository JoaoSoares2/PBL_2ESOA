public abstract class Ativo {

    private Long id;
    private String nome;
    private Double valorAtual;
    private Double quantidade;

    public Ativo() {}

    public Ativo(String nome, Double valorAtual, Double quantidade) {
        this.nome = nome;
        this.valorAtual = valorAtual;
        this.quantidade = quantidade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getValorAtual() { return valorAtual; }
    public void setValorAtual(Double valorAtual) { this.valorAtual = valorAtual; }

    public Double getQuantidade() { return quantidade; }
    public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }

    public void atualizarCotacao(Double novoValor) {
        this.valorAtual = novoValor;
    }

    public void atualizarCotacao(Double variacao, boolean percentual) {
        if (percentual) {
            this.valorAtual = this.valorAtual * (1 + variacao / 100);
        } else {
            this.valorAtual = variacao;
        }
    }

    public Double calcularValorTotal() {
        return this.valorAtual * this.quantidade;
    }

    @Override
    public String toString() {
        return "Ativo{nome='" + nome + "', valorAtual=R$" + String.format("%.2f", valorAtual)
                + ", quantidade=" + quantidade + "}";
    }
}
