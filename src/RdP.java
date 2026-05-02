public class RdP {
    private final int[][] matrizIncidencia = {
        // T0  T1  T2  T3  T4  T5  T6  T7  T8  T9  T10 T11
        {-1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},  //P0
        { 1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},  //P1
        {-1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},  //P2
        { 0,  1, -1,  0,  0, -1,  0, -1,  0,  0,  0,  0},  //P3
        { 0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0,  0},  //P4
        { 0,  0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0},  //P5
        { 0,  0, -1,  0,  1, -1,  1, -1,  0,  0,  1,  0},  //P6
        { 0,  0,  0,  0,  0,  1, -1,  0,  0,  0,  0,  0},  //P7
        { 0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0,  0},  //P8
        { 0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0},  //P9
        { 0,  0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0},  //P10
        { 0,  0,  0,  0,  1,  0,  1,  0,  0,  0,  1, -1}   //P11
    };

    private final int[] marcadoInicial = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
    private int[] marcadoActual;
    private int[][] matrizIncidenciaT;
    private final int cantidadTransiciones = 12;
    private final int cantidadPlazas = 12;
    private long[] timestamps;

    private final long[] tiemposTransiciones = {
        0,      // T0 = instantáneo
        100,    // T1 = 100ms (transferencia al buffer)
        0,      // T2 = instantáneo
        80,     // T3 = 80ms (etapa media)
        100,    // T4 = 100ms (finalizar media)
        0,      // T5 = instantáneo
        80,     // T6 = 80ms (finalizar simple)
        0,      // T7 = instantáneo
        80,     // T8 = 80ms (etapa alta 1)
        80,     // T9 = 80ms (etapa alta 2)
        100,    // T10 = 100ms (finalizar alta)
        0       // T11 = instantáneo
    };

    public RdP() {
        this.marcadoActual = marcadoInicial.clone();
        initMatrizTranspuesta();
        this.timestamps = new long[cantidadTransiciones];
        for (int i = 0; i < cantidadTransiciones; i++) {
            timestamps[i] = 0;
        }
    }

    private void initMatrizTranspuesta() {
        matrizIncidenciaT = new int[cantidadTransiciones][cantidadPlazas];
        for (int i = 0; i < cantidadPlazas; i++) {
            for (int j = 0; j < cantidadTransiciones; j++) {
                matrizIncidenciaT[j][i] = matrizIncidencia[i][j];
            }
        }
    }

    public boolean isSensibilizada(int transicion) {
        if (transicion < 0 || transicion >= cantidadTransiciones) {
            return false;
        }
        for (int plaza = 0; plaza < cantidadPlazas; plaza++) {
            if (matrizIncidenciaT[transicion][plaza] < 0 &&
                marcadoActual[plaza] < Math.abs(matrizIncidenciaT[transicion][plaza])) {
                return false;
            }
        }
        return true;
    }

    public void disparar(int transicion) {
        if (!isSensibilizada(transicion)) {
            throw new IllegalArgumentException("Transición " + transicion + " no está sensibilizada");
        }
        for (int plaza = 0; plaza < cantidadPlazas; plaza++) {
            marcadoActual[plaza] += matrizIncidencia[plaza][transicion];
        }
        timestamps[transicion] = System.currentTimeMillis();
    }

    public void verificarInvariantesPlaza() {
        // Invariante 1: P0 + P1 + P3 + P4 + P5 + P7 + P8 + P9 + P10 + P11 = 3
        int suma1 = marcadoActual[0] + marcadoActual[1] + marcadoActual[3] + marcadoActual[4] +
                    marcadoActual[5] + marcadoActual[7] + marcadoActual[8] + marcadoActual[9] +
                    marcadoActual[10] + marcadoActual[11];
        if (suma1 != 3) {
            throw new RuntimeException("Invariante de plaza 1 violado: suma=" + suma1);
        }

        // Invariante 2: P1 + P2 = 1
        int suma2 = marcadoActual[1] + marcadoActual[2];
        if (suma2 != 1) {
            throw new RuntimeException("Invariante de plaza 2 violado: suma=" + suma2);
        }

        // Invariante 3: P10 + P4 + P5 + P6 + P7 + P8 + P9 = 1
        int suma3 = marcadoActual[10] + marcadoActual[4] + marcadoActual[5] + marcadoActual[6] +
                    marcadoActual[7] + marcadoActual[8] + marcadoActual[9];
        if (suma3 != 1) {
            throw new RuntimeException("Invariante de plaza 3 violado: suma=" + suma3);
        }
    }

    public long getTiempoTransicion(int transicion) {
        if (transicion < 0 || transicion >= cantidadTransiciones) {
            return 0;
        }
        return tiemposTransiciones[transicion];
    }

    public synchronized int[] getSensibilizadas() {
        int[] sensibilizadas = new int[cantidadTransiciones];
        int contador = 0;
        for (int i = 0; i < cantidadTransiciones; i++) {
            if (isSensibilizada(i)) {
                sensibilizadas[contador++] = i;
            }
        }
        int[] resultado = new int[contador];
        System.arraycopy(sensibilizadas, 0, resultado, 0, contador);
        return resultado;
    }

    public synchronized int[] getMarcadoActual() {
        return marcadoActual.clone();
    }

    public int getCantidadTransiciones() {
        return cantidadTransiciones;
    }

    public int getCantidadPlazas() {
        return cantidadPlazas;
    }
}
