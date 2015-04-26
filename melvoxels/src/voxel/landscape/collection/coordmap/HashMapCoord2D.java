package voxel.landscape.collection.coordmap;

import voxel.landscape.coord.Coord2;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by didyouloseyourdog on 7/28/14.
 */
public class HashMapCoord2D<T> {
    private Class<T> type;

    private HashMap<Coord2,T> table;

    @SuppressWarnings("unchecked")
    public HashMapCoord2D(Class<T> _type) {
        table = new HashMap<Coord2, T>(64*64);
        type = _type;
    }
    public int size() {
        return table.size();
    }
    public Set<Coord2> keySet() {
        return table.keySet();
    }
    public void Set(T obj, Coord2 pos) {
        table.put(pos.clone(), obj);
    }
    public void Set(T obj, int x, int y) {
        Set(obj, new Coord2(x, y));
    }

    public T GetInstance(Coord2 pos) {
        T obj = table.get(pos);
        if (obj == null) {
            try {
                obj = type.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Set(obj, pos);
        }
        return obj;
    }
    public T GetInstance(int x, int y) {
        return GetInstance(new Coord2(x,y));
    }

    public T Get(Coord2 pos) {
        return table.get(pos);
    }
    public T Get(int x, int y) {
        return Get(new Coord2(x,y));
    }

    public T SafeGet(Coord2 pos) {
        return Get(pos);
    }
    public T SafeGet(int x, int y) {
        return Get(x,y);
    }

    public void AddOrReplace(T obj, Coord2 pos) {
        Set(obj, pos);
    }
    public void AddOrReplace(T obj, int x, int y) {
        AddOrReplace(obj, new Coord2(x, y));
    }

    public void Remove(int x, int z) { Remove(new Coord2(x,z)); }
    public void Remove(Coord2 key) {
        table.remove(key);
    }

}
