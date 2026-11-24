import java.util.regex.Pattern;

/**
 * Utilitario de validacao de regras de negocio e formatos para a Fase 7.
 *
 * Contem algoritmos oficiais de validacao de CPF e CNPJ (com calculo
 * dos digitos verificadores por modulo 11), regex de e-mail, dominios
 * permitidos no banco (Tipo de Transacao, Nivel de Risco, Tipo de Relatorio)
 * e metodos de leitura guiada com validacao imediata.
 */
public final class Validador {

    private static final Pattern REGEX_EMAIL = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern REGEX_TICKER = Pattern.compile("^[A-Z]{4}[0-9]{1,2}$");

    private Validador() {
        // Construtor privado para impedir instanciacao
    }

    // =========================================================================
    // VALIDACOES ALGORITMICAS E REGEX
    // =========================================================================

    /**
     * Valida CPF aplicando a regra oficial da Receita Federal (calculo dos dois digitos verificadores).
     */
    public static boolean validarCpf(String cpf) {
        if (cpf == null) {
            return false;
        }

        String apenasDigitos = cpf.replaceAll("\\D", "");
        if (apenasDigitos.length() != 11) {
            return false;
        }

        // Rejeita CPFs formados por digitos repetidos (ex: 111.111.111-11)
        if (apenasDigitos.chars().distinct().count() == 1) {
            return false;
        }

        try {
            // Primeiro digito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (apenasDigitos.charAt(i) - '0') * (10 - i);
            }
            int resto = 11 - (soma % 11);
            int digito1 = (resto >= 10) ? 0 : resto;

            if (digito1 != (apenasDigitos.charAt(9) - '0')) {
                return false;
            }

            // Segundo digito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (apenasDigitos.charAt(i) - '0') * (11 - i);
            }
            resto = 11 - (soma % 11);
            int digito2 = (resto >= 10) ? 0 : resto;

            return digito2 == (apenasDigitos.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formata uma string de 11 digitos em CPF: 000.000.000-00.
     */
    public static String formatarCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        String d = cpf.replaceAll("\\D", "");
        if (d.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    d.substring(0, 3), d.substring(3, 6), d.substring(6, 9), d.substring(9, 11));
        }
        return cpf;
    }

    /**
     * Valida CNPJ aplicando a regra oficial da Receita Federal (calculo dos dois digitos verificadores).
     */
    public static boolean validarCnpj(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        String apenasDigitos = cnpj.replaceAll("\\D", "");
        if (apenasDigitos.length() != 14) {
            return false;
        }

        // Rejeita CNPJs com digitos todos iguais
        if (apenasDigitos.chars().distinct().count() == 1) {
            return false;
        }

        try {
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += (apenasDigitos.charAt(i) - '0') * peso1[i];
            }
            int resto = soma % 11;
            int digito1 = (resto < 2) ? 0 : (11 - resto);

            if (digito1 != (apenasDigitos.charAt(12) - '0')) {
                return false;
            }

            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += (apenasDigitos.charAt(i) - '0') * peso2[i];
            }
            resto = soma % 11;
            int digito2 = (resto < 2) ? 0 : (11 - resto);

            return digito2 == (apenasDigitos.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formata uma string de 14 digitos em CNPJ: 00.000.000/0000-00.
     */
    public static String formatarCnpj(String cnpj) {
        if (cnpj == null) {
            return "";
        }
        String d = cnpj.replaceAll("\\D", "");
        if (d.length() == 14) {
            return String.format("%s.%s.%s/%s-%s",
                    d.substring(0, 2), d.substring(2, 5), d.substring(5, 8), d.substring(8, 12), d.substring(12, 14));
        }
        return cnpj;
    }

    /**
     * Valida formato de e-mail.
     */
    public static boolean validarEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return REGEX_EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Valida ticker de acao no formato padrao B3 (ex: PETR4, VALE3, BOVA11).
     */
    public static boolean validarTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            return false;
        }
        return REGEX_TICKER.matcher(ticker.trim().toUpperCase()).matches();
    }

    /**
     * Valida nivel de risco para PerfilInvestidor (BAIXO, MEDIO, ALTO).
     */
    public static boolean validarNivelRisco(String risco) {
        if (risco == null) {
            return false;
        }
        String r = risco.trim().toUpperCase();
        return r.equals("BAIXO") || r.equals("MEDIO") || r.equals("ALTO");
    }

    /**
     * Valida tipo de transacao (COMPRA, VENDA).
     */
    public static boolean validarTipoTransacao(String tipo) {
        if (tipo == null) {
            return false;
        }
        String t = tipo.trim().toUpperCase();
        return t.equals("COMPRA") || t.equals("VENDA");
    }

    /**
     * Valida tipo de relatorio (MENSAL, ANUAL).
     */
    public static boolean validarTipoRelatorio(String tipo) {
        if (tipo == null) {
            return false;
        }
        String t = tipo.trim().toUpperCase();
        return t.equals("MENSAL") || t.equals("ANUAL");
    }

    // =========================================================================
    // METODOS INTERATIVOS DE LEITURA COM VALIDACAO
    // =========================================================================

    public static String lerCpf(String prompt, boolean obrigatorio) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, obrigatorio);
            if (!obrigatorio && (entrada == null || entrada.isEmpty())) {
                return null;
            }
            if (validarCpf(entrada)) {
                return formatarCpf(entrada);
            }
            ConsoleUtils.imprimirAviso("CPF invalido! Digite os 11 digitos numericos validos.");
        }
    }

    public static String lerCpfOpcional(String prompt, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, false);
            if (entrada == null || entrada.isEmpty()) {
                return valorAtual;
            }
            if (validarCpf(entrada)) {
                return formatarCpf(entrada);
            }
            ConsoleUtils.imprimirAviso("CPF invalido! Digite um CPF valido ou pressione ENTER para manter o atual.");
        }
    }

    public static String lerCnpj(String prompt, boolean obrigatorio) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, obrigatorio);
            if (!obrigatorio && (entrada == null || entrada.isEmpty())) {
                return null;
            }
            if (validarCnpj(entrada)) {
                return formatarCnpj(entrada);
            }
            ConsoleUtils.imprimirAviso("CNPJ invalido! Digite os 14 digitos numericos validos.");
        }
    }

    public static String lerCnpjOpcional(String prompt, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, false);
            if (entrada == null || entrada.isEmpty()) {
                return valorAtual;
            }
            if (validarCnpj(entrada)) {
                return formatarCnpj(entrada);
            }
            ConsoleUtils.imprimirAviso("CNPJ invalido! Digite um CNPJ valido ou pressione ENTER para manter o atual.");
        }
    }

    public static String lerEmail(String prompt, boolean obrigatorio) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, obrigatorio);
            if (!obrigatorio && (entrada == null || entrada.isEmpty())) {
                return null;
            }
            if (validarEmail(entrada)) {
                return entrada.trim().toLowerCase();
            }
            ConsoleUtils.imprimirAviso("E-mail com formato invalido! (exemplo: investidor@fiap.com.br)");
        }
    }

    public static String lerEmailOpcional(String prompt, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, false);
            if (entrada == null || entrada.isEmpty()) {
                return valorAtual;
            }
            if (validarEmail(entrada)) {
                return entrada.trim().toLowerCase();
            }
            ConsoleUtils.imprimirAviso("E-mail invalido! Digite um e-mail valido ou pressione ENTER para manter o atual.");
        }
    }

    public static String lerTicker(String prompt, boolean obrigatorio, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, obrigatorio);
            if (entrada == null || entrada.isEmpty()) {
                if (valorAtual != null && !valorAtual.isEmpty()) {
                    return valorAtual;
                }
                if (!obrigatorio) {
                    return null;
                }
            }
            if (entrada != null && validarTicker(entrada)) {
                return entrada.trim().toUpperCase();
            }
            ConsoleUtils.imprimirAviso("Ticker invalido! Formato esperado: 4 letras seguidas de 1 ou 2 numeros (ex: PETR4, VALE3, BOVA11).");
        }
    }

    public static String lerNivelRisco(String prompt, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, false);
            if (entrada == null || entrada.isEmpty()) {
                if (valorAtual != null && !valorAtual.isEmpty()) {
                    return valorAtual;
                }
                ConsoleUtils.imprimirAviso("O nivel de risco e obrigatorio (BAIXO, MEDIO ou ALTO).");
                continue;
            }
            String normalizado = entrada.trim().toUpperCase();
            if (validarNivelRisco(normalizado)) {
                return normalizado;
            }
            ConsoleUtils.imprimirAviso("Nivel de risco invalido! Opcoes permitidas: BAIXO, MEDIO, ALTO.");
        }
    }

    public static String lerTipoTransacao(String prompt, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, false);
            if (entrada == null || entrada.isEmpty()) {
                if (valorAtual != null && !valorAtual.isEmpty()) {
                    return valorAtual;
                }
                ConsoleUtils.imprimirAviso("O tipo de transacao e obrigatorio (COMPRA ou VENDA).");
                continue;
            }
            String normalizado = entrada.trim().toUpperCase();
            if (validarTipoTransacao(normalizado)) {
                return normalizado;
            }
            ConsoleUtils.imprimirAviso("Tipo de transacao invalido! Opcoes permitidas: COMPRA, VENDA.");
        }
    }

    public static String lerTipoRelatorio(String prompt, String valorAtual) {
        while (true) {
            String entrada = ConsoleUtils.lerTexto(prompt, false);
            if (entrada == null || entrada.isEmpty()) {
                if (valorAtual != null && !valorAtual.isEmpty()) {
                    return valorAtual;
                }
                ConsoleUtils.imprimirAviso("O tipo de relatorio e obrigatorio (MENSAL ou ANUAL).");
                continue;
            }
            String normalizado = entrada.trim().toUpperCase();
            if (validarTipoRelatorio(normalizado)) {
                return normalizado;
            }
            ConsoleUtils.imprimirAviso("Tipo de relatorio invalido! Opcoes permitidas: MENSAL, ANUAL.");
        }
    }
}
