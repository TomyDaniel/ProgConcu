public interface PoliticaInterface {
    /**
     * Dado un array de transiciones sensibilizadas, decide cuál debe disparar.
     * Solo se llama para transiciones conflictivas. Devuelve -1 si ninguna disponible.
     */
    int elegir(boolean[] m, boolean[] Vs);

    /**
     * Indica si una transición pertenece al conjunto de conflictos
     * que esta política administra. Las transiciones no conflictivas
     * siempre se aprueban sin consultar la política.
     */
    boolean esTransicionConflictiva(int transition);
}