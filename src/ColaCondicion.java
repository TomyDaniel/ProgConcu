import java.util.concurrent.Semaphore;

/**
 * Clase ColaCondicion
 *
 * Gestiona la espera y el despertar de los hilos que intentan disparar
 * transiciones no sensibilizadas. Cada transición tiene su propio semáforo,
 * así cuando el monitor quiere despertar a alguien, despierta exactamente
 * al hilo que espera la transición correcta.
 */
public class ColaCondicion {

    private final Semaphore[] semaforos;
    private final int[] encolados;

    public ColaCondicion(int cantidadTransiciones) {
        this.semaforos = new Semaphore[cantidadTransiciones];
        this.encolados = new int[cantidadTransiciones];
        for (int i = 0; i < cantidadTransiciones; i++) {
            semaforos[i] = new Semaphore(0);
        }
    }

    public void encolar(int transition) {
        encolados[transition]++;
    }

    public void liberar(int transition) {
        if (encolados[transition] > 0) {
            encolados[transition]--;
            semaforos[transition].release();
        }
    }

    public boolean hasEnqueued(int transition) {
        return encolados[transition] > 0;
    }

    public void esperarPor(int transition) throws InterruptedException {
        semaforos[transition].acquire();
    }
}