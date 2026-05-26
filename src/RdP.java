public class RdP {
    private int[] marcado;
    private final int[] marcadoInicial;
    private final int[][] matrizIncidencia;
    private final long[] tiempos;
    private long[] tSensibilizado;
    private final int[][] invariantesPlaza;
    private final int[] valoresInvariantes;

    public RdP() {
        this.marcadoInicial = new int[]{3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        this.marcado = marcadoInicial.clone();
        this.matrizIncidencia = new int[][] {
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
        // Estos tiempos solo corresponden a alfa, no ponemos los de beta dejando implicitamente que sean infinito
        this.tiempos = new long[]{0, 100, 0, 100, 100, 0, 100, 0, 100, 100, 100, 0};
        this.tSensibilizado = new long[12];
        this.invariantesPlaza = new int[][] {
                {  0,  1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0},
                {  0,  0,  0,  0,  1,  1,  1,  1,  1,  1,  1,  0},
                {  1,  1,  0,  1,  1,  1,  0,  1,  1,  1,  1,  1}
        };
        this.valoresInvariantes = new int[]{1, 1, 3};
    }

    public boolean esSensibilizada(int transition) {
        if (!esSensibilizadaEstructural(transition)) return false;
        if (tiempos[transition] > 0) {
            if (tSensibilizado[transition] == 0) {
                actualizarTiempoSensibilizado(transition);
            }
            return estaDentroVentanaTemporal(transition);
        }
        return true;
    }

    public boolean[] getSensibilizadas() {
        boolean[] sensibilizadas = new boolean[matrizIncidencia[0].length];
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

        for (int t = 0; t < matrizIncidencia[0].length; t++) {
            if (tiempos[t] > 0) {
                if (esSensibilizadaEstructural(t)) {
                    if (tSensibilizado[t] == 0) {
                        actualizarTiempoSensibilizado(t);
                    }
                } else {
                    tSensibilizado[t] = 0; // Perdió los tokens, el tiempo se resetea
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

    private boolean estaDentroVentanaTemporal(int transition) {
        return calcularTiempoEspera(transition) == 0;
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