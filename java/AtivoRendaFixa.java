public class AtivoRendaFixa extends Ativo {

    private Double taxaJurosAnual;

    public AtivoRendaFixa() {}

    public AtivoRendaFixa(String nome, Double valorAtual, Double quantidade, Double taxaJurosAnual) {
        super(nome, valorAtual, quantidade);
        this.taxaJurosAnual = taxaJurosAnual;
    }

    public Double getTaxaJurosAnual() { return taxaJurosAnual; }
    public void setTaxaJurosAnual(Double taxaJurosAnual) { this.taxaJurosAnual = taxaJurosAnual; }

    @Override
    public Double calcularValorTotal() {
        double valorBase = super.calcularValorTotal();
        double rendimentoMensal = taxaJurosAnual / 12 / 100;
        return valorBase * (1 + rendimentoMensal);
    }

    @Override
    public String toString() {
        return "AtivoRendaFixa{nome='" + getNome() + "', taxa=" + taxaJurosAnual
                + "% a.a., total=R$" + String.format("%.2f", calcularValorTotal()) + "}";
    }
}
