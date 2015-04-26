package voxel.landscape.collection.coordmap;

import voxel.landscape.coord.Coord3;
import voxel.landscape.util.Asserter;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 7/28/14.
 */
public class HashMapCoord3D<T>
{
    private Class<T> type;
    private HashMap<Coord3,T> table;

    @SuppressWarnings("unchecked")
    public HashMapCoord3D(Class<T> _type) {
        table = new HashMap<Coord3, T>(64*64);
        type = _type;
    }
    public void Set(T obj, Coord3 pos) { table.put(pos.clone(), obj); }

    public void Set(T obj, int x, int y, int z) {
        Set(obj, new Coord3(x, y, z));
    }

    public T GetInstance(Coord3 pos) {
        T obj = Get(pos);
        if (obj == null) {
            try {
                obj = type.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                Asserter.assertFalseAndDie("Get Instance failed");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Asserter.assertFalseAndDie("Get Instance failed");
            }
            Set(obj, pos);
        }
        return obj;
    }
    public T GetInstance(int x, int y, int z) {
        return GetInstance(new Coord3(x,y, z));
    }

    public T Get(Coord3 pos) { return table.get(pos); }

    public T Get(int x, int y, int z) {
        return Get(new Coord3(x, y, z));
    }

    public T SafeGet(Coord3 pos) {
        return Get(pos);
    }
    public T SafeGet(int x, int y, int z) {
        return Get(x,y, z);
    }

    public void AddOrReplace(T obj, Coord3 pos) {
        Set(obj, pos);
    }
    public void AddOrReplace(T obj, int x, int y, int z) {
        AddOrReplace(obj, new Coord3(x, y, z));
    }

    public void Remove(Coord3 pos) { table.remove(pos); }

}
