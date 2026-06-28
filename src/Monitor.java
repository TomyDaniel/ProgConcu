public class Monitor implements MonitorInterface {
    private final RdP rdp;
    private final Mutex mutex;
    private final ColaCondicion colas;
    private final PoliticaInterface politica;
    private final Logger logger;

    public Monitor(RdP rdp, Mutex mutex, ColaCondicion colas, PoliticaInterface politica, Logger logger) {
        this.rdp = rdp;
        this.mutex = mutex;
        this.colas = colas;
        this.politica = politica;
        this.logger = logger;
    }

    @Override
    public boolean fireTransition(int transition) throws InterruptedException {
        String hilo = Thread.currentThread().getName();

        mutex.acquire();
        logger.logDetalle(hilo + " adquirio el mutex (fireTransition T" + transition + ")");

        boolean k = true;

        while (k) {
            if (Thread.currentThread().isInterrupted()) {
                logger.logDetalle(hilo + " interrumpido, libera el mutex");
                mutex.release();
                throw new InterruptedException();
            }

            boolean resultado = rdp.disparar(transition);

            if (resultado) {
                logger.logDetalle(hilo + " disparo T" + transition);
                logger.logTransicion(transition);

                boolean[] Vs = rdp.getSensibilizadas();
                boolean[] Vc = colas.quienesEstan();

                boolean[] m = new boolean[Vs.length];
                boolean hayParaDespertar = false;
                for (int i = 0; i < m.length; i++) {
                    m[i] = Vs[i] && Vc[i];
                    if (m[i]) {
                        hayParaDespertar = true;
                    }
                }

                if (hayParaDespertar) {
                    int candidato = politica.elegir(m, Vs);
                    if (candidato != -1) {
                        logger.logDetalle(hilo + " despertó a hilo esperando por T" + candidato + " (handoff)");
                        colas.liberar(candidato);
                        return true;
                    }
                }

                k = false;
            } else {
                if (!rdp.esSensibilizadaEstructural(transition)) {
                    logger.logDetalle(hilo + " libera mutex, espera en cola de condicion (T" + transition + " sin tokens)");
                    mutex.release();
                    colas.esperar(transition);
                    logger.logDetalle(hilo + " hereda el mutex por handoff");
                    k = true;
                } else {
                    long tiempoEspera = rdp.calcularTiempoEspera(transition);
                    logger.logDetalle(hilo + " libera mutex, duerme " + tiempoEspera + "ms (T" + transition + " no sensibilizada por tiempo)");
                    mutex.release();
                    Thread.sleep(tiempoEspera);
                    mutex.acquire();
                    logger.logDetalle(hilo + " re-adquirio el mutex");
                    k = true;
                }
            }
        }

        logger.logDetalle(hilo + " libera mutex y sale");
        mutex.release();
        return true;
    }
}
