import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Logger extends Thread {
    private final LinkedList<LogEntry> queue;
    private BufferedWriter detalleWriter;
    private BufferedWriter transicionesWriter;
    private volatile boolean finalizar;
    private final long startTime;

    private static class LogEntry {
        final String mensaje;
        final boolean esTransicion;
        final long timestamp;

        LogEntry(String mensaje, boolean esTransicion) {
            this.mensaje = mensaje;
            this.esTransicion = esTransicion;
            this.timestamp = System.currentTimeMillis();
        }
    }

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
        LogEntry entry = new LogEntry("T" + transition + "-", true);
        synchronized (this) {
            queue.addLast(entry);
            notifyAll();
        }
    }

    public void logDetalle(String mensaje) {
        String timestamp = "[" + (System.currentTimeMillis() - startTime) + "ms]";
        LogEntry entry = new LogEntry(timestamp + " " + mensaje, false);
        synchronized (this) {
            queue.addLast(entry);
            notifyAll();
        }
    }

    public void finalizar() {
        synchronized (this) {
            finalizar = true;
            queue.addLast(new LogEntry("FIN", false));
            notifyAll();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                LogEntry entrada;
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

                if (entrada.mensaje.equals("FIN")) {
                    break;
                }

                try {
                    if (entrada.esTransicion) {
                        String ts = "[" + (entrada.timestamp - startTime) + "ms] ";
                        detalleWriter.write(ts + entrada.mensaje);
                        detalleWriter.newLine();
                        transicionesWriter.write(entrada.mensaje);
                    } else {
                        detalleWriter.write(entrada.mensaje);
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
