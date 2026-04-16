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

    public void exibirPerfil() {
        System.out.println("Perfil: " + nomePerfil);
        System.out.println("Nível de Risco: " + nivelRisco);
        System.out.println("Descrição: " + descricao);
    }

    public String avaliarRisco() {
        // Regra de negócio: retorna uma recomendação baseada no nível de risco
        return switch (nivelRisco.toUpperCase()) {
            case "ALTO"   -> "Perfil agressivo";
            case "MEDIO"  -> "Perfil moderado";
            case "BAIXO"  -> "Perfil conservador";
            default       -> "Nível de risco não reconhecido.";
        };
    }
}