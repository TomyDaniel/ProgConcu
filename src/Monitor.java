public class Monitor implements MonitorInterface {
    private final RdP rdp;
    private final PoliticaInterface politica;
    private final ColaCondicion colaCondicion;

    public Monitor(RdP rdp, PoliticaInterface politica) {
        this.rdp = rdp;
        this.politica = politica;
        this.colaCondicion = new ColaCondicion(rdp.getCantidadTransiciones());
    }

    @Override
    public boolean fireTransition(int transition) throws InterruptedException {
        colaCondicion.acquireMutex();

        while (true) {
            // Esperamos por tokens (sensibilización estructural)
            while (!rdp.esSensibilizadaEstructural(transition)) {
                colaCondicion.encolar(transition);
                colaCondicion.releaseMutex();

                // Dormimos esperando que otro hilo nos despierte
                colaCondicion.esperarPor(transition);

                colaCondicion.acquireMutex();
            }

            // Comprobamos si hay que esperar por el temporizador
            long tiempoEspera = rdp.calcularTiempoEspera(transition);
            if (tiempoEspera > 0) {
                colaCondicion.releaseMutex();

                // Dormimos el tiempo exacto soltando el monitor para no bloquear a otros
                Thread.sleep(tiempoEspera);

                colaCondicion.acquireMutex();
                continue; // Reevaluamos desde 0 porque mientras dormíamos, el estado pudo cambiar
            }

            // Si llegamos acá, la transición está completamente lista (tokens + tiempo)
            break;
        }

        boolean[] sensibilizadas = rdp.getSensibilizadas();
        boolean[] posibles = new boolean[sensibilizadas.length];

        for (int i = 0; i < sensibilizadas.length; i++) {
            posibles[i] = sensibilizadas[i] && (colaCondicion.hasEnqueued(i) || i == transition);
        }

        int transicionElegida = politica.elegir(posibles);

        if (transicionElegida == transition) {
            rdp.disparar(transition);

            // CASCADA
            boolean[] sensibilizadasTrasDisparo = rdp.getSensibilizadas();
            boolean[] posiblesTrasDisparo = new boolean[sensibilizadasTrasDisparo.length];
            for (int i = 0; i < sensibilizadasTrasDisparo.length; i++) {
                posiblesTrasDisparo[i] = sensibilizadasTrasDisparo[i] && colaCondicion.hasEnqueued(i);
            }

            int transicionCandidato = politica.elegir(posiblesTrasDisparo);

            if (transicionCandidato != -1) {
                colaCondicion.liberar(transicionCandidato);
            }

            colaCondicion.releaseMutex();
            return true;
        } else {
            // Si la política eligió otra, nos encolamos
            colaCondicion.encolar(transition);
            colaCondicion.releaseMutex();
            return false;
        }
    }
}