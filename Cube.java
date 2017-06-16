import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public final class Cube extends Entity {
    public double sideLength;

    private Ray3[] faces;

    public Cube(Vector3 position, double sideLength, Surface surface, String texture) {
        this.position = position;
        this.sideLength = sideLength;
        this.surface = surface;
        double hs = sideLength / 2;
        try {
            this.texture = ImageIO.read(new File(texture));
        } catch (IOException e) {
        }
        faces = new Ray3[]{
            new Ray3(
                position.plus(new Vector3(-hs, 0, 0)),
                new Vector3(-1, 0, 0)
            ),
            new Ray3(
                position.plus(new Vector3(hs, 0, 0)),
                new Vector3(1, 0, 0)
            ),
            new Ray3(
                position.plus(new Vector3(0, -hs, 0)),
                new Vector3(0, -1, 0)
            ),
            new Ray3(
                position.plus(new Vector3(0, hs, 0)),
                new Vector3(0, 1, 0)
            ),
            new Ray3(
                position.plus(new Vector3(0, 0, -hs)),
                new Vector3(0, 0, -1)
            ),
            new Ray3(
                position.plus(new Vector3(0, 0, hs)),
                new Vector3(0, 0, 1)
            )
        };
    }

    @Override
    public Ray3 collide(Ray3 ray) {
        Ray3 closestNormal = null;
        double distanceSquared = 0;
        for (Ray3 face : faces) {
            Vector3 faceNormal = face.direction;
            double distance = ray.position.minus(face.position).dot(faceNormal);
            if (distance < 0) {
                faceNormal = faceNormal.scale(-1);
                distance = -distance;
            }
            Ray3 normal = new Ray3(
                ray.position.minus(
                    ray.direction.scale(distance / ray.direction.dot(faceNormal))
                ),
                faceNormal
            );
            if (normal.position.minus(ray.position).dot(ray.direction) < 0.00001) {
                continue;
            }
            Vector3 fp = normal.position.minus(face.position);
            double hs = sideLength / 2;
            if (Math.abs(fp.x()) > hs || Math.abs(fp.y()) > hs || Math.abs(fp.z()) > hs) {
                continue;
            }
            if (closestNormal == null ||
                    normal.position.minus(ray.position).lengthSquared() < distanceSquared) {
                closestNormal = normal;
                distanceSquared = normal.position.minus(ray.position).lengthSquared();
            }
        }
        return closestNormal;
    }
}
