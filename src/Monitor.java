public class Monitor implements MonitorInterface{
    private final RdP redPetri;
    private final Politica politica;

    public Monitor(RdP rdp, Politica politica) {
        this.redPetri = rdp;
        this.politica = politica;
    }


    @Override
    public boolean fireTransition(int transition) {
        // Lógica para disparar una transición en el RdP
        // Retorna true si la transición fue exitosa, false en caso contrario
        return true;
    }
}
