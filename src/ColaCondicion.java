
import java.util.concurrent.Semaphore;

/**
 * Clase ColaCondicion
 *
 * Gestiona la espera y el despertar de los hilos que intentan disparar
 * transiciones no sensibilizadas. Cada transición tiene su propio semáforo,
 * así cuando el monitor quiere despertar a alguien, despierta exactamente
 * al hilo que espera la transición correcta.
 *
 * También contiene el mutex que protege la sección crítica del Monitor.
 */
public class ColaCondicion {

    private final Semaphore[] semaforos;
    private final Semaphore mutex;
    private final int[] encolados;
    private volatile boolean terminado;

    public ColaCondicion(int cantidadTransiciones) {
        this.semaforos = new Semaphore[cantidadTransiciones];
        this.encolados = new int[cantidadTransiciones];
        this.terminado = false;
        this.mutex = new Semaphore(1);
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

    public void acquireMutex() throws InterruptedException {
        mutex.acquire();
    }

    public void releaseMutex() {
        mutex.release();
    }

    public void setTerminado() {
        terminado = true;
        for (int i = 0; i < semaforos.length; i++) {
            semaforos[i].release();
        }
    }

    public boolean isTerminado() {
        return terminado;
    }

    public Semaphore getSemaforo(int transition) {
        return semaforos[transition];
    }
}
