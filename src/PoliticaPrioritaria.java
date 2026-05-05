import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoliticaPrioritaria implements PoliticaInterface {

    // Transiciones en conflicto, en orden de prioridad: T5 (simple) > T2 (media) > T7 (alta)
    private final int[] prioridades;

    public PoliticaPrioritaria() {
        this.prioridades = new int[]{5, 2, 7};
    }

    @Override
    public int elegir(boolean[] sensibilizadas) {
        // Devuelve la transición de mayor prioridad que esté sensibilizada
        for (int t : prioridades) {
            if (t < sensibilizadas.length && sensibilizadas[t]) return t;
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