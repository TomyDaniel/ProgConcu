import java.util.concurrent.Semaphore;

public class ColaCondicion {
    private final Semaphore[] semaforos;

    public ColaCondicion(int cantidadTransiciones) {
        this.semaforos = new Semaphore[cantidadTransiciones];
        for (int i = 0; i < cantidadTransiciones; i++) {
            semaforos[i] = new Semaphore(0);
        }
    }

    public boolean[] quienesEstan() {
        boolean[] esperando = new boolean[semaforos.length];
        for (int i = 0; i < semaforos.length; i++) {
            esperando[i] = semaforos[i].hasQueuedThreads();
        }
        return esperando;
    }

    public void esperar(int transicion) {
        try {
            semaforos[transicion].acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void liberar(int transicion) {
        semaforos[transicion].release();
    }
}