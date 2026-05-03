import java.util.concurrent.atomic.AtomicInteger;

public class Transicion extends Thread {
    private final MonitorInterface monitor;
    private final int[] secuencia;
    private final Logger logger;
    private final AtomicInteger invariantesGlobales;

    public Transicion(MonitorInterface monitor, int[] secuencia, Logger logger,
                      AtomicInteger invariantesGlobales, String nombre) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.logger = logger;
        this.invariantesGlobales = invariantesGlobales;
        this.setName(nombre);
    }

    @Override
    public void run() {
        try {
            // Ciclo de vida infinito del hilo. Solo termina si es interrumpido.
            while (!Thread.currentThread().isInterrupted()) {
                for (int transicion : secuencia) {
                    boolean disparado = false;
                    while (!disparado) {
                        disparado = monitor.fireTransition(transicion);
                        if (disparado) {
                            logger.log(transicion);

                            // Si acabamos de disparar T11, se completó un invariante en el sistema
                            if (transicion == 11) {
                                invariantesGlobales.incrementAndGet();
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            // El Main nos mandó la señal de interrupción porque se alcanzó la meta.
            System.out.println(getName() + " recibió la orden de finalización.");
            Thread.currentThread().interrupt(); // Restablecemos el flag por convención
        }
    }
}