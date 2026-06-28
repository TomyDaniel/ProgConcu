import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AnalizadorInvariantes {
    private final String logPath;
    private final int invariantesEsperados;

    public AnalizadorInvariantes(String logPath, int invariantesEsperados) {
        this.logPath = logPath;
        this.invariantesEsperados = invariantesEsperados;
    }

    public void analizar() {
        StringBuilder logsBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(logPath))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.contains("T")) {
                    logsBuilder.append(linea.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo log: " + e.getMessage());
            return;
        }

        String logs = logsBuilder.toString();
        if (logs.isEmpty()) {
            System.out.println("No se encontró la secuencia de disparos en el log.");
            return;
        }

        // Firmas únicas de cada camino antes de llegar a P11
        int conteoSimple = contarOcurrencias(logs, "T6-");
        int conteoMedia = contarOcurrencias(logs, "T4-");
        int conteoAlta = contarOcurrencias(logs, "T10-");
        int totalT11 = contarOcurrencias(logs, "T11-");

        System.out.println("Total secuencias principales completadas (T11): " + totalT11);

        double porcentajeSimple = totalT11 > 0 ? (conteoSimple * 100.0) / totalT11 : 0;
        double porcentajeMedia = totalT11 > 0 ? (conteoMedia * 100.0) / totalT11 : 0;
        double porcentajeAlta = totalT11 > 0 ? (conteoAlta * 100.0) / totalT11 : 0;

        System.out.printf("  IT2 (simple): %d veces (%.1f%%)%n", conteoSimple, porcentajeSimple);
        System.out.printf("  IT1 (media): %d veces (%.1f%%)%n", conteoMedia, porcentajeMedia);
        System.out.printf("  IT3 (alta): %d veces (%.1f%%)%n", conteoAlta, porcentajeAlta);

        System.out.println("\nEstado final del análisis:");
        int sumaCaminos = conteoSimple + conteoMedia + conteoAlta;

        if (sumaCaminos == totalT11) {
            System.out.println("  [OK] Consistencia interna: T4+T6+T10 coincide con T11.");
        } else {
            System.out.println("  [ERROR] Discrepancia estructural: suma de procesos (" + sumaCaminos + ") != T11 (" + totalT11 + ")");
        }

        if (totalT11 == invariantesEsperados) {
            System.out.println("  [OK] Se completaron exactamente " + invariantesEsperados + " invariantes.");
        } else {
            System.out.println("  [ADVERTENCIA] Se completaron " + totalT11 + " invariantes, se esperaban " + invariantesEsperados + ".");
        }
    }

    private int contarOcurrencias(String texto, String subcadena) {
        int contador = 0;
        int indice = 0;
        while ((indice = texto.indexOf(subcadena, indice)) != -1) {
            contador++;
            indice += subcadena.length();
        }
        return contador;
    }

}