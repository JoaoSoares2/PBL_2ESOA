import java.util.Date;
import java.util.List;

public class Transacao {

    private Long id;
    private String tipo;   // "COMPRA" ou "VENDA"
    private Double valor;
    private Date data;

    public Transacao() {
        this.data = new Date();
    }

    public Transacao(String tipo, Double valor) {
        this();
        this.tipo = tipo;
        this.valor = valor;
    }

    public Transacao registrarCompra(Double valor) {
        return new Transacao("COMPRA", valor);
    }

    public Transacao registrarVenda(Double valor) {
        return new Transacao("VENDA", valor);
    }

    public void consultarTransacoes(List<Transacao> transacoes) {
        transacoes.forEach(t ->
            System.out.println("[" + t.getTipo() + "] R$ " + t.getValor() + " em " + t.getData())
        );
    }
}