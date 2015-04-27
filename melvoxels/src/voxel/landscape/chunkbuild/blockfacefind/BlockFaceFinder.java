package voxel.landscape.chunkbuild.blockfacefind;

import com.jme3.renderer.Camera;

import voxel.landscape.chunkbuild.blockfacefind.floodfill.FloodFill4D;
import voxel.landscape.chunkbuild.bounds.XZBounds;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 10/2/14.
 */
public class BlockFaceFinder {

    public static final int THREAD_COUNT = 8;
	public final List<FloodFill4D> floodFills;
    public final BlockingQueue<Coord3> floodFilledChunkCoords = new ArrayBlockingQueue<Coord3>(456);
    private String threadName;
    private AtomicBoolean shouldStop = new AtomicBoolean(false);
    private ExecutorService threadPool;

    public BlockFaceFinder(TerrainMap _map, BlockingQueue<Coord3> _chunkCoordsToBeFlooded, Camera _cam, XZBounds _xzBounds, String _threadName) {
    	threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
    	floodFills = new ArrayList<>(THREAD_COUNT);
    	for(int i = 0; i < THREAD_COUNT; i++) {
	        FloodFill4D floodFill = new FloodFill4D(_map, _cam, _chunkCoordsToBeFlooded, floodFilledChunkCoords, shouldStop, _xzBounds);
	        floodFills.add(floodFill);
    	}
        threadName = _threadName;
    }
    public void start() {
    	for (FloodFill4D floodFill : floodFills) {
    		threadPool.execute(floodFill);
    	}
    }

    public void shutdown() {
        shouldStop.set(true);
        threadPool.shutdownNow();
    }

}
