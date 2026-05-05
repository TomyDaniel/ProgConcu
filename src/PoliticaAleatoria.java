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
    public int elegir(boolean[] m, boolean[] Vs) {
        // La política aleatoria no hace "reserva de recursos" mirando Vs.
        // Simplemente busca quiénes tienen tokens Y están esperando en la cola (m).

        List<Integer> opcionesValidas = new ArrayList<>();

        // Recolectamos todas las transiciones que están listas para ser despertadas
        for (int i = 0; i < m.length; i++) {
            if (m[i]) {
                opcionesValidas.add(i);
            }
        }

        // Si no hay nadie a quien despertar, retornamos -1
        if (opcionesValidas.isEmpty()) {
            return -1;
        }

        // Elegimos una transición al azar de la lista
        int indiceAleatorio = rand.nextInt(opcionesValidas.size());
        return opcionesValidas.get(indiceAleatorio);
    }

    @Override
    public boolean esTransicionConflictiva(int transition) {
        for (int t : conflictivas) {
            if (t == transition) return true;
        }
        return false;
    }
}