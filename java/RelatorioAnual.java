import java.util.Date;

public class RelatorioAnual extends Relatorio {

    private int ano;

    public RelatorioAnual() {
        super("ANUAL");
    }

    public RelatorioAnual(int ano) {
        super("ANUAL");
        this.ano = ano;
    }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    @Override
    public void gerarRelatorio() {
        setDataGeracao(new Date());
        String conteudo = "=== Relatório Anual: " + ano + " ===\n"
                + "Período: 01/01/" + ano + " até 31/12/" + ano + "\n"
                + "Gerado em: " + getDataGeracao();
        setConteudo(conteudo);
        System.out.println(conteudo);
    }

    @Override
    public String toString() {
        return "RelatorioAnual{ano=" + ano + ", tipo='" + getTipo() + "'}";
    }
}
