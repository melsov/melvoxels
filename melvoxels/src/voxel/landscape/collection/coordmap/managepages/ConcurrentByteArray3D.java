package voxel.landscape.collection.coordmap.managepages;

import voxel.landscape.coord.Coord3;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Created by didyouloseyourdog on 7/30/14.
 */
public class ConcurrentByteArray3D
{
    private Coord3 size;
//    private byte list[][][];

    private AtomicIntegerArray list;
//    private ConcurrentMap<Coord3, byte> list = new ConcurrentHashMap<Coord3,byte>();
    public ConcurrentByteArray3D(Coord3 _size) {
        this.size = _size;
//        list = new byte[size.x][size.y][size.z];
        int sz = size.y * size.z * size.x;
        list = new AtomicIntegerArray(sz);
    }
    public Coord3 getSize() {
        return size;
    }

    public byte Get(Coord3 pos) {
        return Get(pos.x, pos.y, pos.z);
    }
    public byte Get(int x, int y, int z) {
        return (byte)list.get(y * (size.x*size.z) + z * (size.x) + x );
//        return list[z][y][x];
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
        list.set(y * (size.x*size.z) + z * (size.x) + x , obj);
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
