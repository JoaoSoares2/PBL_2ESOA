public class PerfilInvestidor {

    private Long id;
    private String nomePerfil;
    private String descricao;
    private String nivelRisco;

    public PerfilInvestidor() {}

    public PerfilInvestidor(String nomePerfil, String descricao, String nivelRisco) {
        this.nomePerfil = nomePerfil;
        this.descricao = descricao;
        this.nivelRisco = nivelRisco;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomePerfil() { return nomePerfil; }
    public void setNomePerfil(String nomePerfil) { this.nomePerfil = nomePerfil; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(String nivelRisco) { this.nivelRisco = nivelRisco; }

    public void exibirPerfil() {
        System.out.println("Perfil: " + nomePerfil);
        System.out.println("Nível de Risco: " + nivelRisco);
        System.out.println("Descrição: " + descricao);
    }

    public String avaliarRisco() {
        return switch (nivelRisco.toUpperCase()) {
            case "ALTO"  -> "Perfil agressivo: alta exposição a risco para maior rentabilidade.";
            case "MEDIO" -> "Perfil moderado: equilíbrio entre segurança e rentabilidade.";
            case "BAIXO" -> "Perfil conservador: prioridade à segurança do capital.";
            default      -> "Nível de risco não reconhecido.";
        };
    }

    @Override
    public String toString() {
        return "PerfilInvestidor{perfil='" + nomePerfil + "', risco='" + nivelRisco + "'}";
    }
}
