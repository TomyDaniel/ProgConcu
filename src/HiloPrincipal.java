public class HiloPrincipal implements Runnable {
    private final Monitor monitor;
    private final int[] secuencia;
    private final String nombre;

    public HiloPrincipal(Monitor monitor, int[] secuencia, String nombre) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.nombre = nombre;
    }

    @Override
    public void run() {
        try {
            while (Hilos.sistemaEnEjecucion && Hilos.invariantesCompletados.get() < 200) {
                boolean exito = ejecutarSecuencia();
                if (!exito) {
                    Thread.sleep(10);
                }
            }
            System.out.println("[" + nombre + "] Terminado");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[" + nombre + "] Interrumpido");
        } catch (Exception e) {
            System.out.println("[" + nombre + "] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean ejecutarSecuencia() throws InterruptedException {
        for (int transicion : secuencia) {
            boolean exito = false;
            int intentos = 0;
            while (!exito && intentos < 100) {
                try {
                    monitor.esperarTransicion(transicion);
                    exito = monitor.fireTransition(transicion);
                    if (exito) {
                        System.out.println("[" + nombre + "] T" + transicion + " disparada");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                if (!exito) {
                    Thread.sleep(10);
                    intentos++;
                }
            }
            if (!exito) {
                return false;
            }
        }
        return true;
    }
}
