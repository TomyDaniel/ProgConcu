public class Monitor implements MonitorInterface {
    private final RdP redPetri;
    private final Politica politica;
    private final Log log;
    private final Object cerrojo = new Object();

    public Monitor(RdP rdp, Politica politica, Log log) {
        this.redPetri = rdp;
        this.politica = politica;
        this.log = log;
    }

    @Override
    public boolean fireTransition(int transition) {
        long tiempoInicio = System.currentTimeMillis();
        synchronized (cerrojo) {
            if (!redPetri.isSensibilizada(transition)) {
                return false;
            }
            redPetri.disparar(transition);
            try {
                redPetri.verificarInvariantesPlaza();
            } catch (RuntimeException e) {
                System.err.println("ERROR: " + e.getMessage());
                throw e;
            }
            int[] marcado = redPetri.getMarcadoActual();
            try {
                log.registrarDisparo(transition, tiempoInicio, marcado);
            } catch (Exception e) {
                System.err.println("Error al registrar disparo: " + e.getMessage());
            }
            cerrojo.notifyAll();
        }

        long tiempo = redPetri.getTiempoTransicion(transition);
        if (tiempo > 0) {
            try {
                Thread.sleep(tiempo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return true;
    }

    public int[] getSensibilizadas() {
        synchronized (cerrojo) {
            return redPetri.getSensibilizadas();
        }
    }

    public int elegirTransicion() {
        synchronized (cerrojo) {
            int[] sensibilizadas = redPetri.getSensibilizadas();
            if (sensibilizadas.length == 0) {
                return -1;
            }
            return politica.elegir(sensibilizadas);
        }
    }

    public RdP getRedPetri() {
        return redPetri;
    }

    public synchronized void esperarTransicion(int transicion) throws InterruptedException {
        synchronized (cerrojo) {
            while (!redPetri.isSensibilizada(transicion)) {
                cerrojo.wait();
            }
        }
    }
}
