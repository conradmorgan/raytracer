import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public final class Sphere extends Entity {
    public double radius;

    public Sphere(Vector3 position, double radius, Surface surface, String texture) {
        this.position = position;
        this.radius = radius;
        this.surface = surface;
        try {
            this.texture = ImageIO.read(new File(texture));
        } catch (IOException e) {
        }
    }

    @Override
    public Ray3 collide(Ray3 ray) {
        Vector3 closestPoint = ray.direction.scale(
            position.minus(ray.position).dot(ray.direction)
        ).plus(ray.position);
        Vector3 perpendicular = closestPoint.minus(position);
        if (perpendicular.lengthSquared() >= radius * radius) {
            return null;
        }
        Vector3 opposite = ray.direction.scale(
            Math.sqrt(radius*radius - perpendicular.lengthSquared())
        );
        Vector3 intersection1 = position.plus(perpendicular).minus(opposite);
        Vector3 intersection2 = position.plus(perpendicular).plus(opposite);
        double distance1 = intersection1.minus(ray.position).dot(ray.direction);
        double distance2 = intersection2.minus(ray.position).dot(ray.direction);
        double delta = 0.000001;
        if (distance1 <= delta && distance2 <= delta) {
            return null;
        }
        Vector3 intersection;
        if (distance1 > 0 && distance2 <= delta) {
            intersection = intersection1;
        } else if (distance2 > 0 && distance1 <= delta) {
            intersection = intersection2;
        } else if (distance1 < distance2) {
            intersection = intersection1;
        } else {
            intersection = intersection2;
        }
        Ray3 normal = new Ray3(intersection, intersection.minus(position));
        if (ray.position.minus(position).lengthSquared() < radius * radius) {
            normal.direction = normal.direction.scale(-1);
        }
        normal.direction = normal.direction.normalize();
        return normal;
    }
}
