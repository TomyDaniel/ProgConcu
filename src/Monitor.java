import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private final RdP rdp;
    private final PoliticaInterface politica;
    private final ColaCondicion colaCondicion;
    private final Semaphore mutex;

    public Monitor(RdP rdp, PoliticaInterface politica) {
        this.rdp = rdp;
        this.politica = politica;
        this.colaCondicion = new ColaCondicion(rdp.getCantidadTransiciones());
        this.mutex = new Semaphore(1);
    }

    @Override
    public boolean fireTransition(int transition) throws InterruptedException {
        mutex.acquire();

        // Esperamos hasta que haya tokens suficientes
        while (!rdp.esSensibilizadaEstructural(transition)) {
            colaCondicion.encolar(transition);
            mutex.release();
            colaCondicion.esperarPor(transition);
            mutex.acquire();
        }

        // Esperamos el tiempo mínimo si la transición es temporal
        long tiempoEspera = rdp.calcularTiempoEspera(transition);
        if (tiempoEspera > 0) {
            mutex.release();
            Thread.sleep(tiempoEspera);
            mutex.acquire();
            // Reevaluamos: mientras dormíamos otro pudo consumir los tokens
            if (!rdp.esSensibilizadaEstructural(transition)) {
                colaCondicion.encolar(transition);
                mutex.release();
                return false;
            }
        }

        // Solo consultamos la política si esta transición es parte de un conflicto.
        // Las transiciones no conflictivas se aprueban directamente.
        if (politica.esTransicionConflictiva(transition)) {
            boolean[] sensibilizadas = rdp.getSensibilizadas();
            int transicionElegida = politica.elegir(sensibilizadas);

            if (transicionElegida != transition) {
                // La política prefiere otro camino, cedemos
                colaCondicion.encolar(transition);
                mutex.release();
                return false;
            }
        }

        // Disparamos
        rdp.disparar(transition);

        // Cascada: después del disparo notificamos a los hilos que pueden avanzar.
        // Para transiciones conflictivas, la política decide quién va primero.
        // Para el resto, despertamos a todos los que estén sensibilizados y esperando.
        boolean[] sensibilizadasTras = rdp.getSensibilizadas();

        // Primero: intentamos despertar una transición conflictiva si la política lo aprueba
        boolean[] posiblesConflictivas = new boolean[sensibilizadasTras.length];
        for (int i = 0; i < sensibilizadasTras.length; i++) {
            posiblesConflictivas[i] = sensibilizadasTras[i]
                    && colaCondicion.hasEnqueued(i)
                    && politica.esTransicionConflictiva(i);
        }
        int candidatoConflictivo = politica.elegir(posiblesConflictivas);
        if (candidatoConflictivo != -1) {
            colaCondicion.liberar(candidatoConflictivo);
        }

        // Segundo: despertamos todas las no conflictivas que estén sensibilizadas y esperando
        for (int i = 0; i < sensibilizadasTras.length; i++) {
            if (sensibilizadasTras[i] && colaCondicion.hasEnqueued(i) && !politica.esTransicionConflictiva(i)) {
                colaCondicion.liberar(i);
            }
        }

        mutex.release();
        return true;
    }
}