import java.util.Date;

public class Relatorio {

    private Long id;
    private String tipo;
    private Date dataGeracao;

    public Relatorio() {}

    public Relatorio(String tipo) {
        this.tipo = tipo;
        this.dataGeracao = new Date();
    }

    public void gerarRelatorio() {
        this.dataGeracao = new Date();
        System.out.println("Relatório do tipo [" + tipo + "] gerado em: " + dataGeracao);
        // Lógica de geração de PDF.
    }
}