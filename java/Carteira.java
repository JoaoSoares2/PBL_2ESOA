import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Carteira {

    private Long id;
    private String nome;
    private Double saldoTotal;
    private Date dataCriacao;

    private List<CarteiraAtivo> carteiraAtivos;
    private List<Transacao> transacoes;

    public Carteira() {
        this.carteiraAtivos = new ArrayList<>();
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

    public List<CarteiraAtivo> getCarteiraAtivos() { return carteiraAtivos; }
    public void setCarteiraAtivos(List<CarteiraAtivo> carteiraAtivos) { this.carteiraAtivos = carteiraAtivos; }

    public List<Transacao> getTransacoes() { return transacoes; }
    public void setTransacoes(List<Transacao> transacoes) { this.transacoes = transacoes; }

    public void adicionarAtivo(Ativo ativo, Double quantidade, Double precoMedio) {
        this.carteiraAtivos.add(new CarteiraAtivo(this, ativo, quantidade, precoMedio));
        calcularSaldo();
    }

    public void adicionarAtivo(Ativo ativo) {
        adicionarAtivo(ativo, ativo.getQuantidade(), ativo.getValorAtual());
    }

    public void removerAtivo(Ativo ativo) {
        this.carteiraAtivos.removeIf(ca -> ca.getAtivo().equals(ativo));
        calcularSaldo();
    }

    public void removerAtivo(String nomeAtivo) {
        this.carteiraAtivos.removeIf(ca -> ca.getAtivo().getNome().equalsIgnoreCase(nomeAtivo));
        calcularSaldo();
    }

    public Double calcularSaldo() {
        this.saldoTotal = carteiraAtivos.stream()
                .mapToDouble(CarteiraAtivo::calcularValorAtual)
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
                + ", ativos=" + carteiraAtivos.size() + "}";
    }
}
