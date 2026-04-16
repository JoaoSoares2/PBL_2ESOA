import java.util.Date;
import java.util.UUID;

public class AutenticacaoMultifator {

    private Long id;
    private String codigo;
    private Date expiracao;

    public AutenticacaoMultifator() {}

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
}