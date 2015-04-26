package voxel.landscape.coord;

import com.jme3.math.Vector3f;

import static voxel.landscape.coord.Axis.X;

public class Direction {
    public static final int XNEG = 0, XPOS = 1, YNEG = 2, YPOS = 3, ZNEG = 4, ZPOS = 5;

    public static final int[] Directions = new int[] {XNEG, XPOS, YNEG, YPOS, ZNEG, ZPOS};

    public static final int[] PositiveDirections = new int[] { XPOS, YPOS, ZPOS};

    /* Index-able order array */
    public static Coord3[] DirectionCoords = new Coord3[]{
            new Coord3(-1, 0, 0),
            new Coord3(1, 0, 0),
            new Coord3(0, -1, 0),
            new Coord3(0, 1, 0),
            new Coord3(0, 0, -1),
            new Coord3(0, 0, 1),
    };
    public static Coord3[] DirectionCoordsAndZero = new Coord3[]{
            new Coord3(0, 0, 0),
            new Coord3(-1, 0, 0),
            new Coord3(1, 0, 0),
            new Coord3(0, -1, 0),
            new Coord3(0, 1, 0),
            new Coord3(0, 0, -1),
            new Coord3(0, 0, 1),
    };
    public static Vector3f[] DirectionVector3fs = new Vector3f[]{
            new Vector3f(-1, 0, 0),
            new Vector3f(1, 0, 0),
            new Vector3f(0, -1, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, 0, -1),
            new Vector3f(0, 0, 1),
    };
    public static Coord3[] DirectionXZCoords = new Coord3[]{
            new Coord3(-1, 0, 0),
            new Coord3(1, 0, 0),
            new Coord3(0, 0, -1),
            new Coord3(0, 0, 1),
    };
    public static Coord3[] DirectionXZNegCoords = new Coord3[]{
            new Coord3(-1, 0, 0),
            new Coord3(0, 0, -1),
    };
    public static Coord3[] DirectionXYCoords = new Coord3[]{
            new Coord3(-1, 0, 0),
            new Coord3(1, 0, 0),
            new Coord3(0, -1, 0),
            new Coord3(0, 1, 0),
    };
    public static Coord3[] DirectionYZCoords = new Coord3[]{
            new Coord3(0, -1, 0),
            new Coord3(0, 1, 0),
            new Coord3(0, 0, -1),
            new Coord3(0, 0, 1),
    };
    public static Vector3f[] DirectionXZVector3fs = new Vector3f[]{
            new Vector3f(-1, 0, 0),
            new Vector3f( 1, 0, 0),
            new Vector3f( 0, 0,-1),
            new Vector3f( 0, 0, 1),
    };
    public static Vector3f[] DirectionXZVector3fsAbsValues = new Vector3f[]{
            new Vector3f(1, 0, 0),
            new Vector3f(1, 0, 0),
            new Vector3f(0, 0, 1),
            new Vector3f(0, 0, 1),
    };
    public static Vector3f[] DirectionXZAndZeroVector3fs = new Vector3f[]{
            new Vector3f(0, 0, 0),
            new Vector3f(-1, 0, 0),
            new Vector3f(1, 0, 0),
            new Vector3f(0, 0, -1),
            new Vector3f(0, 0, 1),
    };
    public static Coord3[] DirectionCoordsXZAndDown = new Coord3[]{
            new Coord3(0, -1, 0),
            new Coord3(-1, 0, 0),
            new Coord3(1, 0, 0),
            new Coord3(0, 0, -1),
            new Coord3(0, 0, 1),
    };
    public static String[] Names = new String[] {
            "XNEG", "XPOS", "YNEG", "YPOS", "ZNEG", "ZPOS"
    };
    public static final Vector3f UNIT_XZ = new Vector3f(1f, 0f, 1f);

    public static int OppositeDirection(int dir) {
        if (dir % 2 == 0) return dir + 1;
        return dir - 1;
    }
    public static Coord3[] NudgeDirectionsNormalToAxis(int axis) {
        if (axis == X) return DirectionYZCoords;
        if (axis == Axis.Y) return DirectionXZCoords;
        return DirectionXYCoords;
    }

    public static Coord3 DirectionCoordForDirection(int dir) {
        return DirectionCoords[dir];
    }

    public static boolean IsNegDir(int dir) {
        return dir % 2 == 0;
    }

    public static Vector3f AddToComponentAtAxis(Vector3f vec, float whatToAdd, int axis) {
        if (axis == X)
            vec.x += whatToAdd;
        else if (axis == Axis.Y)
            vec.y += whatToAdd;
        else
            vec.z += whatToAdd;
        return vec;
    }


    public static int AxisForDirection(int dir) {
        if (dir <= Direction.XPOS) {
            return X;
        }
        if (dir <= Direction.YPOS) {
            return Axis.Y;
        }
        return Axis.Z;
    }

    public static int[] BitMasks = new int[]{
            1 << XNEG,
            1 << XPOS,
            1 << YNEG,
            1 << YPOS,
            1 << ZNEG,
            1 << ZPOS
    };
    public static int[] NegativeBitMasks = new int[] {
            0b111110,
            0b111101,
            0b111011,
            0b110111,
            0b101111,
            0b011111
    };

    public static Coord3[] UnitCoordsForNormalAxis(int normal) {
        switch (normal) {
            case Axis.X:
                return new Coord3[] { Coord3.ypos.clone(), Coord3.zpos.clone() };
            case Axis.Y:
                return new Coord3[] { Coord3.xpos.clone(), Coord3.zpos.clone() };
            default:
                return new Coord3[] { Coord3.xpos.clone(), Coord3.ypos.clone() };
        }
    }

    public static String ToString(int dir) {

        switch (dir) {
            case XPOS:
                return "XPOS";
            case XNEG:
                return "XNEG";
            case YNEG:
                return "YNEG";
            case YPOS:
                return "YPOS";
            case ZNEG:
                return "ZNEG";
            case ZPOS:
                return "ZPOS";
            default:
                return "INVALID DIRECTION";

        }
    }
}
