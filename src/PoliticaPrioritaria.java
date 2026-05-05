public class PoliticaPrioritaria implements PoliticaInterface {
    private final int[] prioridades;

    public PoliticaPrioritaria() {
        this.prioridades = new int[]{5, 2, 7};
    }

    @Override
    public int elegir(boolean[] m, boolean[] Vs) {
        // Prioridad Absoluta al Camino Simple (T5)
        if (Vs[5]) { // Si hay datos y procesador disponible para el Simple...
            if (m[5]) {
                return 5; // El hilo Simple ya llegó a la cola, lo despertamos.
            } else {
                // El hilo Simple está ocupado (yendo a T11), pero le guardamos el recurso.
                // Retornamos -1 para evitar que los hilos Medio y Alto se lo roben.
                return -1;
            }
        }

        // Solo si NO hay datos para el Simple, dejamos pasar a los demás
        if (m[2]) return 2;
        if (m[7]) return 7;

        // Retorno por defecto
        for (int i = 0; i < m.length; i++) {
            if (m[i]) return i;
        }
        return -1;
    }

    @Override
    public boolean esTransicionConflictiva(int transition) {
        for (int t : prioridades) {
            if (t == transition) return true;
        }
        return false;
    }
}