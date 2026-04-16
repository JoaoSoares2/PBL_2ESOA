import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Carteira {

    private Long id;
    private String nome;
    private Double saldoTotal;
    private Date dataCriacao;

    // Relacionamentos
    private List<Ativo> ativos;           // 0..*
    private List<Transacao> transacoes;   // 0..*

    public Carteira() {
        this.ativos = new ArrayList<>();
        this.transacoes = new ArrayList<>();
        this.dataCriacao = new Date();
        this.saldoTotal = 0.0;
    }

    public Carteira(String nome) {
        this();
        this.nome = nome;
    }

    public void adicionarAtivo(Ativo ativo) {
        this.ativos.add(ativo);
        calcularSaldo();
    }

    public void removerAtivo(Ativo ativo) {
        this.ativos.remove(ativo);
        calcularSaldo();
    }

    public Double calcularSaldo() {
        this.saldoTotal = ativos.stream()
                .mapToDouble(Ativo::calcularValorTotal)
                .sum();

        return this.saldoTotal;
    }

    public List<Transacao> listarTransacoes() {
        return this.transacoes;
    }

    public void registrarTransacao(Transacao transacao) {
        this.transacoes.add(transacao);
    }
}