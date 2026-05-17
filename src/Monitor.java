public class Monitor implements MonitorInterface {
    private final RdP rdp;
    private final Mutex mutex;
    private final ColaCondicion colas;
    private final PoliticaInterface politica;

    public Monitor(RdP rdp, Mutex mutex, ColaCondicion colas, PoliticaInterface politica) {
        this.rdp = rdp;
        this.mutex = mutex;
        this.colas = colas;
        this.politica = politica;
    }

    @Override
    public boolean fireTransition(int transition) throws InterruptedException {
        mutex.acquire(); // 00 acquire()
        boolean k = true;

        while (k) {
            // Si el Main nos interrumpe, salimos soltando el mutex
            if (Thread.currentThread().isInterrupted()) {
                mutex.release();
                throw new InterruptedException();
            }

            // Verificamos si podemos disparar por estructura (si hay tokens)
            if (rdp.esSensibilizadaEstructural(transition)) {

                // Verificamos si estamos dentro de la ventana de tiempo
                long tiempoEspera = rdp.calcularTiempoEspera(transition);

                if (tiempoEspera == 0) {
                    // ¡Tokens OK y Tiempo OK! Disparamos.
                    rdp.disparar(transition);

                    // 00 sensibilizadas() y quienesEstan()
                    boolean[] Vs = rdp.getSensibilizadas();
                    boolean[] Vc = colas.quienesEstan();

                    // Ecuación del diagrama: m = Vs AND Vc
                    boolean[] m = new boolean[Vs.length];
                    boolean hayParaDespertar = false;
                    for (int i = 0; i < m.length; i++) {
                        m[i] = Vs[i] && Vc[i];
                        if (m[i]) {
                            hayParaDespertar = true;
                        }
                    }

                    if (hayParaDespertar) { // alt [m > 0]
                        // 00 cual()
                        int candidato = politica.elegir(m, Vs);
                        if (candidato != -1) {
                            // 00 release() a la cola
                            colas.liberar(candidato);

                            // Política Signal and Exit (Handoff).
                            // Retornamos TRUE, pero NO liberamos el Mutex.
                            return true;
                        }
                    }

                    // Si no hay a quién despertar (m == 0), rompemos el loop
                    k = false;

                } else {
                    // Tiene tokens, pero NO cumplió el tiempo.
                    // Debe dormir FUERA del monitor para no estorbar.
                    mutex.release();
                    Thread.sleep(tiempoEspera);
                    mutex.acquire(); // Volvemos a entrar tras despertar
                    k = true; // Volvemos a evaluar el ciclo
                }

            } else {
                // NO tiene tokens. Va a la cola de condición correspondiente.
                mutex.release();
                colas.esperar(transition);

                // Cuando el hilo despierte acá, es porque OTRO hilo le hizo colas.liberar().
                // Por la regla del Handoff, ya tiene el Mutex regalado.
                k = true;
            }
        }

        // Sólo llegamos aquí si disparamos exitosamente pero no había a nadie a quien despertar
        mutex.release(); // 00 release() del Mutex
        return true;
    }
}