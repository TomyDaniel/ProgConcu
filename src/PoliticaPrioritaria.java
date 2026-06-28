public class PoliticaPrioritaria implements PoliticaInterface {
    public PoliticaPrioritaria() {
    }

    @Override
    public int elegir(boolean[] m, boolean[] Vs) {
        // Prioridad al Camino Simple (T5) si está disponible
        if (m[5]) {
            return 5;
        }

        // Si Simple no está disponible, elegir aleatoriamente entre Media y Alta
        boolean mediaDisponible = m[2];
        boolean altaDisponible = m[7];

        if (mediaDisponible && altaDisponible) {
            // Ambos disponibles, elegir aleatoriamente
            return Math.random() < 0.5 ? 2 : 7;
        } else if (mediaDisponible) {
            return 2;
        } else if (altaDisponible) {
            return 7;
        }

        // Si ninguno de los tres está disponible, buscar cualquier otro
        for (int i = 0; i < m.length; i++) {
            if (m[i]) return i;
        }
        return -1;
    }

}