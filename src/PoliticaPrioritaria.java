import java.util.ArrayList;
import java.util.List;

public class PoliticaPrioritaria implements PoliticaInterface {

    // T5 (simple) > T2 (media) > T7 (alta)
    private final int[] prioridades;

    public PoliticaPrioritaria() {
        this.prioridades = new int[]{5, 2, 7};
    }

    @Override
    public int elegir(boolean[] sensibilizadas) {
        List<Integer> disponibles = obtenerDisponibles(sensibilizadas);
        if (disponibles.isEmpty()) return -1;
        for (int transicion : prioridades) {
            if (disponibles.contains(transicion)) return transicion;
        }
        return disponibles.get(0);
    }

    private List<Integer> obtenerDisponibles(boolean[] sensibilizadas) {
        List<Integer> disponibles = new ArrayList<>();
        for (int i = 0; i < sensibilizadas.length; i++) {
            if (sensibilizadas[i]) disponibles.add(i);
        }
        return disponibles;
    }
}