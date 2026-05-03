import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger extends Thread {
    private final BlockingQueue<String> queue;
    private BufferedWriter writer;
    private final AtomicBoolean finalizar;
    private final String nombreArchivo;
    private final long startTime;

    public Logger(String nombreArchivo, String politica) {
        this.queue = new LinkedBlockingQueue<>();
        this.finalizar = new AtomicBoolean(false);
        this.nombreArchivo = nombreArchivo;
        this.startTime = System.currentTimeMillis();

        try {
            this.writer = new BufferedWriter(new FileWriter(nombreArchivo));
            writer.write("=== LOG DE DISPAROS ===");
            writer.newLine();
            writer.write("Política: " + politica);
            writer.newLine();
            writer.write("Inicio: " + new java.util.Date());
            writer.newLine();
            writer.write("======================");
            writer.newLine();
            // Dejamos la línea lista para que los hilos empiecen a escribir sus secuencias
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error abriendo archivo de log: " + e.getMessage());
        }

        this.setDaemon(false);
        this.setName("Logger-Thread");
    }

    public void log(int transition) {
        try {
            // Formato lineal: T0-T1-T5-
            queue.put("T" + transition + "-");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void finalizar() {
        finalizar.set(true);
        try {
            queue.put("FIN");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isTerminado() {
        return finalizar.get() && queue.isEmpty();
    }

    public long getTotalTime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String entrada = queue.take();
                if (entrada.equals("FIN")) {
                    writer.newLine(); // Hacemos el salto de línea al terminar toda la cadena
                    break;
                }
                // Escribimos en la misma línea sin saltos
                writer.write(entrada);
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        } finally {
            escribirResumenFinal();
            cerrarArchivo();
        }
    }

    private void escribirResumenFinal() {
        try {
            writer.write("======================");
            writer.newLine();
            writer.write("Fin: " + new java.util.Date());
            writer.newLine();
            writer.write("Tiempo total: " + getTotalTime() + " ms");
            writer.newLine();
            writer.write("======================");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error escribiendo resumen: " + e.getMessage());
        }
    }

    private void cerrarArchivo() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando archivo de log: " + e.getMessage());
        }
    }
}