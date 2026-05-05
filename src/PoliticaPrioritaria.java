public class PoliticaPrioritaria implements PoliticaInterface {
    private final int[] prioridades;

    public PoliticaPrioritaria() {
        this.prioridades = new int[]{5, 2, 7};
    }

    @Override
    public int elegir(boolean[] sensibilizadas) {
        // Devuelve la transición en conflicto de mayor prioridad
        for (int t : prioridades) {
            if (t < sensibilizadas.length && sensibilizadas[t]) return t;
        }

        // Si no hay conflictivas, elige la primera que esté disponible
        for (int i = 0; i < sensibilizadas.length; i++) {
            if (sensibilizadas[i]) return i;
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