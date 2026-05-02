import java.util.concurrent.atomic.AtomicInteger;

public class Hilos implements Runnable {
    private final Monitor monitor;
    private final int[] secuencia;
    private final String nombre;
    private final int tipoInvariante;

    public static AtomicInteger invariantesCompletados = new AtomicInteger(0);
    public static volatile boolean sistemaEnEjecucion = true;
    public static int[] conteoInvariantes = {0, 0, 0};
    private static final int INVARIANTES_META = 200;

    public Hilos(Monitor monitor, int[] secuencia, String nombre, int tipoInvariante) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.nombre = nombre;
        this.tipoInvariante = tipoInvariante;
    }

    @Override
    public void run() {
        try {
            while (invariantesCompletados.get() < INVARIANTES_META) {
                ejecutarInvariante();

                int completados = invariantesCompletados.incrementAndGet();
                synchronized (Hilos.class) {
                    conteoInvariantes[tipoInvariante]++;
                }
                System.out.println("[" + nombre + "] Invariante #" + completados + " completado");

                if (completados >= INVARIANTES_META) {
                    sistemaEnEjecucion = false;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("[" + nombre + "] Error: " + e.getMessage());
        }
    }

    private void ejecutarInvariante() throws Exception {
        for (int i = 0; i < secuencia.length; i++) {
            int transicion = secuencia[i];
            boolean exito = false;
            int reintentos = 0;

            // No chequear sistemaEnEjecucion aquí - permitir terminar el invariante en progreso
            while (!exito && reintentos < 500) {
                try {
                    exito = monitor.fireTransition(transicion);
                    if (exito) {
                        System.out.println("[" + nombre + "] T" + transicion);
                    } else {
                        Thread.sleep(5);
                        reintentos++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }

            if (!exito) {
                throw new Exception("No se pudo disparar T" + transicion + " después de " + reintentos + " intentos");
            }
        }
    }
}
