import java.util.Date;

public class UsuarioEmpresa {

    private Long id;
    private Usuario usuario;
    private Empresa empresa;
    private String cargo;
    private Date dataVinculo;

    public UsuarioEmpresa() {}

    public UsuarioEmpresa(Usuario usuario, Empresa empresa, String cargo) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.cargo = cargo;
        this.dataVinculo = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public Date getDataVinculo() { return dataVinculo; }
    public void setDataVinculo(Date dataVinculo) { this.dataVinculo = dataVinculo; }

    @Override
    public String toString() {
        return "UsuarioEmpresa{usuario='" + usuario.getNome() + "', empresa='" + empresa.getNome()
                + "', cargo='" + cargo + "', dataVinculo=" + dataVinculo + "}";
    }
}
