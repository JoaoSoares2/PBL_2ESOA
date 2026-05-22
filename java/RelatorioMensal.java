import java.util.Date;

public class RelatorioMensal extends Relatorio {

    private int mes;
    private int ano;

    public RelatorioMensal() {
        super("MENSAL");
    }

    public RelatorioMensal(int mes, int ano) {
        super("MENSAL");
        this.mes = mes;
        this.ano = ano;
    }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    @Override
    public void gerarRelatorio() {
        setDataGeracao(new Date());
        String conteudo = "=== Relatório Mensal: " + String.format("%02d/%d", mes, ano) + " ===\n"
                + "Período de referência: " + mes + "/" + ano + "\n"
                + "Gerado em: " + getDataGeracao();
        setConteudo(conteudo);
        System.out.println(conteudo);
    }

    @Override
    public String toString() {
        return "RelatorioMensal{mes=" + mes + ", ano=" + ano + ", tipo='" + getTipo() + "'}";
    }
}
