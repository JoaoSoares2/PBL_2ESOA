import java.util.Date;
import java.util.List;

public class Transacao {

    private Long id;
    private String tipo;
    private Double valor;
    private Date data;
    private String descricao;
    private Ativo ativo;

    public Transacao() {
        this.data = new Date();
    }

    public Transacao(String tipo, Double valor) {
        this();
        this.tipo = tipo;
        this.valor = valor;
    }

    public Transacao(String tipo, Double valor, String descricao) {
        this(tipo, valor);
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Ativo getAtivo() { return ativo; }
    public void setAtivo(Ativo ativo) { this.ativo = ativo; }

    public Transacao registrarCompra(Double valor) {
        return new Transacao("COMPRA", valor);
    }

    public Transacao registrarCompra(Double valor, String descricao) {
        return new Transacao("COMPRA", valor, descricao);
    }

    public Transacao registrarCompra(Ativo ativo) {
        Transacao t = new Transacao("COMPRA", ativo.calcularValorTotal(), "Compra de " + ativo.getNome());
        t.setAtivo(ativo);
        return t;
    }

    public Transacao registrarVenda(Double valor) {
        return new Transacao("VENDA", valor);
    }

    public Transacao registrarVenda(Double valor, String descricao) {
        return new Transacao("VENDA", valor, descricao);
    }

    public Transacao registrarVenda(Ativo ativo) {
        Transacao t = new Transacao("VENDA", ativo.calcularValorTotal(), "Venda de " + ativo.getNome());
        t.setAtivo(ativo);
        return t;
    }

    public void consultarTransacoes(List<Transacao> transacoes) {
        transacoes.forEach(t -> {
            String linha = "[" + t.getTipo() + "] R$ " + String.format("%.2f", t.getValor())
                    + " em " + t.getData();
            if (t.getAtivo() != null) linha += " | Ativo: " + t.getAtivo().getNome();
            if (t.getDescricao() != null) linha += " - " + t.getDescricao();
            System.out.println(linha);
        });
    }

    @Override
    public String toString() {
        return "Transacao{tipo='" + tipo + "', valor=R$" + String.format("%.2f", valor)
                + (ativo != null ? ", ativo='" + ativo.getNome() + "'" : "")
                + (descricao != null ? ", descricao='" + descricao + "'" : "") + "}";
    }
}
