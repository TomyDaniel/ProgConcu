import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoliticaAleatoria implements PoliticaInterface {
    private final int[] conflictivas;
    private final Random rand;

    public PoliticaAleatoria() {
        this.conflictivas = new int[]{5, 2, 7};
        this.rand = new Random();
    }

    @Override
    public int elegir(boolean[] sensibilizadas) {
        List<Integer> conflictivasDisponibles = new ArrayList<>();
        List<Integer> todasDisponibles = new ArrayList<>();

        for (int i = 0; i < sensibilizadas.length; i++) {
            if (sensibilizadas[i]) {
                todasDisponibles.add(i);
                if (esTransicionConflictiva(i)) {
                    conflictivasDisponibles.add(i);
                }
            }
        }

        // Prioridad a resolver conflictos
        if (!conflictivasDisponibles.isEmpty()) {
            return conflictivasDisponibles.get(rand.nextInt(conflictivasDisponibles.size()));
        }
        // Si no hay conflictivas, despierta a cualquiera que esté esperando
        else if (!todasDisponibles.isEmpty()) {
            return todasDisponibles.get(rand.nextInt(todasDisponibles.size()));
        }

        return -1;
    }

    @Override
    public boolean esTransicionConflictiva(int transition) {
        for (int t : conflictivas) {
            if (t == transition) return true;
        }
        return false;
    }
}