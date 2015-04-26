package voxel.landscape.chunkbuild.blockfacefind.floodfill;

import com.jme3.math.ColorRGBA;
import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkslice.ChunkSlice;
import voxel.landscape.coord.*;
import voxel.landscape.debug.DebugGeometry;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.light.LightComputerUtils;
import voxel.landscape.util.Asserter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 10/5/14.
 */
public class FloodFill
{
    private final TerrainMap map;
    public final HashSet<Coord3> dirtyChunks = new HashSet<>(24);
    private AtomicBoolean shouldStop;

    private static List<Coord3> fakeChunkList = new ArrayList<Coord3>();
    private static int fakeRange = 2;
    static {
        for(int x = -fakeRange; x < fakeRange ; ++x ) {
            for(int z = -fakeRange; z < fakeRange; ++z) {
                for(int y = 0; y < TerrainMap.GetWorldHeightInChunks(); ++y) {
                    fakeChunkList.add(new Coord3(x,y,z));
                }
            }
        }
    }

    public FloodFill(TerrainMap _map, AtomicBoolean _shouldStop) {
        map = _map;
        shouldStop = _shouldStop;
    }

    public void flood(ChunkSlice[] chunkSliceShell, Coord3 seedGlobal) {
//        dirtyChunks.clear(); // dirty chunks keeps track of edited chunks
        dirtyChunks.add(Chunk.ToChunkPosition(seedGlobal));

        ChunkSlice yPosChunkSlice = new ChunkSlice(Chunk.ToChunkPosition(seedGlobal), Direction.YPOS);
        ChunkSlice yNegChunkSlice = new ChunkSlice(Chunk.ToChunkPosition(seedGlobal), Direction.YNEG);
        floodScanLines(chunkSliceShell,yPosChunkSlice, yNegChunkSlice, seedGlobal, FloodFill4D.UntouchedType);
        List<ChunkSlice> slices = new ArrayList<ChunkSlice>(25);
        slices.add(yNegChunkSlice);
        slices.add(yPosChunkSlice);

        while(slices.size() > 0) {
            ChunkSlice nextSlice = slices.remove(0);
            ChunkSlice yPosChunkSliceNext = new ChunkSlice(Chunk.ToChunkPosition(seedGlobal), Direction.YPOS);
            ChunkSlice yNegChunkSliceNext = new ChunkSlice(Chunk.ToChunkPosition(seedGlobal), Direction.YNEG);
            while(nextSlice.size() > 0) {
                floodScanLines(chunkSliceShell, yPosChunkSliceNext, yNegChunkSliceNext, nextSlice.removeNext(), FloodFill4D.UntouchedType );
            }
            if (yPosChunkSliceNext.size() > 0) {
                slices.add(yPosChunkSliceNext);
            }
            if (yNegChunkSliceNext.size() > 0) {
                slices.add(yNegChunkSliceNext);
            }
        }
    }


    private static void addSeed(ArrayList<Coord3> seeds, Coord3 seed) {
        seeds.add(seed);
    }

    private ChunkSlice chunkSliceFromShell(ChunkSlice[] shell, int direction) {
        return shell[direction];
    }

    private static boolean IsCoordInDebugArea(Coord3 co) {
        Box debugBox = new Box(
                new Coord3(0,14,14),
                new Coord3(17, 17, 5)
        );
        return debugBox.contains(co);
    }

    /*
     * Finds transparent blocks (mostly air) below the surface in a given chunk, starting at a given seed coord
     * @param initialSeedGlobal
     * Uses the scan lines algorithm.
     * New seeds one block above and one block below (in Y) are store in the yPos/Neg "chunk slices"
     * New seeds found outside the chunk are also stored in chunkSlices (the ones in @param chunkSliceShell )
     * the "untouchedType" is always BlockType.NON_EXISTENT. It's a parameter b/c this might help provide some flexibility to
     * flood fill non air in the future?? (this idea is not really born out anywhere else) (TODO: decide what we need vis-a-vis flood filling in general).
     */
    public void floodScanLines(ChunkSlice[] chunkSliceShell, ChunkSlice yPosChunkSlice, ChunkSlice yNegChunkSlice, Coord3 initialSeedGlobal, int untouchedType) {

        // TODO: deal with water!

        ArrayList<Coord3> seeds = new ArrayList<Coord3>(Chunk.XLENGTH * Chunk.YLENGTH * Chunk.ZLENGTH);

        Asserter.assertTrue(initialSeedGlobal != null, "initial seed null...not good");

        Chunk seedChunk = map.lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(initialSeedGlobal));
        Box chunkBox = Box.WorldCoordBoxForChunk(seedChunk);

        /* wasIs arrays used to check whether blocks were unset before lookup:
         * we (mostly?) want to continue flooding only over air blocks that were of type "non_existant"
         * before we looked them up. *wasIs[0] stores the type before lookup.
         * *wasIs[1] stores the type after lookup */
        int[] thisCoordWasIs = new int[2];
        int[] xNEGWasIs = new int[2];
        int[] xPOSWasIs = new int[2];
        int[] zNEGWasIs = new int[2];
        int[] zPOSWasIs = new int[2];
        int[] yNEGWasIs = new int[2];
        int[] yPOSWasIs = new int[2];

        getCurrentWasIsWithinChunk(initialSeedGlobal, seedChunk, thisCoordWasIs, untouchedType);
//        if (map.isAboveSurface(initialSeedGlobal) || !BlockType.IsNonExistentOrPlaceHolderAir(thisCoordWasIs[0])) {

        if (map.isAboveSurface(initialSeedGlobal) || BlockType.IsFloodFilledAir(thisCoordWasIs[0])) {
            return;
        }

        seeds.add(initialSeedGlobal);
        int z1;
        boolean spanXNEG, spanXPOS;

        int xAreaStart = chunkBox.start.x, yAreaStart = chunkBox.start.y, zAreaStart = chunkBox.start.z;
        int xAreaEnd = chunkBox.extent().x, yAreaEnd = chunkBox.extent().y, zAreaEnd = chunkBox.extent().z;

        while(seeds.size() > 0) {
            Coord3 seed = seeds.remove(0);
            z1 = seed.z;


            Coord3 lessZNEGCoord = null;
            Coord3 previousZNEGForLight = null;
            int blockType = BlockType.NON_EXISTENT.ordinal();
            while (true) {
                if (shouldStop.get()) { return; }
                /*
                 * ZNEG (march backwards until we shouldn't anymore)
                 */
                int previousType = -1;
                if (lessZNEGCoord != null) {
                    previousZNEGForLight = lessZNEGCoord;
                    previousType = blockType;
                }
                lessZNEGCoord = new Coord3(seed.x, seed.y, z1);

                blockType = map.lookupOrCreateBlock(lessZNEGCoord);
                addFaceForType(seedChunk, chunkBox, lessZNEGCoord, Direction.ZPOS, blockType);
                if (BlockType.IsSolid(blockType) || map.isAboveSurface(lessZNEGCoord) || BlockType.IsFloodFilledAir(blockType)) {
                    z1++;
                    break;
                }

                else if (z1 == zAreaStart) {
                    Coord3 zNegNeighbor = new Coord3(seed.x, seed.y, z1 - 1);
                    map.setIsGetWasIsUnsetIfAir(zNegNeighbor, untouchedType, zNEGWasIs);

                    if (ShouldSeedBlock(zNegNeighbor, zNEGWasIs)) {
                        chunkSliceFromShell(chunkSliceShell, Direction.ZNEG).addCoord(zNegNeighbor);
                    }
                    addFaceForType(seedChunk, chunkBox, zNegNeighbor, Direction.ZPOS, zNEGWasIs[1]);

                    offerLightBothWays(lessZNEGCoord, zNegNeighbor, blockType, zNEGWasIs[1]);
                    break;
                }
                /* light */
                if (previousZNEGForLight != null) {
                    offerLightBothWays(previousZNEGForLight, lessZNEGCoord, previousType, blockType);
                }
                z1--;
            }
            spanXNEG = spanXPOS = false;
            /************
             * Walk in ZPOS direction.
             * Add seeds and neighbor seeds as needed.
             * if wasIs[0] for x/y neg/pos != NON EXISTENT, don't seed. it's already been covered
             * On the other hand, if we hit AIR (as opposed to NON E) on THE CURRENT block, it doesn't mean we've covered
             * this area already, so keep going.
             * Set blocks back to NON EXISTENT when inspecting blocks beyond this chunk
             ************/
            Coord3 subject = null;
            Coord3 previousSubjectForLight = null;
            int previousSubjectBlockType = -1;
            int iterationSafetyCount = 0;
            while(true) {
                if (shouldStop.get()) { return; }
                if (iterationSafetyCount++ > 8000) { Asserter.assertFalseAndDie("death by iteration: z1 : " + z1 + "chunk box: " + chunkBox.toString()); return; }

                previousSubjectForLight = subject;
                previousSubjectBlockType = thisCoordWasIs[1];

                subject = new Coord3(seed.x, seed.y, z1);
                /*
                 * Look up the current block. And possibly set it in the map as a side effect.
                 */
                getCurrentWasIsWithinChunk(subject, seedChunk, thisCoordWasIs, untouchedType);

                // TODO: figure out why that sliver still doesn't get flood filled
                /*
                 * ZPOS: hit a wall?
                 * */
                addFaceForType(seedChunk, chunkBox, subject, Direction.ZNEG, thisCoordWasIs[1]);
                if (BlockType.IsSolid(thisCoordWasIs[1]) || map.isAboveSurface(subject) || BlockType.IsFloodFilledAir(thisCoordWasIs[1])) {
                    break;
                }
                setFloodFilledAirIfAir(subject, thisCoordWasIs[1]);
                if(previousSubjectForLight != null) {
                    offerLightBothWays(previousSubjectForLight, subject, previousSubjectBlockType, thisCoordWasIs[1]);
                }

                /*
                 * XNEG
                 */
                {
                    Coord3 xNegNeighbor = new Coord3(seed.x - 1, seed.y, z1);
                    if (seed.x > xAreaStart) {
                        getCurrentWasIsWithinChunk(xNegNeighbor, seedChunk, xNEGWasIs, untouchedType);
                    } else {
                        map.setIsGetWasIsUnsetIfAir(xNegNeighbor, untouchedType, xNEGWasIs);
                    }
                    if (ShouldSeedBlock(xNegNeighbor, xNEGWasIs)) {
                        if (seed.x == xAreaStart) {
                            chunkSliceFromShell(chunkSliceShell, Direction.XNEG).addCoord(xNegNeighbor);
                        } else if (!spanXNEG) {
                            addSeed(seeds, xNegNeighbor);
                            spanXNEG = true;
                        }
                    } else {
                        spanXNEG = false;
                    }
                    addFaceForType(seedChunk, chunkBox, xNegNeighbor, Direction.XPOS, xNEGWasIs[1]);
                    offerLightBothWays(subject, xNegNeighbor, thisCoordWasIs[1], xNEGWasIs[1]);
                }

                /*
                 * XPOS
                 */
                {
                    Coord3 xPosNeighbor = new Coord3(seed.x + 1, seed.y, z1);
                    if (seed.x < xAreaEnd - 1) {
                        getCurrentWasIsWithinChunk(xPosNeighbor, seedChunk, xPOSWasIs, untouchedType);
                    } else {
                        map.setIsGetWasIsUnsetIfAir(xPosNeighbor, untouchedType, xPOSWasIs);
                    }
                    if (ShouldSeedBlock(xPosNeighbor, xPOSWasIs)) {
                        if (seed.x == xAreaEnd - 1) {
                            chunkSliceFromShell(chunkSliceShell, Direction.XPOS).addCoord(xPosNeighbor);
                        } else if (!spanXPOS) {
                            addSeed(seeds, xPosNeighbor);
                            spanXPOS = true;
                        }
                    }
                    else {
                        spanXPOS = false;
                    }
                    addFaceForType(seedChunk, chunkBox, xPosNeighbor, Direction.XNEG, xPOSWasIs[1]);
                    offerLightBothWays(subject, xPosNeighbor, thisCoordWasIs[1], xPOSWasIs[1]);
                }

                /*
                 * YNEG
                 */
                Coord3 yNegNeighbor = new Coord3(seed.x, seed.y - 1, z1);
                map.setIsGetWasIsUnsetIfAir(yNegNeighbor, untouchedType, yNEGWasIs);
                if (ShouldSeedBlock(yNegNeighbor, yNEGWasIs)) {
                    if (seed.y == yAreaStart) {
                        chunkSliceFromShell(chunkSliceShell, Direction.YNEG).addCoord(yNegNeighbor);
                    } else {
                        yNegChunkSlice.addCoord(yNegNeighbor);
                    }
                }
                addFaceForType(seedChunk, chunkBox, yNegNeighbor, Direction.YPOS, yNEGWasIs[1]);
                offerLightBothWays(subject, yNegNeighbor, thisCoordWasIs[1], yNEGWasIs[1]);

                /*
                 * YPOS
                 */
                Coord3 yPosNeighbor = new Coord3(seed.x, seed.y + 1, z1);
                map.setIsGetWasIsUnsetIfAir(yPosNeighbor, untouchedType, yPOSWasIs);

                if (ShouldSeedBlock(yPosNeighbor, yPOSWasIs)){
                    if (seed.y == yAreaEnd - 1) {
                        chunkSliceFromShell(chunkSliceShell, Direction.YPOS).addCoord(yPosNeighbor);
                    } else {
                        yPosChunkSlice.addCoord(yPosNeighbor);
                    }
                }
                addFaceForType(seedChunk, chunkBox, yPosNeighbor, Direction.YNEG, yPOSWasIs[1]);
                offerLightBothWays(subject, yPosNeighbor, thisCoordWasIs[1], yPOSWasIs[1]);

                /*
                 * SURFACE CHECK
                 */ //PURGE? this will never be true here (checked already)
                if (map.isAboveSurface(new Coord3(seed.x, seed.y, z1 ))) { break; }

                /*
                 * ZPOS
                 */
                if (z1 == zAreaEnd - 1) {
                    Coord3 zPosNeighbor = new Coord3(seed.x, seed.y, z1 + 1);
                    map.setIsGetWasIsUnsetIfAir(zPosNeighbor, untouchedType, zPOSWasIs);

                    if (ShouldSeedBlock(zPosNeighbor, zPOSWasIs)) {
                        chunkSliceFromShell(chunkSliceShell, Direction.ZPOS).addCoord(zPosNeighbor);
                    }
                    addFaceForType(seedChunk, chunkBox, zPosNeighbor, Direction.ZNEG, zPOSWasIs[1]);
                    break;
                }

                z1++;
            }
        }
        return;
    }

    private boolean ShouldSeedBlock(Coord3 global, int[] wasIs) {
        if (wasIs[1] == BlockType.AIR.ordinal() && !map.isAboveSurface(global)) return true;
        return wasIs[0] == BlockType.NON_EXISTENT.ordinal() && (wasIs[1] == BlockType.AIR.ordinal() || wasIs[1] == BlockType.FLOODFILLED_AIR.ordinal());
    }

    // add a face to a block in direction. Use seedChunk if it contains the block coord to cut down on looks up in map
    private void addFaceForType(Chunk seedChunk, Box chunkBounds, Coord3 globalBlockLocation, int direction, int blockType) {
        Coord3 updatedChunkCoord = map.setBlockFaceForChunkIfType(seedChunk, chunkBounds, globalBlockLocation, direction, blockType);
        if (updatedChunkCoord != null) { dirtyChunks.add(updatedChunkCoord); }
    }
    private void getCurrentWasIsWithinChunk(Coord3 global, Chunk chunk, int[] wasIs, int untouchedType) {
        wasIs[0] = chunk.blockAt(Chunk.ToChunkLocalCoord(global));
        if (wasIs[0] != FloodFill4D.UntouchedType) {
            wasIs[1] = wasIs[0];
            return;
        }
        wasIs[1] = map.lookupOrCreateBlock(global);
    }
    private void setFloodFilledAirIfAir(Coord3 global, int block) {
        if (block == BlockType.AIR.ordinal()) {
            map.setBlockAtWorldCoord(BlockType.FLOODFILLED_AIR.ordinal(), global);
        }
    }

    public static final boolean FLOOD_FILL_HANDLES_LIGHT = true;
    private void offerLightBothWays(Coord3 from, Coord3 to, int fromType, int toType){
        if (FLOOD_FILL_HANDLES_LIGHT) {
            if (!BlockType.IsTranslucent(toType)) return;
            int fromLightLevel = map.GetSunLightmap().GetLight(from);
            int toLightLevel = map.GetSunLightmap().GetLight(to);
            if (fromLightLevel == toLightLevel) return;
            if (fromLightLevel > toLightLevel) {
                map.GetSunLightmap().SetMaxLight((byte) (fromLightLevel - LightComputerUtils.GetLightStep(fromType)), to);
            } else {
                map.GetSunLightmap().SetMaxLight((byte) (toLightLevel - LightComputerUtils.GetLightStep(toType)), from);
            }
        }
    }

    private static Box2 DebugBox = new Box2(new Coord2(-1), new Coord2(0));
    private static void debugBlockWithinBounds(Coord3 global, ColorRGBA color) {
        DebugGeometry.AddDebugBlock(global, color);
    }

}