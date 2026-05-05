import java.util.concurrent.atomic.AtomicInteger;

public class Transicion extends Thread {
    private final MonitorInterface monitor;
    private final int[] secuencia;
    private final Logger logger;
    private final AtomicInteger invariantesGlobales;
    private final int limite;

    public Transicion(MonitorInterface monitor, int[] secuencia, Logger logger,
                      AtomicInteger invariantesGlobales, int limite, String nombre) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.logger = logger;
        this.invariantesGlobales = invariantesGlobales;
        this.limite = limite;
        this.setName(nombre);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                for (int transicion : secuencia) {

                    // Verificación atómica PREVIA al disparo de la última transición
                    if (transicion == 11) {
                        if (invariantesGlobales.incrementAndGet() > limite) {
                            // Revertimos porque no obtuvimos el permiso para disparar
                            invariantesGlobales.decrementAndGet();
                            return; // Este hilo terminó su cuota
                        }
                    }

                    boolean disparado = false;
                    while (!disparado) {
                        disparado = monitor.fireTransition(transicion);
                        if (disparado) {
                            logger.log(transicion);

                            // Si disparamos T11 y somos el hilo 2000, forzamos la salida
                            if (transicion == 11) {
                                if (invariantesGlobales.get() == limite) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println(getName() + " recibió la orden de finalización.");
            Thread.currentThread().interrupt();
        }
    }
}