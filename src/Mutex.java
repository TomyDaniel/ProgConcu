import java.util.concurrent.Semaphore;

public class Mutex {
    // Usamos fairness (true) para evitar inanición en la puerta de entrada
    private final Semaphore mutex = new Semaphore(1, true);

    public void acquire() {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void release() {
        mutex.release();
    }
}