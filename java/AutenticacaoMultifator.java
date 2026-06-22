import java.util.Date;

public class AutenticacaoMultifator {

    private Long id;
    private String codigo;
    private Date expiracao;

    public AutenticacaoMultifator() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Date getExpiracao() { return expiracao; }
    public void setExpiracao(Date expiracao) { this.expiracao = expiracao; }

    public String gerarCodigo() {
        this.codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
        this.expiracao = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
        return this.codigo;
    }

    public boolean validarCodigo(String codigoInformado) {
        if (expiracao == null || new Date().after(expiracao)) {
            return false;
        }
        return this.codigo != null && this.codigo.equals(codigoInformado);
    }

    @Override
    public String toString() {
        return "AutenticacaoMultifator{expiracao=" + expiracao + "}";
    }
}
