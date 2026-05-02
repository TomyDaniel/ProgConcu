import java.util.Random;

public class PoliticaAleatoria extends Politica {
    private final Random random = new Random();

    @Override
    public int elegir(int[] sensibilizadas) {
        if (sensibilizadas.length == 0) {
            throw new IllegalArgumentException("No hay transiciones sensibilizadas");
        }
        return sensibilizadas[random.nextInt(sensibilizadas.length)];
    }
}
