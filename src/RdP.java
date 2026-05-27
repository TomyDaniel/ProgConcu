public class RdP {
    private int[] marcado = new int[]{3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
    private final int[][] matrizIncidencia = new int[][] {
            {-1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            { 1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {-1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
            { 0,  1, -1,  0,  0, -1,  0, -1,  0,  0,  0,  0},
            { 0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0,  0},
            { 0,  0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0},
            { 0,  0, -1,  0,  1, -1,  1, -1,  0,  0,  1,  0},
            { 0,  0,  0,  0,  0,  1, -1,  0,  0,  0,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0},
            { 0,  0,  0,  0,  1,  0,  1,  0,  0,  0,  1, -1}
    };
    private final long[] tiempos = new long[]{0, 100, 0, 100, 100, 0, 100, 0, 100, 100, 100, 0};
    private long[] tSensibilizado = new long[12];
    private final int[][] invariantesPlaza = new int[][] {
            {  0,  1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  1,  1,  1,  1,  1,  1,  1,  0},
            {  1,  1,  0,  1,  1,  1,  0,  1,  1,  1,  1,  1}
    };
    private final int[] valoresInvariantes = new int[]{1, 1, 3};

    public RdP() {
    }

    public boolean esSensibilizada(int transition) {
        if (!esSensibilizadaEstructural(transition)) return false;
        if (tiempos[transition] > 0) return calcularTiempoEspera(transition) == 0;
        return true;
    }

    public boolean[] getSensibilizadas() {
        boolean[] sensibilizadas = new boolean[getCantidadTransiciones()];
        for (int t = 0; t < sensibilizadas.length; t++) {
            sensibilizadas[t] = esSensibilizada(t);
        }
        return sensibilizadas;
    }

    public void disparar(int transition) {
        for (int plaza = 0; plaza < marcado.length; plaza++) {
            marcado[plaza] += matrizIncidencia[plaza][transition];
        }

        tSensibilizado[transition] = 0;

        for (int t = 0; t < getCantidadTransiciones(); t++) {
            if (tiempos[t] > 0) {
                if (esSensibilizadaEstructural(t)) {
                    if (tSensibilizado[t] == 0) {
                        actualizarTiempoSensibilizado(t);
                    }
                } else {
                    tSensibilizado[t] = 0;
                }
            }
        }

        if (!verificarInvariantesPlaza()) {
            throw new RuntimeException("Violación de invariante de plaza tras disparar T" + transition);
        }
    }

    public int getCantidadTransiciones() {
        return matrizIncidencia[0].length;
    }



    public boolean esSensibilizadaEstructural(int transition) {
        for (int plaza = 0; plaza < marcado.length; plaza++) {
            if (matrizIncidencia[plaza][transition] < 0) {
                int tokensNecesarios = -matrizIncidencia[plaza][transition];
                if (marcado[plaza] < tokensNecesarios) {
                    return false;
                }
            }
        }
        return true;
    }

    public long calcularTiempoEspera(int transition) {
        if (tiempos[transition] == 0) return 0;

        if (tSensibilizado[transition] == 0 && esSensibilizadaEstructural(transition)) {
            actualizarTiempoSensibilizado(transition);
        }

        long tiempoTranscurrido = System.currentTimeMillis() - tSensibilizado[transition];
        long restante = tiempos[transition] - tiempoTranscurrido;
        return restante > 0 ? restante : 0;
    }

    private void actualizarTiempoSensibilizado(int transition) {
        tSensibilizado[transition] = System.currentTimeMillis();
    }

    private boolean verificarInvariantesPlaza() {
        for (int i = 0; i < invariantesPlaza.length; i++) {
            int suma = 0;
            for (int plaza = 0; plaza < marcado.length; plaza++) {
                suma += invariantesPlaza[i][plaza] * marcado[plaza];
            }
            if (suma != valoresInvariantes[i]) return false;
        }
        return true;
    }
}