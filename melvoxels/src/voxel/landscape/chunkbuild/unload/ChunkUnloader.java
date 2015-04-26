package voxel.landscape.chunkbuild.unload;

import voxel.landscape.collection.ColumnMap;
import voxel.landscape.coord.Coord3;
import voxel.landscape.debug.DebugGeometry;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.player.B;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by didyouloseyourdog on 4/4/15.
 */
public class ChunkUnloader implements Runnable {

    private final ColumnMap columnMap;
    private final TerrainMap map;
    private final BlockingQueue<Coord3> unloadChunks;
    private final BlockingQueue<Coord3> deletableChunks;

    private static int instanceCount = 0;

    private static final HashMap<Coord3, Integer> debugWriteCount = new HashMap<>(500);

    public ChunkUnloader(TerrainMap map, ColumnMap columnMap, BlockingQueue<Coord3> unloadColumns, BlockingQueue<Coord3> deletableColumns) {
        this.map = map;
        this.unloadChunks = unloadColumns;
        this.deletableChunks = deletableColumns;
        this.columnMap = columnMap;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Unload-Thread-"+instanceCount++);
        while(true) {
            Coord3 chunkCoord = null;
            try {
                chunkCoord = unloadChunks.take(); //thread will block while nothing is available...maybe forever...
                if (chunkCoord.equal(Coord3.SPECIAL_FLAG)) {
                    B.bugln("**time to quit: " + Thread.currentThread().getName());
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            map.writeChunkAndColumn(chunkCoord); //WANT
            try {
                if (!deletableChunks.contains(chunkCoord)) {
                    deletableChunks.put(chunkCoord.clone()); // put = blocking version of add
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void debugCoordWriteTimes(Coord3 coord3) {
        Integer i = debugWriteCount.get(coord3);
        i = i == null || i == 0 ? new Integer(1) : new Integer(i + 1);
        debugWriteCount.put(coord3, i);
        B.bugln(coord3.toString() + " : " + i);

    }
}
