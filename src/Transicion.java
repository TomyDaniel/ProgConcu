import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Transicion extends Thread {
    private final MonitorInterface monitor;
    private final int[] secuencia;
    private final Logger logger;
    private final AtomicInteger invariantesGlobales;
    private final int limite;
    private final AtomicBoolean apagado;

    // Contador para controlar que entren exactamente 200 tokens
    private static final AtomicInteger tokensGenerados = new AtomicInteger(0);

    public Transicion(MonitorInterface monitor, int[] secuencia, Logger logger,
                      AtomicInteger invariantesGlobales, int limite, String nombre,
                      AtomicBoolean apagado) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.logger = logger;
        this.invariantesGlobales = invariantesGlobales;
        this.limite = limite;
        this.apagado = apagado;
        this.setName(nombre);
    }

    @Override
    public void run() {
        try {
            while (!apagado.get()) {
                for (int transicion : secuencia) {

                    // Dejamos de hacer tareas cuando llegamos a 200
                    if (transicion == 0) {
                        if (tokensGenerados.incrementAndGet() > limite) {
                            return; // El generador terminó su trabajo y muere en paz
                        }
                    }

                    boolean disparado = false;
                    while (!disparado) {
                        disparado = monitor.fireTransition(transicion);
                        if (disparado) {
                            // Le avisamos al Main que un token logró salir
                            if (transicion == 11) {
                                invariantesGlobales.incrementAndGet();
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