/**
 * Interface MonitorInterface
 *
 * Contrato que debe cumplir el Monitor.
 * Es el único método público que expone la clase Monitor,
 * tal como exige el enunciado.
 */
public interface MonitorInterface {
    boolean fireTransition(int transition) throws InterruptedException;
}