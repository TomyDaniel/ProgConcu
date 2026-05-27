import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int CANTIDAD_INVARIANTES = 200;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Sistema de Procesamiento de Datos ===");
        System.out.println("Invariantes a completar: " + CANTIDAD_INVARIANTES);

        // Configuracion de Hilos
        int cantHilosGenerador = 1;
        int cantHilosSimple    = 1;
        int cantHilosMedia     = 1;
        int cantHilosAlta      = 1;
        int cantHilosSalida    = 1;

        // Inicializacion de clases que no aportan concurrencia
        RdP rdp = new RdP();
        PoliticaInterface politica = new PoliticaAleatoria();
        String nombrePolitica = politica.getClass().getSimpleName();
        System.out.println("Política: " + nombrePolitica);

        // Inicializacion del logger
        Logger logger = new Logger(nombrePolitica);
        logger.start();

        // Inicializacion de clases fundamentales en la concurrencia
        Mutex mutex = new Mutex();
        ColaCondicion colas = new ColaCondicion(rdp.getCantidadTransiciones());
        Monitor monitor = new Monitor(rdp, mutex, colas, politica, logger);

        AtomicInteger invariantesGlobales = new AtomicInteger(0);
        AtomicBoolean apagado = new AtomicBoolean(false);
        List<Transicion> todosLosHilos = new ArrayList<>();

        // Creacion de Hilos
        // Hilo generador: T0 -> T1
        for (int i = 0; i < cantHilosGenerador; i++) {
            todosLosHilos.add(new Transicion(monitor, new int[]{0, 1}, logger, invariantesGlobales, CANTIDAD_INVARIANTES, "Generador-" + i, apagado));
        }

        // Modo simple: T5 -> T6
        for (int i = 0; i < cantHilosSimple; i++) {
            todosLosHilos.add(new Transicion(monitor, new int[]{5, 6}, logger, invariantesGlobales, CANTIDAD_INVARIANTES, "Simple-" + i, apagado));
        }

        // Modo media: T2 -> T3 -> T4
        for (int i = 0; i < cantHilosMedia; i++) {
            todosLosHilos.add(new Transicion(monitor, new int[]{2, 3, 4}, logger, invariantesGlobales, CANTIDAD_INVARIANTES, "Media-" + i, apagado));
        }

        // Modo alta: T7 -> T8 -> T9 -> T10
        for (int i = 0; i < cantHilosAlta; i++) {
            todosLosHilos.add(new Transicion(monitor, new int[]{7, 8, 9, 10}, logger, invariantesGlobales, CANTIDAD_INVARIANTES, "Alta-" + i, apagado));
        }

        // Salida / Join: T11
        for (int i = 0; i < cantHilosSalida; i++) {
            todosLosHilos.add(new Transicion(monitor, new int[]{11}, logger, invariantesGlobales, CANTIDAD_INVARIANTES, "Salida-" + i, apagado));
        }

        // Ejecucion de las cosas
        System.out.println("Iniciando " + todosLosHilos.size() + " hilos en total...");
        long tiempoInicio = System.currentTimeMillis();

        for (Transicion hilo : todosLosHilos) {
            hilo.start();
        }

        // Esperamos hasta terminar
        while (invariantesGlobales.get() < CANTIDAD_INVARIANTES) {
            Thread.sleep(50);
        }

        System.out.println("\nMeta alcanzada. Enviando señal de apagado a todos los hilos...");

        // Apagamos: primero flag, luego interrupt para despertar bloqueados
        apagado.set(true);
        for (Transicion hilo : todosLosHilos) {
            hilo.interrupt();
        }

        for (Transicion hilo : todosLosHilos) {
            hilo.join();
        }

        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        System.out.println("Todos los hilos terminaron su ejecución.");
        System.out.println("Tiempo de ejecución: " + tiempoTotal + " ms");

        // Apagado de loggers
        logger.finalizar();
        logger.join();

        System.out.println("Log principal guardado en: log.txt");
        System.out.println("Log de transiciones guardado en: log_transiciones.txt");

        System.out.println("\n=== Análisis de Invariantes ===");
        AnalizadorInvariantes analizador = new AnalizadorInvariantes("log_transiciones.txt", CANTIDAD_INVARIANTES);
        analizador.analizar();

        System.out.println("\n=== Fin del Sistema ===");
    }
}