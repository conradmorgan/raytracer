public final class Vector3 {
    private double x, y, z;

    public Vector3() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 plus(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 minus(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 scale(double s) {
        return new Vector3(s * x, s * y, s * z);
    }

    public double dot(Vector3 v) {
        return x*v.x + y*v.y + z*v.z;
    }

    public Vector3 cross(Vector3 v) {
        return new Vector3(
            y*v.z - z*v.y,
            z*v.x - x*v.z,
            x*v.y - y*v.x
        );
    }

    public double lengthSquared() {
        return x*x + y*y + z*z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector3 normalize() {
        if (isZero()) {
            return this;
        }
        return scale(1 / length());
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
