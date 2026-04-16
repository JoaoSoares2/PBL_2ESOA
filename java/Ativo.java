public class Ativo {

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

    public void atualizarCotacao(Double novoValor) {
        this.valorAtual = novoValor;
    }

    public Double calcularValorTotal() {
        return this.valorAtual * this.quantidade;
    }
}