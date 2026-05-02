public class PoliticaPriorizada extends Politica {
    @Override
    public int elegir(int[] sensibilizadas) {
        if (sensibilizadas.length == 0) {
            throw new IllegalArgumentException("No hay transiciones sensibilizadas");
        }
        // Prioridad: T5 (simple) > T2 (media) > T7 (alta)
        for (int t : sensibilizadas) {
            if (t == 5) return 5; // Modo simple prioritario
        }
        for (int t : sensibilizadas) {
            if (t == 2) return 2; // Modo media
        }
        for (int t : sensibilizadas) {
            if (t == 7) return 7; // Modo alta
        }
        return sensibilizadas[0]; // Fallback
    }
}

