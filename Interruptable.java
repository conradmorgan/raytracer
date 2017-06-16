// For objects that have calculations that can be interrupted,
// such as a progressive render of the RayTracer class.
public interface Interruptable {
    public void interrupt();
}
