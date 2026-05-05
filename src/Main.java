import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int CANTIDAD_INVARIANTES = 200;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Sistema de Procesamiento de Datos ===");
        System.out.println("Invariantes a completar: " + CANTIDAD_INVARIANTES);

        RdP rdp = new RdP();
        PoliticaInterface politica = new PoliticaAleatoria();
        String nombrePolitica = politica.getClass().getSimpleName();
        System.out.println("Política: " + nombrePolitica);

        Monitor monitor = new Monitor(rdp, politica);
        String archivoLog = "log_" + nombrePolitica + ".txt";
        Logger logger = new Logger(archivoLog, nombrePolitica);
        logger.start();

        AtomicInteger invariantesGlobales = new AtomicInteger(0);

        // Hilo 0 - Generador (pre-conflicto): T0 -> T1
        Transicion hiloGenerador = new Transicion(
                monitor, new int[]{0, 1}, logger,
                invariantesGlobales, CANTIDAD_INVARIANTES, "Hilo-Generador"
        );

        // Hilo 1 - modo simple: T5 -> T6 -> T11
        Transicion hiloSimple = new Transicion(
                monitor, new int[]{5, 6, 11}, logger,
                invariantesGlobales, CANTIDAD_INVARIANTES, "Hilo-Simple"
        );

        // Hilo 2 - modo media: T2 -> T3 -> T4 -> T11
        Transicion hiloMedia = new Transicion(
                monitor, new int[]{2, 3, 4, 11}, logger,
                invariantesGlobales, CANTIDAD_INVARIANTES, "Hilo-Media"
        );

        // Hilo 3 - modo alta: T7 -> T8 -> T9 -> T10 -> T11
        Transicion hiloAlta = new Transicion(
                monitor, new int[]{7, 8, 9, 10, 11}, logger,
                invariantesGlobales, CANTIDAD_INVARIANTES, "Hilo-Alta"
        );

        System.out.println("Iniciando hilos...");
        long tiempoInicio = System.currentTimeMillis();

        hiloGenerador.start();
        hiloSimple.start();
        hiloMedia.start();
        hiloAlta.start();

        // Esperamos a que el sistema alcance la meta
        while (invariantesGlobales.get() < CANTIDAD_INVARIANTES) {
            Thread.sleep(50);
        }

        System.out.println("\nMeta alcanzada. Enviando señal de apagado...");

        hiloGenerador.interrupt();
        hiloSimple.interrupt();
        hiloMedia.interrupt();
        hiloAlta.interrupt();

        hiloGenerador.join();
        hiloSimple.join();
        hiloMedia.join();
        hiloAlta.join();

        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        System.out.println("Todos los hilos terminaron.");
        System.out.println("Tiempo de ejecución: " + tiempoTotal + " ms");

        logger.finalizar();
        logger.join();
        System.out.println("Log guardado en: " + archivoLog);

        System.out.println("\n=== Análisis de Invariantes ===");
        AnalizadorInvariantes analizador = new AnalizadorInvariantes(archivoLog);
        analizador.analizar();

        if (analizador.cumpleInvariante()) {
            System.out.println("  Invariantes de transición verificados correctamente.");
        } else {
            System.out.println("  Error en la verificación de invariantes.");
        }
        System.out.println("\n=== Fin del Sistema ===");
    }
}