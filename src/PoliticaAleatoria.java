import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoliticaAleatoria implements PoliticaInterface {

    private final Random rand;

    public PoliticaAleatoria() {
        this.rand = new Random();
    }

    @Override
    public int elegir(boolean[] sensibilizadas) {
        List<Integer> disponibles = obtenerDisponibles(sensibilizadas);
        if (disponibles.isEmpty()) return -1;
        return disponibles.get(rand.nextInt(disponibles.size()));
    }

    private List<Integer> obtenerDisponibles(boolean[] sensibilizadas) {
        List<Integer> disponibles = new ArrayList<>();
        for (int i = 0; i < sensibilizadas.length; i++) {
            if (sensibilizadas[i]) disponibles.add(i);
        }
        return disponibles;
    }
}