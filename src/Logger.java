import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Logger extends Thread {
    private final LinkedList<String> queue;
    private BufferedWriter detalleWriter;
    private BufferedWriter transicionesWriter;
    private volatile boolean finalizar;
    private final long startTime;

    public Logger(String politica) {
        this.queue = new LinkedList<>();
        this.finalizar = false;
        this.startTime = System.currentTimeMillis();

        try {
            this.detalleWriter = new BufferedWriter(new FileWriter("log.txt"));
            this.transicionesWriter = new BufferedWriter(new FileWriter("log_transiciones.txt"));

            detalleWriter.write("=== LOG DETALLADO DEL SISTEMA ===");
            detalleWriter.newLine();
            detalleWriter.write("Politica: " + politica);
            detalleWriter.newLine();
            detalleWriter.write("Inicio: " + new java.util.Date());
            detalleWriter.newLine();
            detalleWriter.write("==================================");
            detalleWriter.newLine();
            detalleWriter.flush();

            transicionesWriter.flush();
        } catch (IOException e) {
            System.err.println("Error abriendo archivos de log: " + e.getMessage());
        }

        this.setDaemon(false);
        this.setName("Logger-Thread");
    }

    public void logTransicion(int transition) {
        synchronized (this) {
            queue.addLast("T" + transition + "-");
            notifyAll();
        }
    }

    public void logDetalle(String mensaje) {
        String timestamp = "[" + (System.currentTimeMillis() - startTime) + "ms]";
        synchronized (this) {
            queue.addLast(timestamp + " " + mensaje);
            notifyAll();
        }
    }

    public void finalizar() {
        synchronized (this) {
            finalizar = true;
            queue.addLast("FIN");
            notifyAll();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String entrada;
                synchronized (this) {
                    while (queue.isEmpty()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            cerrarArchivos();
                            return;
                        }
                    }
                    entrada = queue.removeFirst();
                }

                if (entrada.equals("FIN")) {
                    break;
                }

                try {
                    if (entrada.startsWith("T")) {
                        String ts = "[" + (System.currentTimeMillis() - startTime) + "ms] ";
                        detalleWriter.write(ts + entrada);
                        detalleWriter.newLine();
                        transicionesWriter.write(entrada);
                    } else {
                        detalleWriter.write(entrada);
                        detalleWriter.newLine();
                    }
                } catch (IOException e) {
                    System.err.println("Error escribiendo log: " + e.getMessage());
                }
            }
        } finally {
            escribirResumenFinal();
            cerrarArchivos();
        }
    }

    private void escribirResumenFinal() {
        try {
            String resumen = "==================================";
            String fin = "Fin: " + new java.util.Date();
            String tiempo = "Tiempo total: " + (System.currentTimeMillis() - startTime) + " ms";

            detalleWriter.write(resumen);
            detalleWriter.newLine();
            detalleWriter.write(fin);
            detalleWriter.newLine();
            detalleWriter.write(tiempo);
            detalleWriter.newLine();
            detalleWriter.write(resumen);
            detalleWriter.newLine();
            detalleWriter.flush();

            transicionesWriter.flush();
        } catch (IOException e) {
            System.err.println("Error escribiendo resumen: " + e.getMessage());
        }
    }

    private void cerrarArchivos() {
        try {
            if (detalleWriter != null) detalleWriter.close();
            if (transicionesWriter != null) transicionesWriter.close();
        } catch (IOException e) {
            System.err.println("Error cerrando archivos de log: " + e.getMessage());
        }
    }
}
