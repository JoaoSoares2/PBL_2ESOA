public class Acao extends Ativo {

    private String ticker;
    private String bolsa;
    private Double variacaoDiaria;

    public Acao() {}

    public Acao(String nome, Double valorAtual, Double quantidade, String ticker, String bolsa) {
        super(nome, valorAtual, quantidade);
        this.ticker = ticker;
        this.bolsa = bolsa;
        this.variacaoDiaria = 0.0;
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getBolsa() { return bolsa; }
    public void setBolsa(String bolsa) { this.bolsa = bolsa; }

    public Double getVariacaoDiaria() { return variacaoDiaria; }
    public void setVariacaoDiaria(Double variacaoDiaria) { this.variacaoDiaria = variacaoDiaria; }

    @Override
    public Double calcularValorTotal() {
        double valorBase = super.calcularValorTotal();
        return valorBase * (1 + variacaoDiaria / 100);
    }

    @Override
    public String toString() {
        return "Acao{ticker='" + ticker + "', bolsa='" + bolsa + "', variacao=" + variacaoDiaria
                + "%, total=R$" + String.format("%.2f", calcularValorTotal()) + "}";
    }
}
