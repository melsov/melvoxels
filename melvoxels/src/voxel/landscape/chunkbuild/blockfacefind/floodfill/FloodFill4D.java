package voxel.landscape.chunkbuild.blockfacefind.floodfill;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.WorldGenerator;
import voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkslice.ChunkSlice;
import voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkslice.ChunkSliceBag;
import voxel.landscape.chunkbuild.bounds.XZBounds;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.coord.Box;
import voxel.landscape.coord.BoxIterator;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.player.B;
import voxel.landscape.util.Asserter;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 10/9/14.
 * The fourth 'D' is time
 * Class to manage flood filling as
 * the camera moves around
 */
public class FloodFill4D implements Runnable
{
    private Camera camera;
    public BlockingQueue<Coord3> floodFilledChunkCoords;
    public final BlockingQueue<Coord3> chunkCoordsToBeFlooded;

    // TODO: plan a separate (singleton?) class that manages a 'building bounds'. (area where we want to build at any given time)
    // TODO: plan exactly what such a class should need to do: maintain current bounds, what about coords that recently went from in to out of bounds?
    // TODO: maybe first list classes that would be clients

    private ChunkSliceBag inBoundsBag;
    private ChunkSliceBag outOfBoundsBag;
    private AtomicBoolean shouldStop;
    private TerrainMap map;
    public static final int UntouchedType = BlockType.NON_EXISTENT.ordinal();
    private FloodFill floodFill;
    private static int instanceCount = 0;

    public FloodFill4D(TerrainMap _map, Camera _camera, BlockingQueue<Coord3> _chunkCoordsToBeFlooded, BlockingQueue<Coord3> _floodFilledChunkCoords, AtomicBoolean _shouldStop, XZBounds _xzBounds) {
        map = _map;
        camera = _camera;
        chunkCoordsToBeFlooded = _chunkCoordsToBeFlooded;
        floodFilledChunkCoords = _floodFilledChunkCoords;
        shouldStop = _shouldStop;
        outOfBoundsBag = ChunkSliceBag.UnboundedChunkSliceBag();
        inBoundsBag = ChunkSliceBag.ChunkSliceBagWithBounds(_xzBounds);
        floodFill = new FloodFill(map, shouldStop);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Flood-Fill-"+instanceCount++);
        while (!shouldStop.get()) {
            Coord3 chunkCoord = null;
            try {
                chunkCoord = chunkCoordsToBeFlooded.take();
                if (chunkCoord.equal(Coord3.SPECIAL_FLAG)) {
                    B.bugln("time to quit: " + Thread.currentThread().getName());
                    return;
                }
            } catch (InterruptedException e) {
                B.bugln(Thread.currentThread().getName() + " will stop now");
                break;
            }
            startFlood(chunkCoord);
        }
    }

    public static final boolean DONT_ACTUALLY_FLOOD_FILL = true;
    public void startFlood(Coord3 chunkCoord) {
        // * SHORT CIRCUIT THE WHOLE FLOOD FILL (DON'T FLOOD FILL--for testing) *
        if (DONT_ACTUALLY_FLOOD_FILL || WorldGenerator.TEST_DONT_BUILD)
        { try { floodFilledChunkCoords.put(chunkCoord); } catch (InterruptedException e) { e.printStackTrace(); } return; }

        Chunk chunk = map.GetChunk(chunkCoord);
        Asserter.assertChunkNotNull(chunk, chunkCoord);
        boolean originalChunkCoordWasNeverAdded = chunk.chunkFloodFillSeedSet.size() == 0;

        while(chunk.chunkFloodFillSeedSet.size() > 0) {
            if (shouldStop.get()) return;
            flood(chunk.chunkFloodFillSeedSet.removeNext());
        }

        /*
         * CHECK THE COLUMN OF THIS CHUNK CO
         * SEE IF THERE ARE ANY SLICES IN THE OUTOFBOUNDS-BAG IN THIS COLUMN
         * IF SO, REMOVE THEM AND FLOOD FILL WITH THEM
         */
        List<ChunkSlice> outOfBoundsBagSlices = outOfBoundsBag.getSlices();
        for(int i=0; i<outOfBoundsBagSlices.size(); ++i) {
            if (shouldStop.get()) return;
            ChunkSlice obbSlice = outOfBoundsBagSlices.get(i);
            if (obbSlice.getChunkCoord().x == chunkCoord.x && obbSlice.getChunkCoord().z == chunkCoord.z) {
                while(obbSlice.size() > 0) {
                    flood(obbSlice.removeNext());
                }
                outOfBoundsBagSlices.remove(i--);
            }
        }
        if (shouldStop.get()) return;
        // if there were no seeds (no overhangs) we still need to pass this chunk coord along
        if (originalChunkCoordWasNeverAdded) {
            try { floodFilledChunkCoords.put(chunkCoord); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
    private synchronized void putDirtyChunks() { //stab in the dark : 'synchronized'
        if (floodFill.dirtyChunks.size() == 0) return;
        Coord3[] dirtyChunks = new Coord3[floodFill.dirtyChunks.size()];
        dirtyChunks = floodFill.dirtyChunks.toArray(dirtyChunks);
        for(Coord3 dirty : dirtyChunks) {
            try { floodFilledChunkCoords.put(dirty); } catch (InterruptedException e) { e.printStackTrace(); }
            floodFill.dirtyChunks.remove(dirty);
        }
    }

    private void flood(Coord3 initialSeed) {
        Coord3 initialChunkCoord = Chunk.ToChunkPosition(initialSeed);

        /*
         * flood fill the initial chunk
         */
        ChunkSlice[] seedChunkShell = new ChunkSlice[6];
        initializeChunkShell(seedChunkShell, initialSeed);

        floodFill.flood(seedChunkShell, initialSeed);
        putDirtyChunks();

        /* add chunk slices to one or the other bounds bag from chunkShell after flood filling the initial seed */
        for(int i = 0; i <= Direction.ZPOS; ++i) {
            if (seedChunkShell[i].size() == 0)  { continue; }
            if (!inBoundsBag.add(seedChunkShell[i])) {
                outOfBoundsBag.add(seedChunkShell[i]);
            }
        }

        /*
         * get a slice from the in-bounds bag,
         * flood fill it and add it's shell sides
         * to either out or in bounds
         */
        depleteBag: while(inBoundsBag.size() > 0)
        {
            ChunkSlice chunkSlice = null;
            /*
             * get a chunk slice
             */
            ColumnMap columnMap = map.getApp().getColumnMap();
            while (chunkSlice == null) {
                if (inBoundsBag.size() == 0) {
                    break depleteBag;
                }
                // find a slice whose column IS_BUILT
                List<ChunkSlice> iBBSlices = inBoundsBag.getSlices();
                for(int i = 0; i < iBBSlices.size(); ++i) {
                    ChunkSlice slice = iBBSlices.remove(i); // iBBSlices.get(i);
                    if (slice.size() == 0) {  Asserter.assertFalseAndDie("happens?"); continue; } //TEST? NOT SURE?

                    if (columnMap.HasBuiltSurface(slice.getChunkCoord().x, slice.getChunkCoord().z)) {
                        chunkSlice = slice;
                        break;
                    } else {
                        outOfBoundsBag.add(slice); //TODO: figure how to handle out of bounds becoming inbounds
                        --i;
                    }
                }
            }

            while(chunkSlice != null && chunkSlice.size() > 0) {
                Coord3 seed = chunkSlice.removeNext();
                ChunkSlice[] chunkShell = new ChunkSlice[6];
                initializeChunkShell(chunkShell, seed);

                floodFill.flood(chunkShell, seed);
                for (int i = 0; i <= Direction.ZPOS; ++i) {
                    if (chunkShell[i].size() == 0) { continue; }
                    if (!inBoundsBag.add(chunkShell[i])) {
                        outOfBoundsBag.add(chunkShell[i]);
                    }
                }
            }

            putDirtyChunks();
        }
    }

    private Box boundsFromGlobal(Vector3f global) {
        return boundsFromChunkCoord(Chunk.ToChunkPosition(Coord3.FromVector3f(global)));
    }
    private Box boundsFromChunkCoord(Coord3 chunkCo) {
//        return new Box(Coord3.Zero.clone(), new Coord3(8, 4, 8)); //TEST
        return new Box(new Coord3(-20000, -100000, -20000), new Coord3(9000000, 9000000, 90000000)); //TEST
//        Coord3 start = chunkCo.minus(FFBoundsHalf);
//        start.y = Math.max(start.y, 0);
//        return new Box(start, FFBoundsHalf.multy(2)); //WANT
    }


    private void debugPrintHashSet(HashSet<Coord3> coord3s, Box bounds) {
        BoxIterator bi = new BoxIterator(bounds);
        B.bugln("---COORDS---");
        while(bi.hasNext()) {
            Coord3 n = bi.next();
            if (n.x == bounds.start.x) {
                B.bugln("");
            }
            String r;
            if (coord3s.contains(n)) {
                r = "X-";
            } else {
                r = "0-";
            }
            B.bug(r);
        }
        B.bugln("");
    }

    private void initializeChunkShell(ChunkSlice[] chunkSlices, Coord3 globalBlockCoord) {
        Coord3 chunkPosition = Chunk.ToChunkPosition(globalBlockCoord);
        for(int i = 0; i <= Direction.ZPOS; ++i) {
            chunkSlices[i] = new ChunkSlice(chunkPosition.add(Direction.DirectionCoords[i]),i);
        }
    }

}
