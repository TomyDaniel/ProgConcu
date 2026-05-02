import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=== SIMULADOR RED DE PETRI - 200 INVARIANTES ===\n");

        RdP rdp = new RdP();
        Politica politica = new PoliticaAleatoria();
        Log log = new Log("logs/ejecucion.log");

        try {
            Monitor monitor = new Monitor(rdp, politica, log);

            // Definir los 3 invariantes de transición
            int[] invariante1 = {0, 1, 2, 3, 4, 11};      // Simple
            int[] invariante2 = {0, 1, 5, 6, 11};         // Media
            int[] invariante3 = {0, 1, 7, 8, 9, 10, 11}; // Alta

            log.escribir("Sistema iniciado con 3 hilos");
            log.escribir("Marcado inicial: " + arrayToString(rdp.getMarcadoActual()));

            long tiempoInicio = System.currentTimeMillis();
            System.out.println("Tiempo inicio: " + tiempoInicio);

            // Crear 3 hilos (uno para cada invariante)
            List<Thread> hilos = new ArrayList<>();
            Thread hilo1 = new Thread(new Hilos(monitor, invariante1, "Hilo-Simple", 0));
            Thread hilo2 = new Thread(new Hilos(monitor, invariante2, "Hilo-Media", 1));
            Thread hilo3 = new Thread(new Hilos(monitor, invariante3, "Hilo-Alta", 2));

            hilos.add(hilo1);
            hilos.add(hilo2);
            hilos.add(hilo3);

            // Iniciar los 3 hilos
            for (Thread h : hilos) {
                h.start();
            }

            // Esperar a que terminen
            for (Thread h : hilos) {
                h.join();
            }

            long tiempoFin = System.currentTimeMillis();
            long tiempoTotal = tiempoFin - tiempoInicio;

            System.out.println("\n=== RESULTADOS ===");
            System.out.println("Invariantes completados: " + Hilos.invariantesCompletados.get());
            System.out.println("Tiempo total: " + tiempoTotal + " ms (" + String.format("%.2f", tiempoTotal / 1000.0) + " s)");
            System.out.println("  - Simple: " + Hilos.conteoInvariantes[0]);
            System.out.println("  - Media: " + Hilos.conteoInvariantes[1]);
            System.out.println("  - Alta: " + Hilos.conteoInvariantes[2]);

            if (tiempoTotal < 20000 || tiempoTotal > 40000) {
                System.out.println("⚠ Tiempo fuera del rango 20-40 segundos");
            } else {
                System.out.println("✓ Tiempo dentro del rango requerido");
            }

            log.escribir("\nMarcado final: " + arrayToString(rdp.getMarcadoActual()));
            log.analizarInvariantesTransicion();
            log.mostrarEstadisticas(tiempoInicio, tiempoFin, Hilos.conteoInvariantes);
            log.escribir("Sistema finalizado");

            log.cerrar();
            System.out.println("\nLog: logs/ejecucion.log");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            log.cerrar();
        }
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
