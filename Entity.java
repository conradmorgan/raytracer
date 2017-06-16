import java.awt.image.BufferedImage;

public abstract class Entity {
    public Surface surface;
    public Vector3 position;
    public BufferedImage texture;
    public abstract Ray3 collide(Ray3 ray);
}
