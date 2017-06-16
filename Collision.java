// Describes a collision in the world of a ray against an entity.
// The normal is a vector perpendicular to the surface at the point
// of collision, and should be normalized.
public final class Collision {
    public Entity entity;
    public Ray3 normal;

    public Collision(Entity entity, Ray3 normal) {
        this.entity = entity;
        this.normal = normal;
    }
}
