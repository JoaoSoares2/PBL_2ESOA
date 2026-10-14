import java.util.Date;

public class Relatorio {

    private Long id;
    private String tipo;
    private Date dataGeracao;
    private String conteudo;
    private Usuario usuario;

    public Relatorio() {}

    public Relatorio(String tipo) {
        this.tipo = tipo;
        this.dataGeracao = new Date();
    }

    public Relatorio(String tipo, Usuario usuario) {
        this(tipo);
        this.usuario = usuario;
    }

    public Relatorio(String tipo, String conteudo, Usuario usuario) {
        this(tipo, usuario);
        this.conteudo = conteudo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(Date dataGeracao) { this.dataGeracao = dataGeracao; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public void gerarRelatorio() {
        this.dataGeracao = new Date();
        this.conteudo = "Relatório [" + tipo + "] gerado em: " + dataGeracao;
        System.out.println(this.conteudo);
    }

    public void gerarRelatorio(String filtro) {
        this.dataGeracao = new Date();
        this.conteudo = "Relatório [" + tipo + "] | Filtro: " + filtro + " | Gerado em: " + dataGeracao;
        System.out.println(this.conteudo);
    }

    public void gerarRelatorio(String filtro, Date dataInicio) {
        this.dataGeracao = new Date();
        this.conteudo = "Relatório [" + tipo + "] | Período: de " + dataInicio + " até " + dataGeracao
                + " | Filtro: " + filtro;
        System.out.println(this.conteudo);
    }

    @Override
    public String toString() {
        return "Relatorio{tipo='" + tipo + "', dataGeracao=" + dataGeracao + "}";
    }
}
