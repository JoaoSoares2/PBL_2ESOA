import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Utilitario de console para a Fase 7 - Missao Tio Patinhas.
 *
 * Centraliza a leitura segura de dados pelo teclado (Scanner),
 * tratamento de quebras de linha/buffer, formatacao de saida
 * (moeda, data, tabelas) e elementos visuais de interface de console.
 */
public final class ConsoleUtils {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final NumberFormat MOEDA_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_BR);
    private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATA_HORA_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private ConsoleUtils() {
        // Construtor privado para impedir instanciacao
    }

    // =========================================================================
    // METODOS DE FORMATACAO E EXIBICAO
    // =========================================================================

    public static void imprimirCabecalho(String titulo) {
        System.out.println();
        System.out.println("================================================================================");
        int espacos = Math.max(0, (80 - titulo.length()) / 2);
        System.out.println(" ".repeat(espacos) + titulo);
        System.out.println("================================================================================");
    }

    public static void imprimirSubcabecalho(String subtitulo) {
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  " + subtitulo);
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static void imprimirSucesso(String mensagem) {
        System.out.println("  [SUCESSO] " + mensagem);
    }

    public static void imprimirErro(String mensagem) {
        System.err.println("  [ERRO] " + mensagem);
    }

    public static void imprimirAviso(String mensagem) {
        System.out.println("  [AVISO] " + mensagem);
    }

    public static void imprimirLinhaDivisoria() {
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static void pausar() {
        System.out.print("\nPressione ENTER para continuar...");
        try {
            SCANNER.nextLine();
        } catch (Exception ignored) {
        }
    }

    public static String formatarMoeda(Double valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        return MOEDA_FORMAT.format(valor);
    }

    public static String formatarData(Date data) {
        if (data == null) {
            return "-";
        }
        return DATA_FORMAT.format(data);
    }

    public static String formatarDataHora(Date data) {
        if (data == null) {
            return "-";
        }
        return DATA_HORA_FORMAT.format(data);
    }

    // =========================================================================
    // METODOS DE LEITURA SEGURA DE DADOS
    // =========================================================================

    /**
     * Le uma string do console.
     *
     * @param prompt Mensagem de solicitacao exibida ao usuario
     * @param obrigatorio Se true, nao aceita resposta vazia/em branco
     * @return O texto digitado pelo usuario
     */
    public static String lerTexto(String prompt, boolean obrigatorio) {
        while (true) {
            System.out.print(prompt);
            try {
                String entrada = SCANNER.nextLine();
                if (entrada != null) {
                    entrada = entrada.trim();
                }
                if (obrigatorio && (entrada == null || entrada.isEmpty())) {
                    imprimirAviso("Este campo e obrigatorio e nao pode ser deixado em branco.");
                    continue;
                }
                return entrada;
            } catch (NoSuchElementException e) {
                return "";
            }
        }
    }

    /**
     * Le uma string mantendo o valor atual como padrao caso o usuario aperte ENTER.
     * Ideal para operacoes de Alteracao/Update no CRUD.
     */
    public static String lerTextoOpcional(String prompt, String valorAtual) {
        String texto = lerTexto(prompt, false);
        if (texto == null || texto.isEmpty()) {
            return valorAtual;
        }
        return texto;
    }

    /**
     * Le um numero inteiro no intervalo [min, max].
     */
    public static int lerInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String entrada = SCANNER.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor < min || valor > max) {
                    imprimirAviso("Valor fora da faixa permitida (" + min + " a " + max + "). Tente novamente.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                imprimirAviso("Por favor, digite um numero inteiro valido.");
            }
        }
    }

    /**
     * Le um numero Long com limites opcionais.
     */
    public static Long lerLong(String prompt, Long min, Long max) {
        while (true) {
            System.out.print(prompt);
            String entrada = SCANNER.nextLine().trim();
            try {
                long valor = Long.parseLong(entrada);
                if (min != null && valor < min) {
                    imprimirAviso("O valor minimo permitido e " + min + ".");
                    continue;
                }
                if (max != null && valor > max) {
                    imprimirAviso("O valor maximo permitido e " + max + ".");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                imprimirAviso("Por favor, digite um numero valido (ex: 1, 100, 1000).");
            }
        }
    }

    /**
     * Le um Long opcional, mantendo o valor atual se o usuario apertar ENTER.
     */
    public static Long lerLongOpcional(String prompt, Long valorAtual) {
        while (true) {
            System.out.print(prompt);
            String entrada = SCANNER.nextLine().trim();
            if (entrada.isEmpty()) {
                return valorAtual;
            }
            try {
                return Long.parseLong(entrada);
            } catch (NumberFormatException e) {
                imprimirAviso("Por favor, digite um numero valido ou pressione ENTER para manter o valor atual.");
            }
        }
    }

    /**
     * Le um Double com limites opcionais.
     * Suporta tanto virgula (padrao BR: 12,50) quanto ponto (12.50).
     */
    public static Double lerDouble(String prompt, Double min, Double max) {
        while (true) {
            System.out.print(prompt);
            String entrada = SCANNER.nextLine().trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(entrada);
                if (min != null && valor < min) {
                    imprimirAviso("O valor minimo permitido e " + min + ".");
                    continue;
                }
                if (max != null && valor > max) {
                    imprimirAviso("O valor maximo permitido e " + max + ".");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                imprimirAviso("Por favor, digite um valor numerico valido (ex: 100.50 ou 100,50).");
            }
        }
    }

    /**
     * Le um Double opcional, mantendo o valor atual se o usuario apertar ENTER.
     */
    public static Double lerDoubleOpcional(String prompt, Double valorAtual) {
        while (true) {
            System.out.print(prompt);
            String entrada = SCANNER.nextLine().trim().replace(",", ".");
            if (entrada.isEmpty()) {
                return valorAtual;
            }
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                imprimirAviso("Por favor, digite um valor numerico valido ou pressione ENTER para manter o valor atual.");
            }
        }
    }

    /**
     * Le uma data no formato dd/MM/yyyy.
     */
    public static Date lerData(String prompt, boolean obrigatorio) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);

        while (true) {
            System.out.print(prompt);
            String entrada = SCANNER.nextLine().trim();
            if (entrada.isEmpty()) {
                if (obrigatorio) {
                    imprimirAviso("A data e obrigatoria. Informe no formato dd/MM/yyyy.");
                    continue;
                }
                return null;
            }
            try {
                return sdf.parse(entrada);
            } catch (ParseException e) {
                imprimirAviso("Data invalida! Utilize o formato dd/MM/yyyy (ex: 15/03/2026).");
            }
        }
    }

    /**
     * Solicita confirmacao do usuario (S/N).
     *
     * @return true se o usuario digitar 'S' ou 'SIM', false caso contrario
     */
    public static boolean confirmar(String prompt) {
        System.out.print(prompt + " (S/N): ");
        String resposta = SCANNER.nextLine().trim().toUpperCase();
        return resposta.equals("S") || resposta.equals("SIM");
    }
}
