package voxel.landscape.collection;

import voxel.landscape.BlockType;
import voxel.landscape.chunkbuild.blockfacefind.ChunkLocalCoord;
import voxel.landscape.coord.Coord3;
import voxel.landscape.fileutil.FileUtil;
import voxel.landscape.player.B;
import voxel.landscape.util.Asserter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 4/3/15.
 */
public class LocalBlockMap implements Serializable {

    public HashMap<ChunkLocalCoord, Integer> map;  // public for testing: make private
    public final AtomicBoolean writeDirty = new AtomicBoolean(false);

    public LocalBlockMap(Coord3 _size) {
        map = new HashMap<ChunkLocalCoord, Integer >(_size.x * _size.z * 2);
        Asserter.assertTrue(isEmpty(), "??? wha");
    }

    public boolean isEmpty() {
        return map.size() == 0;
    }

    public int Get(Coord3 pos) {
        Integer blockType = map.get(new ChunkLocalCoord(pos));
        if (blockType != null) return blockType.intValue();
        return BlockType.NON_EXISTENT.ordinal();
    }

    public int Get(int x, int y, int z) {
        return Get(new Coord3(x,y,z));
    }

    public int SafeGet(Coord3 pos) {
        return Get(pos); // SafeGet(pos.x, pos.y, pos.z);
    }
    public int SafeGet(int x, int y, int z) {
        return Get(x,y,z);
    }

    public void Set(int obj, int x, int y, int z) {
        Set(obj, new Coord3(x,y,z));
    }
    public void Set(int obj, Coord3 pos) {
        Integer previous = map.put(new ChunkLocalCoord(pos), obj);
        if (previous != null && previous.intValue() != obj) {
            writeDirty.set(true);
        }
    }

    /*
     * Read / Write
     */
    public void readFromFile(Coord3 position) {
        Object mapO = FileUtil.DeserializeChunkObject(position, FileUtil.LocalBlockMapExtension);
        if (mapO != null) {
            map = (HashMap<ChunkLocalCoord, Integer>) mapO;
            writeDirty.set(false);
        }
    }

    public void writeToFile(Coord3 position) {
        if (!writeDirty.get()) return;
        if (isEmpty()) {
            writeDirty.set(false);
            return;
        }
        try {
            FileUtil.SerializeChunkObject(map, position, FileUtil.LocalBlockMapExtension);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        writeDirty.set(false);
    }

}
