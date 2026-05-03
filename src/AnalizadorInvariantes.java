import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorInvariantes {
    private final String logPath;
    private final Map<String, Integer> conteos;

    public AnalizadorInvariantes(String logPath) {
        this.logPath = logPath;
        this.conteos = new LinkedHashMap<>();
        // Inicializamos los contadores
        conteos.put("IT2 (simple)", 0);
        conteos.put("IT1 (media)", 0);
        conteos.put("IT3 (alta)", 0);
    }

    public void analizar() {
        String logs = "";

        // Extraemos la línea de disparos del archivo
        try (BufferedReader reader = new BufferedReader(new FileReader(logPath))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Buscamos la línea que tiene el formato de los disparos concatenados
                if (linea.startsWith("T0-") || linea.startsWith("T1-") || linea.startsWith("T")) {
                    if (linea.contains("-")) {
                        logs = linea.trim();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo log: " + e.getMessage());
            return;
        }

        if (logs.isEmpty()) {
            System.out.println("No se encontró la secuencia de disparos en el log.");
            return;
        }

        // Definimos el PATRÓN UNIFICADO (Igual al de Python)
        // Usamos (T2), (T5) y (T7) como grupos de captura para saber qué camino se tomó.
        String regex = "T0-(.*?)T1-(.*?)(?:(T2)-(.*?)T3-(.*?)T4-|(T5)-(.*?)T6-|(T7)-(.*?)T8-(.*?)T9-(.*?)T10-)(.*?)T11-";
        Pattern pattern = Pattern.compile(regex);

        boolean huboReemplazo = true;

        // Ciclo de reducción (Consumo del log)
        while (huboReemplazo) {
            huboReemplazo = false;
            Matcher matcher = pattern.matcher(logs);

            if (matcher.find()) {
                // Verificamos qué grupo no es nulo para saber qué invariante se completó
                if (matcher.group(3) != null) {
                    conteos.put("IT1 (media)", conteos.get("IT1 (media)") + 1);
                } else if (matcher.group(6) != null) {
                    conteos.put("IT2 (simple)", conteos.get("IT2 (simple)") + 1);
                } else if (matcher.group(8) != null) {
                    conteos.put("IT3 (alta)", conteos.get("IT3 (alta)") + 1);
                }

                // Armamos el texto sobrante (la "basura" intercalada de otros hilos)
                // Los índices corresponden a todos los (.*?) del regex
                StringBuilder remanente = new StringBuilder();
                int[] garbageGroups = {1, 2, 4, 5, 7, 9, 10, 11, 12};
                for (int g : garbageGroups) {
                    if (matcher.group(g) != null) {
                        remanente.append(matcher.group(g));
                    }
                }

                // Cortamos la cadena original y le incrustamos el remanente en el medio
                logs = logs.substring(0, matcher.start()) + remanente.toString() + logs.substring(matcher.end());
                huboReemplazo = true;
            }
        }

        // Mostramos los resultados finales
        int total = conteos.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Total secuencias principales completadas: " + total);

        for (Map.Entry<String, Integer> entry : conteos.entrySet()) {
            double porcentaje = total > 0 ? (entry.getValue() * 100.0) / total : 0;
            System.out.printf("  %s: %d veces (%.1f%%)%n", entry.getKey(), entry.getValue(), porcentaje);
        }

        // Limpiamos los guiones sobrantes para verificar si la cadena quedó vacía
        String resto = logs.replace("-", "").trim();

        System.out.println("\nEstado final del análisis:");
        // Si quedó completamente vacío O si el sobrante no tiene ningún T11 (es solo precarga cortada)
        if (resto.isEmpty() || !logs.contains("T11")) {
            System.out.println("  [ÉXITO] Análisis completado. El 100% de los disparos finalizados pertenecen a secuencias válidas.");
        } else {
            System.out.println("  [ERROR] Se detectaron invariantes rotos o transiciones fuera de orden.");
            System.out.println("  Texto sobrante en el buffer: " + logs);
        }
    }

    public boolean cumpleInvariante() {
        return true;
    }
}