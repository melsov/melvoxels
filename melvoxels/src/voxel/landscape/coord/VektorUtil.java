package voxel.landscape.coord;

import com.jme3.math.Vector3f;

import java.util.Random;

import static java.lang.Math.*;

public class VektorUtil {

    private static Random random = new Random();
	public static Vector3f Frac(Vector3f v) {
    	return v.subtract(Coord3.FromVector3f(v).toVector3());
    }

	public static Vector3f Floor(Vector3f v) {
		return Coord3.FromVector3f(v).toVector3();
	}

    public static Vector3f OneIfNeg(Vector3f v) {
    	return new Vector3f(v.x < 0 ? 1 : 0, v.y < 0 ? 0 : 1, v.z < 0 ? 1 : 0);
    }

    public static Vector3f OneIfPos(Vector3f v) {
    	return new Vector3f(v.x > 0 ? 1 : 0, v.y > 0 ? 1 : 0, v.z > 0 ? 1 : 0);
    }

    public static boolean AGreaterThanB(Vector3f a, Vector3f b) { return a.x > b.x && a.y > b.y && a.z > b.z;  }

    public static Vector3f Sign(Vector3f v) {
    	return new Vector3f(signum(v.x), signum(v.y), signum(v.z));
    }

    public static Vector3f Abs(Vector3f v) {
    	return new Vector3f(abs(v.x),abs(v.y),abs(v.z));
    }

    public static Vector3f Round(Vector3f v) {
    	return new Vector3f(round(v.x), round(v.y), round(v.z));
    }

    public static Vector3f SwapXZ(Vector3f v) {
        return new Vector3f(v.z, v.y, v.x);
    }
    public static Vector3f SwapXY(Vector3f v) {
        return new Vector3f(v.y, v.x, v.z);
    }
    public static Vector3f SwapYZ(Vector3f v) {
        return new Vector3f(v.x, v.z, v.y);
    }
    public static Vector3f MaskClosestToWholeNumber(Vector3f v) {
    	Vector3f test = VektorUtil.Abs(VektorUtil.Round(v).subtract(v) );
    	Vector3f result = Vector3f.UNIT_X;
    	if (test.y < test.x && test.y < test.z) {
    		result = Vector3f.UNIT_Y;
    	} else if (test.z < test.x) {
    		result = Vector3f.UNIT_Z;
    	}
    	return result.clone();
    }
    public static Vector3f AbsLargestMask(final Vector3f v) {
        Vector3f test = VektorUtil.Abs(v);
        Vector3f result = Vector3f.UNIT_X;
        if (test.y > test.x && test.y > test.z) {
            result = Vector3f.UNIT_Y;
        } else if (test.z > test.x) {
            result = Vector3f.UNIT_Z;
        }
        return result.clone();
    }
    public static boolean HasAComponentThatIsVeryCloseToZero(final Vector3f v) {
        float epsilon = .01f;
        return abs(v.x) < epsilon | abs(v.y) < epsilon | abs(v.z) < epsilon;
    }
    public static Vector3f MakeAllComponentsNonZero(Vector3f vv) {
        float epsilon = .01f;
        Vector3f v = vv.clone();
        if (abs(v.x) < epsilon) v.x = epsilon * random.nextFloat() - .5f;
        if (abs(v.y) < epsilon) v.y = epsilon * random.nextFloat() - .5f;
        if (abs(v.z) < epsilon) v.z = epsilon * random.nextFloat() - .5f;
        return v;
    }
    public static Vector3f SubtractOneFromNegativeComponents(Vector3f v) {
        return new Vector3f(v.x < 0 ? v.x - 1f : v.x, v.y < 0 ? v.y - 1f : v.y, v.z < 0 ? v.z - 1f : v.z);
    }


    public static Vector3f EscapePositionOnUnitGrid(Vector3f position, Vector3f direction) {
        return EscapePositionOnUnitGrid(position, direction, null);
    }

    public static Vector3f EscapePositionOnUnitGrid(Vector3f position, Vector3f direction, MutableInteger escapeThroughFaceDirection) {
        Vector3f corner_dist = DistanceToCorner(position, direction);
        Vector3f relativeEscape = RelativeEscapeVector(corner_dist, direction, 1.05f, escapeThroughFaceDirection);
        return position.add(relativeEscape);
    }
    public static Vector3f RelativeEscapeVector(Vector3f cornerDistance, Vector3f dir) {
        return RelativeEscapeVector(cornerDistance, dir, 1f, null);
    }
    public static Vector3f RelativeEscapeVector(Vector3f cornerDistance, Vector3f dir, float fudgeFactor, final MutableInteger escapeThroughFaceDirection) {
        Vector3f lengths = VektorUtil.Abs(cornerDistance.divide(dir));
        float length;
        if (lengths.x > lengths.y) {
            length = lengths.y;
            if (escapeThroughFaceDirection!= null) escapeThroughFaceDirection.integer = dir.y < 0 ? Direction.YNEG : Direction.YPOS;
        } else {
            length =lengths.x;
            if (escapeThroughFaceDirection!= null) escapeThroughFaceDirection.integer = dir.x < 0 ? Direction.XNEG : Direction.XPOS;
        }
        if (lengths.z < length) {
            length = lengths.z;
            if (escapeThroughFaceDirection!= null) escapeThroughFaceDirection.integer = dir.z < 0 ? Direction.ZNEG : Direction.ZPOS;
        }
        return dir.mult(length * fudgeFactor);
    }

    public static Vector3f RelativeEscapeVectorXZ(Vector3f cornerDistance, Vector3f dir, float fudgeFactor) {
        if (VektorUtil.HasAComponentThatIsVeryCloseToZero(cornerDistance)) return dir.mult(fudgeFactor);
        Vector3f lengths = VektorUtil.Abs(cornerDistance.divide(dir));
        float length = lengths.x < lengths.z ? lengths.x : lengths.z;
        return dir.mult(length * fudgeFactor);
    }

    public static Vector3f DistanceToCorner(Vector3f pos, Vector3f dir) {
        return CornerVector(pos, dir).subtract(pos);
    }

    public static Vector3f CornerVector(Vector3f pos, Vector3f dir) {
        Vector3f corner = SubtractOneFromNegativeComponents(OneIfPos(dir).add(pos));
        return Floor(corner);
    }
}
