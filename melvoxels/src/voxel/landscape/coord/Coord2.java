package voxel.landscape.coord;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;


public class Coord2 implements ICoordXZ
{
    private static final long serialVersionUID = 222L;

    public int x;
	public int y;
	
	public static final Coord2 one = new Coord2(1,1);
	public static final Coord2 zero = new Coord2(0,0);

    public static final Coord2 SPECIAL_FLAG = new Coord2(Integer.MIN_VALUE + 1);
	
	public Coord2(int _x, int _y)
	{
		x = _x;
		y = _y;
	}
	public Coord2(int a) { this(a,a); }
	
	public Coord2(double a, double b) { this((int) a, (int) b); }

    public Coord2() {}

    public Coord2 multy(Coord2 other) {
		return new Coord2(this.x * other.x, this.y * other.y);
	}
	public Coord2 multy(int scaleBy){
		return new Coord2(this.x * scaleBy, this.y * scaleBy);
	}
	public Coord2 multy(double scaleBy){
		return new Coord2(this.x * scaleBy, this.y * scaleBy);
	}
	public Coord2 divideBy(Coord2 other) {
		return new Coord2(this.x / other.x, this.y / other.y);
	}
    public Coord2 divideBy(int value) {
        return new Coord2(this.x / value, this.y / value);
    }
	public Coord2 add(Coord2 other) {
		return new  Coord2(this.x + other.x, this.y + other.y);
	}
    public Coord2 minus(int x, int y) {
        return new  Coord2(this.x - x, this.y - y);
    }
	public Coord2 minus(Coord2 other) {
		return new  Coord2(this.x - other.x, this.y - other.y);
	}
	public static Coord2 Max(Coord2 a, Coord2 b) {
		return new Coord2(a.x > b.x ? a.x : b.x, a.y > b.y ? a.y : b.y);
	}
	public static Coord2 Min(Coord2 a, Coord2 b) {
		return new Coord2(a.x < b.x ? a.x : b.x, a.y < b.y ? a.y : b.y);
	}
	public boolean equals(Object other) {
		if (other.getClass() != Coord2.class) return false;
		return equals((Coord2) other);
	}
	public boolean equals(Coord2 other) { return x == other.x && y == other.y; }
    public boolean greaterThan(Coord2 other) { return x > other.x && y > other.y; }
	public boolean greaterThanOrEqual(Coord2 co) {
		return x >= co.x && y >= co.y;
	}
    public boolean lessThan(Coord2 other) { return x < other.x && y < other.y; }

    @Override
	public Coord2 clone() { return new Coord2(x,y); }
	public Vector3f toVec3XZ() {
		return new Vector3f(x,0,y);
	}
	public Vector2f toVector2f() { return new Vector2f(x,y); }

    public int distanceSquared(Coord2 other) { return other.minus(this).distanceSquared(); }
    public int distanceSquared() { return x*x + y*y; }
    public static Coord2 FromXZOfVector3f(Vector3f v) { return new Coord2(v.x,v.z); }
    public static Coord2 FromCoord3XZ(Coord3 c) { return new Coord2(c.x,c.z); }
	public boolean positiveComponents() { return x >= 0 && y >= 0; }
	public String toString() { return String.format("Coord2:x: %d y: %d", x,y); }

    @Override
    public int hashCode() {
        return (y & Integer.MIN_VALUE) | ((x & Integer.MIN_VALUE) >>> 1 ) |
                ((y & 32765 ) << 14) | (x & 32765);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return y;
    }


}
