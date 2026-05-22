public class AtivoRendaVariavel extends Ativo {

    private String ticker;
    private Double variacaoDiaria;

    public AtivoRendaVariavel() {}

    public AtivoRendaVariavel(String nome, Double valorAtual, Double quantidade, String ticker) {
        super(nome, valorAtual, quantidade);
        this.ticker = ticker;
        this.variacaoDiaria = 0.0;
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public Double getVariacaoDiaria() { return variacaoDiaria; }
    public void setVariacaoDiaria(Double variacaoDiaria) { this.variacaoDiaria = variacaoDiaria; }

    @Override
    public Double calcularValorTotal() {
        double valorBase = super.calcularValorTotal();
        return valorBase * (1 + variacaoDiaria / 100);
    }

    @Override
    public String toString() {
        return "AtivoRendaVariavel{ticker='" + ticker + "', variacao=" + variacaoDiaria
                + "%, total=R$" + String.format("%.2f", calcularValorTotal()) + "}";
    }
}
