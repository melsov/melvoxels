package voxel.landscape.collection.unused;

import voxel.landscape.coord.Coord3;
import voxel.landscape.util.Asserter;

/**
 * Created by didyouloseyourdog on 7/31/14.
 */
public class ByteArray3DNoBitShifting {
    private Coord3 size;
    private int SIZE_BITS_X,SIZE_BITS_Y,SIZE_BITS_Z;
    // Avoid multi-dimensional arrays in java if at all possible: http://stackoverflow.com/questions/258120/what-is-the-memory-consumption-of-an-object-in-java
    private byte list[][][];

    public ByteArray3DNoBitShifting(Coord3 _size) {
        this.size = _size;
        getBitSizes(_size);
        list = new byte[size.x][size.y][size.z];
    }
    private void getBitSizes(Coord3 _size) {
        SIZE_BITS_X = LogBase2(_size.x);
        SIZE_BITS_Y = LogBase2(_size.y);
        SIZE_BITS_Z = LogBase2(_size.z);
    }
    private static int LogBase2(int n) {
        Asserter.assertTrue(n > 0, "Only positive allowed for finding log 2 ");
        int pow = 0;
        for(int i = 0; i <= 31 ; i++) {
            pow = 1 << i;
            if (pow == n) return i;
            Asserter.assertTrue(pow < n, "Sorry your size wasn't a power of 2");
        }
        Asserter.assertFalseAndDie("The log you wanted was beyond int capacity? ");
        return Integer.MIN_VALUE;
    }
    public Coord3 getSize() {
        return size;
    }

    public byte Get(Coord3 pos) {
        return Get(pos.x, pos.y, pos.z);
    }
    public byte Get(int x, int y, int z) {
        return list[x][y][z];
    }

    public byte SafeGet(Coord3 pos) {
        return SafeGet(pos.x, pos.y, pos.z);
    }
    public byte SafeGet(int x, int y, int z) {
        if(!IndexWithinBounds(x, y, z))
            try {
                throw new Exception ("byte array out of bounds: x " + x + " y: " + y + " z: " + z);
            } catch (Exception e) {
                e.printStackTrace();
            }
        return Get(x, y, z);
    }

    public void Set(byte obj, Coord3 pos) {
        Set(obj, pos.x, pos.y, pos.z);
    }
    public void Set(byte obj, int x, int y, int z) {
        list[x][y][z] = obj;
    }

    public boolean IndexWithinBounds(Coord3 pos) {
        return IndexWithinBounds(pos.x, pos.y, pos.z);
    }
    public boolean IndexWithinBounds(int x, int y, int z) {
        if(x>=size.x || y>=size.y || z>=size.z) return false;
        return true;
    }

    public Coord3 GetSize() {
        return size;
    }

    public int GetSizeX() {
        return size.x;
    }
    public int GetSizeY() {
        return size.y;
    }
    public int GetSizeZ() {
        return size.z;
    }

}
