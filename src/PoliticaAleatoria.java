import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoliticaAleatoria implements PoliticaInterface {

    // Transiciones en conflicto entre los tres caminos
    private final int[] conflictivas;
    private final Random rand;

    public PoliticaAleatoria() {
        this.conflictivas = new int[]{5, 2, 7};
        this.rand = new Random();
    }

    @Override
    public int elegir(boolean[] sensibilizadas) {
        List<Integer> disponibles = new ArrayList<>();
        for (int t : conflictivas) {
            if (t < sensibilizadas.length && sensibilizadas[t]) disponibles.add(t);
        }
        if (disponibles.isEmpty()) return -1;
        return disponibles.get(rand.nextInt(disponibles.size()));
    }

    @Override
    public boolean esTransicionConflictiva(int transition) {
        for (int t : conflictivas) {
            if (t == transition) return true;
        }
        return false;
    }
}