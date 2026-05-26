import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger extends Thread {
    private final BlockingQueue<String> queue;
    private BufferedWriter writer;
    private BufferedWriter statsWriter;
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
            this.statsWriter = new BufferedWriter(new FileWriter("log_estadisticas.txt"));

            String encabezado1 = "=== LOG DE DISPAROS ===";
            String encabezado2 = "Política: " + politica;
            String encabezado3 = "Inicio: " + new java.util.Date();
            String encabezado4 = "======================";

            writer.write(encabezado1);
            writer.newLine();

            writer.write(encabezado2);
            writer.newLine();

            writer.write(encabezado3);
            writer.newLine();

            writer.write(encabezado4);
            writer.newLine();

            writer.flush();
        } catch (IOException e) {
            System.err.println("Error abriendo archivos de log: " + e.getMessage());
        }

        this.setDaemon(false);
        this.setName("Logger-Thread");
    }

    public void log(int transition) {
        try {
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
                    writer.newLine();
                    statsWriter.newLine();
                    break;
                }
                writer.write(entrada);
                statsWriter.write(entrada);
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        } finally {
            escribirResumenFinal();
            cerrarArchivos();
        }
    }

    private void escribirResumenFinal() {
        try {
            String resumen1 = "======================";
            String resumen2 = "Fin: " + new java.util.Date();
            String resumen3 = "Tiempo total: " + getTotalTime() + " ms";
            String resumen4 = "======================";

            writer.write(resumen1);
            writer.newLine();

            writer.write(resumen2);
            writer.newLine();

            writer.write(resumen3);
            writer.newLine();

            writer.write(resumen4);
            writer.newLine();

            writer.flush();
            statsWriter.flush();
        } catch (IOException e) {
            System.err.println("Error escribiendo resumen: " + e.getMessage());
        }
    }

    private void cerrarArchivos() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (statsWriter != null) {
                statsWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando archivos de log: " + e.getMessage());
        }
    }
}

