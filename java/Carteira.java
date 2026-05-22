import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Carteira {

    private Long id;
    private String nome;
    private Double saldoTotal;
    private Date dataCriacao;

    private List<Ativo> ativos;
    private List<Transacao> transacoes;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getSaldoTotal() { return saldoTotal; }
    public void setSaldoTotal(Double saldoTotal) { this.saldoTotal = saldoTotal; }

    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }

    public List<Ativo> getAtivos() { return ativos; }
    public void setAtivos(List<Ativo> ativos) { this.ativos = ativos; }

    public List<Transacao> getTransacoes() { return transacoes; }
    public void setTransacoes(List<Transacao> transacoes) { this.transacoes = transacoes; }

    public void adicionarAtivo(Ativo ativo) {
        this.ativos.add(ativo);
        calcularSaldo();
    }

    public void adicionarAtivo(String nome, Double valor, Double quantidade) {
        adicionarAtivo(new AtivoRendaVariavel(nome, valor, quantidade, nome));
    }

    public void removerAtivo(Ativo ativo) {
        this.ativos.remove(ativo);
        calcularSaldo();
    }

    public void removerAtivo(String nomeAtivo) {
        this.ativos.removeIf(a -> a.getNome().equalsIgnoreCase(nomeAtivo));
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

    @Override
    public String toString() {
        return "Carteira{nome='" + nome + "', saldo=R$" + String.format("%.2f", saldoTotal)
                + ", ativos=" + ativos.size() + "}";
    }
}
