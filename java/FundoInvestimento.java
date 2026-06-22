public class FundoInvestimento extends Ativo {

    private Double taxaAdministracao;
    private String tipoFundo;

    public FundoInvestimento() {}

    public FundoInvestimento(String nome, Double valorAtual, Double quantidade, Double taxaAdministracao, String tipoFundo) {
        super(nome, valorAtual, quantidade);
        this.taxaAdministracao = taxaAdministracao;
        this.tipoFundo = tipoFundo;
    }

    public Double getTaxaAdministracao() { return taxaAdministracao; }
    public void setTaxaAdministracao(Double taxaAdministracao) { this.taxaAdministracao = taxaAdministracao; }

    public String getTipoFundo() { return tipoFundo; }
    public void setTipoFundo(String tipoFundo) { this.tipoFundo = tipoFundo; }

    @Override
    public Double calcularValorTotal() {
        double valorBase = super.calcularValorTotal();
        double desconto = taxaAdministracao / 12 / 100;
        return valorBase * (1 - desconto);
    }

    @Override
    public String toString() {
        return "FundoInvestimento{nome='" + getNome() + "', tipo='" + tipoFundo
                + "', taxa=" + taxaAdministracao + "% a.a., total=R$"
                + String.format("%.2f", calcularValorTotal()) + "}";
    }
}
