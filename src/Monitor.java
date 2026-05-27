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

            if (rdp.esSensibilizadaEstructural(transition)) {
                long tiempoEspera = rdp.calcularTiempoEspera(transition);

                if (tiempoEspera == 0) {
                    rdp.disparar(transition);
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
                            colas.liberar(candidato);
                            logger.logDetalle(hilo + " despertó a hilo esperando por T" + candidato + " (handoff)");
                            mutex.release();
                            return true;
                        }
                    }

                    k = false;

                } else {
                    logger.logDetalle(hilo + " libera mutex, duerme " + tiempoEspera + "ms (T" + transition + " no sensibilizada por tiempo)");
                    mutex.release();
                    Thread.sleep(tiempoEspera);
                    mutex.acquire();
                    logger.logDetalle(hilo + " re-adquirio el mutex");
                    k = true;
                }

            } else {
                logger.logDetalle(hilo + " libera mutex, espera en cola de condicion (T" + transition + " sin tokens)");
                mutex.release();
                colas.esperar(transition);
                mutex.acquire();
                logger.logDetalle(hilo + " re-adquirio el mutex despues de ser despertado");
                k = true;
            }
        }

        logger.logDetalle(hilo + " libera mutex y sale (no habia a quien despertar)");
        mutex.release();
        return true;
    }
}