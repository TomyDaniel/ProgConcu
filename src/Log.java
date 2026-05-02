import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log {
    private final File archivo;
    private final BufferedWriter writer;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final List<Integer> secuenciaTransiciones = new ArrayList<>();

    public Log(String nombreArchivo) throws IOException {
        this.archivo = new File(nombreArchivo);
        this.writer = new BufferedWriter(new FileWriter(archivo, false));
    }

    public synchronized void registrarDisparo(int transicion, long timestamp, int[] marcado) throws IOException {
        String tiempo = LocalDateTime.now().format(formatter);
        StringBuilder marcadoStr = new StringBuilder("[");
        for (int i = 0; i < marcado.length; i++) {
            marcadoStr.append(marcado[i]);
            if (i < marcado.length - 1) marcadoStr.append(",");
        }
        marcadoStr.append("]");

        String linea = "[" + tiempo + "] T" + transicion + " | Marcado: " + marcadoStr.toString() + "\n";
        writer.write(linea);
        writer.flush();
        secuenciaTransiciones.add(transicion);
    }

    public synchronized void escribir(String mensaje) throws IOException {
        String tiempo = LocalDateTime.now().format(formatter);
        String linea = "[" + tiempo + "] " + mensaje + "\n";
        writer.write(linea);
        writer.flush();
    }

    public synchronized void analizarInvariantesTransicion() throws IOException {
        escribir("\n=== ANÁLISIS DE INVARIANTES DE TRANSICIÓN ===");

        if (secuenciaTransiciones.isEmpty()) {
            escribir("No hay transiciones registradas");
            return;
        }

        // Patrones de invariantes de transición
        Pattern inv1 = Pattern.compile("0.*1.*2.*3.*4.*11"); // Simple
        Pattern inv2 = Pattern.compile("0.*1.*5.*6.*11");    // Media
        Pattern inv3 = Pattern.compile("0.*1.*7.*8.*9.*10.*11"); // Alta

        int conteoInv1 = 0, conteoInv2 = 0, conteoInv3 = 0;

        StringBuilder secuencia = new StringBuilder();
        for (int t : secuenciaTransiciones) {
            secuencia.append(t).append(",");
        }
        String secStr = secuencia.toString();

        // Búsqueda con ventanas deslizantes
        for (int i = 0; i < secuenciaTransiciones.size() - 5; i++) {
            StringBuilder ventana = new StringBuilder();
            for (int j = i; j < secuenciaTransiciones.size() && j < i + 10; j++) {
                ventana.append(secuenciaTransiciones.get(j)).append(".*");
            }
            String v = ventana.toString();

            if (inv1.matcher(v).find()) conteoInv1++;
            if (inv2.matcher(v).find()) conteoInv2++;
            if (inv3.matcher(v).find()) conteoInv3++;
        }

        escribir("Invariante 1 (Simple: 0→1→2→3→4→11): " + conteoInv1);
        escribir("Invariante 2 (Media: 0→1→5→6→11): " + conteoInv2);
        escribir("Invariante 3 (Alta: 0→1→7→8→9→10→11): " + conteoInv3);
        escribir("Total de transiciones disparadas: " + secuenciaTransiciones.size());
    }

    public synchronized void mostrarEstadisticas(long tiempoInicio, long tiempoFin, int[] conteoInvariantes) throws IOException {
        escribir("\n=== ESTADÍSTICAS FINALES ===");
        long tiempoTotal = tiempoFin - tiempoInicio;
        double segundos = tiempoTotal / 1000.0;
        escribir("Tiempo total de ejecución: " + tiempoTotal + " ms (" + String.format("%.2f", segundos) + " s)");
        escribir("Invariantes completados: " + (conteoInvariantes[0] + conteoInvariantes[1] + conteoInvariantes[2]));
        escribir("  - Simple (T0→T1→T2→T3→T4→T11): " + conteoInvariantes[0]);
        escribir("  - Media (T0→T1→T5→T6→T11): " + conteoInvariantes[1]);
        escribir("  - Alta (T0→T1→T7→T8→T9→T10→T11): " + conteoInvariantes[2]);
    }

    public void cerrar() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
