package voxel.landscape.map;

import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.VoxelLandscape;
import voxel.landscape.WorldGenerator;
import voxel.landscape.collection.chunkface.ChunkBlockFaceMap;
import voxel.landscape.collection.coordmap.managepages.ConcurrentHashMapCoord3D;
import voxel.landscape.coord.*;
import voxel.landscape.debug.DebugGeometry;
import voxel.landscape.map.light.LightComputer;
import voxel.landscape.map.light.LightMap;
import voxel.landscape.map.light.SunLightComputer;
import voxel.landscape.map.light.SunLightMap;
import voxel.landscape.map.water.LiquidUpdater;
import voxel.landscape.map.water.WaterFlowComputer;
import voxel.landscape.map.water.WaterLevelMap;
import voxel.landscape.noise.IBlockDataProvider;
import voxel.landscape.noise.TerrainDataProvider;
import voxel.landscape.util.Asserter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static voxel.landscape.BlockType.*;

public class TerrainMap implements IBlockDataProvider
{
	private static final int MIN_DIM_HORIZONTAL = -8;
	private static final int MAX_DIM_HORIZONTAL = 8;

    private static final int MIN_CHUNK_DIM_VERTICAL = 0;
	private static final int MAX_CHUNK_DIM_VERTICAL = 4;

	public static Coord3 MIN_CHUNK_COORD = new Coord3(MIN_DIM_HORIZONTAL, MIN_CHUNK_DIM_VERTICAL, MIN_DIM_HORIZONTAL);
	public static Coord3 MAX_CHUNK_COORD = new Coord3(MAX_DIM_HORIZONTAL, MAX_CHUNK_DIM_VERTICAL, MAX_DIM_HORIZONTAL);

	TerrainDataProvider terrainDataProvider = new TerrainDataProvider(VoxelLandscape.WorldSettings.seed);

    private ConcurrentHashMapCoord3D<Chunk> chunks = new ConcurrentHashMapCoord3D<Chunk>(Chunk.class);
	private SunLightMap sunLightmap = new SunLightMap();
	private LightMap lightmap = new LightMap();

    private WaterLevelMap liquidLevelMap = new WaterLevelMap();
    private LiquidUpdater liquidUpdaterWater = new LiquidUpdater(LiquidFlowTimeStepSeconds(WATER.ordinal()));

    private final VoxelLandscape app;

    public final BlockingQueue<Coord3> chunkCoordsToBeFlooded = new ArrayBlockingQueue<Coord3>(128);
    public final BlockingQueue<Coord3> chunkCoordsToBePriorityFlooded = new ArrayBlockingQueue<Coord3>(128);
    
    private final Lock lock = new ReentrantLock(true);

	public TerrainMap(VoxelLandscape _app) {
        app = _app;
	}

    public VoxelLandscape getApp() { return app; }

    /*
     * DYNAMIC MAP UPDATES
     */
    public void mapUpdate(float tpf) {
        liquidUpdaterWater.StepScatter(this, tpf);
    }

    /*
     * CHUNK BLOCK FACE
     */
    public ChunkBlockFaceMap blockFaceMap(Coord3 chunkCoord) {
        return lookupOrCreateChunkAtPosition(chunkCoord).chunkBlockFaceMap;
    }
    // add a face to a block in direction. Use chunk if it contains the block coord to cut down on looks up in map
    public Coord3 setBlockFaceForChunkIfType(Chunk chunk, Box chunkBounds, Coord3 globalBlockLocation, int direction, int blockType) {
        if (BlockType.IsRenderedType(blockType)) {
            if (chunkBounds.contains(globalBlockLocation)) {
                setBlockFaceForChunk(chunk, globalBlockLocation, direction, true);
//                seedChunk.chunkBlockFaceMap.addFace(Chunk.ToChunkLocalCoord(globalBlockLocation), direction);
//                dirtyChunks.add(chunk.position);
                return chunk.position;
            }
            setBlockFace(globalBlockLocation, direction, true);
//            dirtyChunks.add(Chunk.ToChunkPosition(globalBlockLocation));
            return Chunk.ToChunkPosition(globalBlockLocation);
        }
        return null;
    }

    public void setBlockFace(Coord3 worldCoord, int direction, boolean shouldExist) {
        Coord3 chunkCo = Chunk.ToChunkPosition(worldCoord);
//        Coord3 localCo = Chunk.ToChunkLocalCoord(worldCoord);
        Chunk chunk = lookupOrCreateChunkAtPosition(chunkCo);
        setBlockFaceForChunk(chunk, worldCoord, direction, shouldExist);
//        if (shouldExist) {
//            chunk.chunkBlockFaceMap.addFace(localCo, direction);
//        } else {
//            chunk.chunkBlockFaceMap.removeFace(localCo, direction);
//        }
    }
    public void setBlockFaceForChunk(Chunk chunk, Coord3 worldCoord, int direction, boolean shouldExist) {
        if (shouldExist) {
            chunk.chunkBlockFaceMap.addFace(Chunk.ToChunkLocalCoord(worldCoord), direction);
        } else {
            chunk.chunkBlockFaceMap.removeFace(Chunk.ToChunkLocalCoord(worldCoord), direction);
        }
    }
    /*
     * WORLD INFO
     */
	public static int GetWorldHeightInChunks() {
		return MAX_CHUNK_DIM_VERTICAL - MIN_CHUNK_DIM_VERTICAL;
	}

	public static int GetWorldHeightInBlocks() {
		return GetWorldHeightInChunks() * Chunk.YLENGTH;
	}

    /*
     * BLOCK INFO
     */
    public int lookupBlock(int x, int y, int z) {
		return lookupBlock(new Coord3(x, y, z));
	}

	public int lookupBlock(Coord3 co) {
		Chunk chunk = getChunk(Chunk.ToChunkPosition(co));
		if (chunk == null)
			return NON_EXISTENT.ordinal();
		return chunk.blockAt(Chunk.ToChunkLocalCoord(co));
	}

	public boolean blockAtWorldCoordIsTranslucent(Coord3 co) {
		return blockAtWorldCoordIsTranslucent(co.x, co.y, co.z);
	}

	public boolean blockAtWorldCoordIsTranslucent(int x, int y, int z) {
		Chunk chunk = getChunk(Chunk.ToChunkPosition(x, y, z));
		if (chunk == null) return false;
		return IsTranslucent(chunk.blockAt(Chunk.ToChunkLocalCoord(x, y, z)));
	}

	public void setBlockAtWorldCoord(int block, Coord3 pos) {
		setBlockAtWorldCoord(block, pos.x, pos.y, pos.z);
	}

	public void setBlockAtWorldCoord(int block, int x, int y, int z) {
		Chunk chunk = lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(x, y, z));
		if (chunk != null) {
            chunk.setBlockAt(block, Chunk.ToChunkLocalCoord(x, y, z));
        }
	}
    /*
     * GET IS SET WASIS (BLOCK LOOK UP)
     */
    public void setIsGetWasIs(Coord3 global, int untouchedType, int[] wasIs) {
        Chunk chunk = lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(global));
        Coord3 local = Chunk.ToChunkLocalCoord(global);
        wasIs[0] = chunk.blockAt(local);
        wasIs[1] = lookupOrCreateBlock(global);
    }

    public void setIsGetWasIsUnsetIfAir(Coord3 woco, int untouchedType, int[] wasIs) {
        setIsGetWasIs(woco, untouchedType, wasIs);
        if (wasIs[0] == untouchedType && wasIs[1] == AIR.ordinal()) {
            setBlockAtWorldCoord(untouchedType, woco);
        }
    }

    public int getMinChunkCoordY() { return MIN_CHUNK_DIM_VERTICAL; }
    public int getMaxChunkCoordY() { return MAX_CHUNK_DIM_VERTICAL; }

	@Override
    public int lookupOrCreateBlock(Coord3 woco) {
        return lookupOrCreateBlock(woco,  terrainDataProvider);
    }
	
	@Override
    public int lookupOrCreateBlock(int xin, int yin, int zin) {
        return lookupOrCreateBlock(new Coord3(xin,yin,zin), terrainDataProvider);
    }
    public int lookupOrCreateBlock(Coord3 woco, TerrainDataProvider _terrainData) {
        int block = lookupBlock(woco);
        if (NON_EXISTENT.equals((int) block) && ChunkCoordWithinWorldBounds(Chunk.ToChunkPosition(woco))) {
            //synchronized block
            synchronized (lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(woco)).blockLockInstanceAt(Chunk.ToChunkLocalCoord(woco))) {
                block = _terrainData.getBlockDataAtPosition(woco.x, woco.y, woco.z);
                setBlockAtWorldCoord(block, woco.x, woco.y, woco.z);
            }
        }
        return block;
    }
    public boolean ChunkCoordWithinWorldBounds(Coord3 chunkCoord) {
        return chunkCoord.y >= MIN_CHUNK_DIM_VERTICAL && chunkCoord.y < MAX_CHUNK_DIM_VERTICAL;
    }
    public boolean isGlobalCoordWithinWorldBounds(Coord3 global) {
        return global.y >= MIN_CHUNK_DIM_VERTICAL * Chunk.YLENGTH && global.y < MAX_CHUNK_DIM_VERTICAL * Chunk.YLENGTH;
    }

    /*
     * Clean up
     */
    public void writeChunkAndColumn(Coord3 chunkPos) {
        Chunk ch = getChunk(chunkPos);
        if (ch != null) {
            ch.writeToFile();
            ch.hasStartedWriting.set(false);
        }
//        lightmap. write data ...
//        sunLightmap.writeLightsToFile(chunkPos);
//        TODO: write water data
        sunLightmap.writeRaysToFile(Coord2.FromCoord3XZ(chunkPos));
    }

    public void removeColumn(ICoordXZ iCoordXZ) {
        if (!columnEmpty(iCoordXZ)) return;
        sunLightmap.RemoveRays(iCoordXZ.getX(), iCoordXZ.getZ());
        app.getColumnMap().Destroy(iCoordXZ.getX(), iCoordXZ.getZ());
    }
    
    public void removeChunk(Coord3 chunkPos) {
    	Chunk ch = getChunk(chunkPos);
        if (ch == null) {
            return;
        }
        if (ch.isWriteDirty()) {
            return;
        }
        chunks.Remove(chunkPos);
        sunLightmap.RemoveLightData(chunkPos);
        lightmap.RemoveLightData(chunkPos);
        // TODO: remove water data
        ch.getChunkBrain().clearMeshBuffers();
        ch.getChunkBrain().detachNodeFromParent();
        ch.getChunkBrain().setSpatialNull();
    }

    public boolean columnHasHiddenChunk(ICoordXZ look) {
        for (Coord3 coord3 : new ColumnRange(look)) {
            Chunk chunk = getChunk(coord3);
            if (chunk != null && chunk.getChunkBrain().isHiding()) {
                return true;
            }
        }
        return false;
    }
    public boolean columnEmpty(ICoordXZ look) {
        for (Coord3 coord3 : new ColumnRange(look)) {
            Chunk chunk = getChunk(coord3);
            if (chunk != null) {
                return false;
            }
        }
        return true;
    }

	/*
	 * populate chunk with block values
	 */
    public static final boolean CRAWL_OVER_SURFACE = true;

    public void generateSurface(int x, int z) {
        generateSurface(x, z, terrainDataProvider, null);
    }
    public void generateSurface(int x, int z, TerrainDataProvider _dataProvider, HashSet<Coord3> touchedChunkCoords) {
        Coord3 chunkPos = new Coord3(x, MAX_CHUNK_DIM_VERTICAL - 1, z);
        instantiateChunksInColumn(x, z);

        // try creating from file
        sunLightmap.readRaysFromFile(new Coord2(x, z));
        // TODO: read lights, water, ? from file
        if (!sunLightmap.raysWriteDirty(new Coord2(x, z))) {
            if (columnChunksBuiltFromFile(x, z)) {
                addCoordsInColumn(touchedChunkCoords, x, z);
                return;
            }
        }

        if (CRAWL_OVER_SURFACE) {
            crawlOverSurface(chunkPos.x, chunkPos.z, _dataProvider, touchedChunkCoords);
        } else {
            generateNoiseForColumnToSurface(chunkPos.x, chunkPos.z, _dataProvider, touchedChunkCoords);
        }
    }
    private void instantiateChunksInColumn(int x, int z) {
        for (Coord3 chunkPos = new Coord3(x, MAX_CHUNK_DIM_VERTICAL - 1, z); chunkPos.y >= MIN_CHUNK_DIM_VERTICAL; chunkPos.y--) {
            lookupOrCreateChunkAtPosition(chunkPos);
        }
    }
    public Coord3 writeDirtyButNotWritingChunkInColumn(int x, int z) {
        for (Coord3 chunkPos = new Coord3(x, MAX_CHUNK_DIM_VERTICAL - 1, z); chunkPos.y >= MIN_CHUNK_DIM_VERTICAL; chunkPos.y--) {
            Chunk chunk = getChunk(chunkPos);
            if (chunk != null && chunk.isWriteDirty() && !chunk.hasStartedWriting.get()) return chunkPos;
        }
        return null;
    }
//    public void updateColumnBuilt(int x, int z) {
//        boolean columnBuilt = true;
//        for (Coord3 chunkPos = new Coord3(x, MAX_CHUNK_DIM_VERTICAL - 1, z); chunkPos.y >= MIN_CHUNK_DIM_VERTICAL; chunkPos.y--) {
//            Chunk chunk = GetChunk(chunkPos);
//            if (chunk != null && !chunk.getHasEverStartedBuilding()) {
//                return;
//            }
//        }
//        if (columnBuilt) {
//            app.getColumnMap().SetIsBuiltIfLessThanBuilt(x, z);
//        }
//    }
    public boolean columnChunksBuiltFromFile(int x, int z) {
        for (Coord3 chunkPos = new Coord3(x, MAX_CHUNK_DIM_VERTICAL - 1, z); chunkPos.y >= MIN_CHUNK_DIM_VERTICAL; chunkPos.y--) {
            Chunk chunk = lookupOrCreateChunkAtPosition(chunkPos);
            if (!chunk.getHasGenerated() || !chunk.isWriteDirty()) return false;
        }
        return true;
    }
    public void addCoordsInColumn(HashSet<Coord3> coord3HashSet,int x, int z) {
        for (Coord3 chunkPos = new Coord3(x, MAX_CHUNK_DIM_VERTICAL - 1, z); chunkPos.y >= MIN_CHUNK_DIM_VERTICAL; chunkPos.y--) {
            coord3HashSet.add(chunkPos.clone());
        }
    }

    /*
     * get the terrain data inside the chunk and also in the -1/+1 box (in block units) that
     * surrounds it (within world limits if any)
     */
    private boolean generateNoiseForColumnToSurface(int cx, int cz, TerrainDataProvider _dataProvider, HashSet<Coord3> touchedChunkCoords) {

        Coord3 worldPos = Chunk.ToWorldPosition(new Coord3(cx, 0, cz));
        Coord3 relPos, absPos, chunkPos = null;
        Chunk possibleChunk = null;

        boolean foundDirt = false;
        for (int i = -1; i < Chunk.CHUNKDIMS.x + 1; ++i) {
            for (int j = -1; j < Chunk.CHUNKDIMS.z + 1; ++j) {
                for (int wocoY = MAX_CHUNK_DIM_VERTICAL * Chunk.YLENGTH - 1 ; wocoY >= MIN_CHUNK_DIM_VERTICAL * Chunk.YLENGTH ; --wocoY) {
                    int k = Chunk.ToChunkLocalY(wocoY);
                    worldPos.y = Chunk.ToWorldPositionY(Chunk.ToChunkPositionY(wocoY), 0);
                    relPos = new Coord3(i, k, j);
                    absPos = worldPos.add(relPos);
                    Coord3 nextChunkPos = Chunk.ToChunkPosition(absPos);
                    if (chunkPos == null || !chunkPos.equal(nextChunkPos)) {
                        chunkPos = nextChunkPos.clone();
                        possibleChunk = lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(absPos));
                    }

                    if (VoxelLandscape.DO_USE_TEST_GEOMETRY) return false;
                    if (possibleChunk == null) continue; // must be at world limit?

                    if (i == 0 && j == 0)
                        touchedChunkCoords.add(nextChunkPos);

                    int block = lookupOrCreateBlock(absPos, _dataProvider);

					/*
					 * CONSIDER: grass is too simplistic! grass should grow only
					 * where there's light... Also, put this logic somewhere
					 * where it will also apply to newly created, destroyed blocks
					 * may need a 'block time step' concept...
					 */
                    // should this block be grass?
//                    if (DIRT.ordinal() == block) {
//                        Coord3 upOne = absPos.add(Coord3.ypos);
//                        if (lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(upOne)) == null ||
//                                IsTranslucent((byte) lookupOrCreateBlock(upOne))) {
//                            block = (byte) GRASS.ordinal();
//                        }
//                    } else
                    if (WATER.ordinal() == block) {
                        liquidLevelMap.SetWaterLevel(WaterFlowComputer.MAX_WATER_LEVEL, absPos);
                        possibleChunk.getChunkBrain().SetLiquidDirty();
                    }

//                    relPos = Chunk.ToChunkLocalCoord(relPos);
//                    possibleChunk.setBlockAt(block, relPos);

                    //DO WE WANT THIS MECHANISM??
//                    if (!foundDirt && k > -1 && j > -1 && i > -1 && k <= Chunk.YLENGTH && j <= Chunk.ZLENGTH && i <= Chunk.XLENGTH) { foundDirt = IsSolid(block); }

                    if (IsSolid(block)) {
                        relPos = Chunk.ToChunkLocalCoord(relPos);
                        if (DIRT.ordinal() == block) {
                            block = GRASS.ordinal();
                            possibleChunk.setBlockAt(block, relPos);
                        }
                        possibleChunk.chunkBlockFaceMap.addFace(relPos, Direction.YPOS);
                        /* Set sunlight map */
                        setSurfaceHeight(new Coord3(absPos.x, wocoY, absPos.z ));
                        break;
                    }
                    // Impractical for large bodies of water: too many vertices so...
                    // TODO: set up flood filling over translucents. Need a totally separate flood fill process.
                    // TODO: but, could also have a special process for 'meshing' lakes: assuming they will mostly not have
                    // TODO: underground rivers (flood fill those), just find all of the columns that constitute the lake and
                    // TODO: meld them into a mesh, etc..
                    // TODO: first step: make the flood fill 'block type agnostic'.
//                    if (BlockType.IsRenderedType(block)) {
//                        for(int dir : Direction.Directions)
//                            setBlockFaceForChunkIfType(possibleChunk, possibleChunkBounds, absPos, dir, block );
//                    }
                }
            }
        }
//        targetChunk.setIsAllAir(!foundDirt);
        return true;
    }

    /*
     * Quicker surface finding: when finding surface at an x/z, check its x,z neighbors. take the highest non-zero neighbor height.
     * check block at point x,neighbor-height,z. If air, go down; solid, go up.
     * (CONSIDER: go more aggressively as you find more of the same—i.e.skip by 2 and then 3 etc.)
     * Did we crawl into a cave?
     * if there's a sufficient height dif between two checked neighbors (and both have height != zero), do a revised height check on
     * all neighbors of the higher one.
     * if, after the revision, any neighbor now has a sufficient height dif with all of its neighbors, recur.
     */
    private boolean crawlOverSurface(int cx, int cz, TerrainDataProvider _dataProvider, HashSet<Coord3> touchedChunkCoords) {
        Coord3 worldPos = Chunk.ToWorldPosition(new Coord3(cx, 0, cz));
        Coord3 relPos, global, chunkPos = null;
        Chunk possibleChunk = null;
        Deque<Coord3> caveCrawlList = new ArrayDeque<>(40);
        Box2 columnFootprint = new Box2(new Coord2(0), new Coord2(Chunk.CHUNKDIMS.x));
        for (int i = -1; i < Chunk.CHUNKDIMS.x + 1; ++i) {
            for (int j = -1; j < Chunk.CHUNKDIMS.z + 1; ++j) {
                //find start y

                relPos = new Coord3(i, 0, j);
                global = worldPos.add(relPos);
                global.y = highestNegativeNeighbor(global, GetWorldHeightInBlocks() - 1);
                relPos.y = Chunk.ToChunkLocalY(global.y);
                /* find the surface absPos y */
                int block = findSurfaceFrom(global, _dataProvider, touchedChunkCoords);
                if (columnFootprint.containsXZ(relPos)) touchedChunkCoords.add(Chunk.ToChunkPosition(global));

                /* for 'safety' go up by CHECK_SURFACE_ABOVE_HEIGHT blocks to see if we get an overhang. */
                for (Coord3 checkAbove = global.clone(); checkAbove.y < global.y + CHECK_SURFACE_ABOVE_HEIGHT; checkAbove.y++) {
                    if (checkAbove.y == GetWorldHeightInBlocks() - 1) { break; }
                    int checkBlock = lookupOrCreateBlock(checkAbove);
                    if (!BlockType.IsAir(checkBlock)) {
                        block = findSurfaceFrom(checkAbove, _dataProvider, touchedChunkCoords);
                        global.y = checkAbove.y;
                    }
                }
                lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(global)).chunkBlockFaceMap.addFace(Chunk.ToChunkLocalCoord(global), Direction.YPOS);
                //*** TEST
                if (WorldGenerator.TEST_DONT_BUILD) {
                    if (i == 0) return true;
                }
                crawlOutOfCave(global, _dataProvider, caveCrawlList, touchedChunkCoords);
            }
        }
        return true;
    }
    private int findSurfaceFrom(Coord3 global, TerrainDataProvider _dataProvider, HashSet<Coord3> touchedChunkCoords) {
        int block = lookupOrCreateBlock(global, _dataProvider);
        int lastBlock = 0;
        boolean startedInAir = BlockType.IsAir(block);
        int increment = startedInAir ? -1 : 1;
        int y_limit = increment < 0 ? 0 : GetWorldHeightInBlocks() - 1;
        while(true) {
            if (global.y == y_limit) { break; }
            global.y += increment;
            block = lookupOrCreateBlock(global, _dataProvider);
            if (startedInAir != BlockType.IsAir(block)) { break; }
            lastBlock = block;
        }
        if (!startedInAir) { global.y--; }
        setSurfaceHeight(global);
//        touchedChunkCoords.add(Chunk.ToChunkPosition(global));
        return !startedInAir ? lastBlock : block;
    }
    private static final int CHECK_SURFACE_ABOVE_HEIGHT = 8;
    private void crawlOutOfCave(Coord3 global, TerrainDataProvider _dataProvider, Deque<Coord3> coords, HashSet<Coord3> touchedChunkCoords) {
        coords.clear();
        coords.add(global);
        crawlOutOfCave(_dataProvider, coords, touchedChunkCoords);
    }

    /* is height dif with neg neighbors greater than CHECK_SURFACE_ABOVE_HEIGHT?
     * if so, we may have crawled into a cave.
     * recursively go back over neg neighbors and see if we can adjust their surface heights (upwards).
     */
    private void crawlOutOfCave(TerrainDataProvider _dataProvider, Deque<Coord3> coords, HashSet<Coord3> touchedChunkCoords) {
        Coord3 global, neighbor;
        int height, neighborHeight;
        while(coords.size() > 0) {
            global = coords.poll();
            height = getSurfaceHeight(global);
            for(Coord3 co : Direction.DirectionXZNegCoords) {
                neighbor = global.add(co);
                neighborHeight = getSurfaceHeight(neighbor);
                if (neighborHeight > 0 && height - neighborHeight > CHECK_SURFACE_ABOVE_HEIGHT) {
                    int block = findSurfaceFrom(neighbor, _dataProvider, touchedChunkCoords);
                    lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(neighbor)).chunkBlockFaceMap.addFace(Chunk.ToChunkLocalCoord(neighbor), Direction.YPOS);
                    if (neighbor.y > neighborHeight) {
                        coords.add(neighbor.clone());
                        touchedChunkCoords.add(Chunk.ToChunkPosition(neighbor));
                    }
                }
            }
        }
    }
    private int highestNegativeNeighbor(Coord3 global, int defaultY) {
        int xneg = getSurfaceHeight(global.x - 1, global.z);
        int zneg = getSurfaceHeight(global.x, global.z - 1);
        if (xneg == 0 && zneg == 0) {
            return defaultY;
        }
        return zneg > xneg ? zneg : xneg;
    }

    /*
     * populate chunk flood fill seed set
     */
    public void populateFloodFillSeedsUpdateFaceMapsInChunkColumn(int cx, int cz, TerrainDataProvider _terrainDataProvider, HashSet<Coord3> touchedChunkCoords) {
        if (WorldGenerator.TEST_DONT_BUILD) return;
        Asserter.assertTrue(!columnChunksBuiltFromFile(cx, cz), "wha: " + cx + ", " + cz);
        if (columnChunksBuiltFromFile(cx, cz)) {
            return;
        }
        Coord3 worldPos = Chunk.ToWorldPosition(new Coord3(cx, 0, cz));

        Coord3 surfacePos;
        /*
         * FIND OVERHANGS: SOLID COLUMNS WITH AIR UNDERNEATH THEM. AND THE AIR IS NOT COVERED BY THEIR NEIGHBOR COLUMNS
         */
        // X and Z pos
        for (int i = -1; i < Chunk.XLENGTH; ++i) {
            for (int j = -1; j < Chunk.ZLENGTH; ++j) {
                if (i == -1 && j == -1) continue;
                surfacePos = worldPos.add(new Coord3(i, 0, j));
                surfacePos.y = getSurfaceHeight(surfacePos.x, surfacePos.z);

                if (i > -1 && j < Chunk.ZLENGTH - 1) {
                    climbUpCliff(surfacePos, surfacePos.y, Direction.ZPOS, _terrainDataProvider, touchedChunkCoords);
                }
                if (j > -1 && i < Chunk.XLENGTH - 1) {
                    climbUpCliff(surfacePos, surfacePos.y, Direction.XPOS, _terrainDataProvider, touchedChunkCoords);
                }
            }
        }
        // X and Z neg
        for (int i = Chunk.XLENGTH; i >= 0 ; --i) {
            for (int j = Chunk.ZLENGTH; j >= 0 ; --j) {
                if (i == Chunk.XLENGTH && j == Chunk.ZLENGTH) continue;
                surfacePos = worldPos.add(new Coord3(i, 0, j));
                surfacePos.y = getSurfaceHeight(surfacePos.x, surfacePos.z);

                if (i < Chunk.XLENGTH && j > 0) {
                    climbUpCliff(surfacePos, surfacePos.y, Direction.ZNEG, _terrainDataProvider, touchedChunkCoords);
                }
                if (j < Chunk.ZLENGTH && i > 0) {
                    climbUpCliff(surfacePos, surfacePos.y, Direction.XNEG, _terrainDataProvider, touchedChunkCoords);
                }
            }
        }
        // TODO: add chunk coord to needs flood fill--here?
    }

    /*
     * Sunlight/Surface
     */
    public boolean isAboveSurface(Coord3 global) { return getSurfaceHeight(global) < global.y;  }
    public int getSurfaceHeight(Coord3 global) { return getSurfaceHeight(global.x,global.z); }
    public int getSurfaceHeight(int x, int z) { return sunLightmap.GetSunHeight(x,z) - 1; }
    public void setSurfaceHeight(Coord3 global) {
        sunLightmap.SetSunHeight(global.y + 1, global.x, global.z);
    }
    public void updateSurface(Coord3 global) {
        if ((global.y > getSurfaceHeight(global))) {
            setSurfaceHeight(global);
        }
    }

    private void climbUpCliff(Coord3 global, int height, int direction, TerrainDataProvider _terrainDataProvider, HashSet<Coord3> touchedChunkCoords) {
        Coord3 neighbor = global.add(Direction.DirectionCoords[direction]);
        int neighborHeight = getSurfaceHeight(neighbor);
        if (height >= neighborHeight) {
            return;
        }

        for(neighbor.y = height + 1; neighbor.y <= neighborHeight; ++neighbor.y ) {
            Coord3 chunkCoord = Chunk.ToChunkPosition(neighbor);
            Chunk chunk = lookupOrCreateChunkAtPosition(chunkCoord);

            int was = chunk.blockAt(Chunk.ToChunkLocalCoord(neighbor));
            int is = was;
            if (was == NON_EXISTENT.ordinal()) {
                is = lookupOrCreateBlock(neighbor, _terrainDataProvider);
            }

            if (IsTranslucent(is)) {
                // TODO: deal with water
                chunk.chunkFloodFillSeedSet.addCoord(neighbor);
                //TACKING ON SUNLIGHT UPDATE
                sunLightmap.SetLight(SunLightComputer.OneStepDownFromMaxLight(AIR), neighbor);
            }
            if (IsSolid(is)) {
                touchedChunkCoords.add(chunkCoord);
                chunk.chunkBlockFaceMap.addFace(Chunk.ToChunkLocalCoord(neighbor),Direction.OppositeDirection(direction));
            }

        }
    }
    public void updateChunksToBeFlooded(HashSet<Coord3> chunkCoords) {
        for (Coord3 co : chunkCoords) {
            try {
                chunkCoordsToBeFlooded.put(co);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
	 * get the terrain data inside the chunk and also in the -1/+1 box (in block units) that
	 * surrounds it (within world limits if any)
	 */
    private boolean generateNoiseForChunkAt(int cx, int cy, int cz, TerrainDataProvider _dataProvider) {
        Chunk targetChunk = lookupOrCreateChunkAtPosition(new Coord3(cx, cy, cz));
        if (targetChunk == null) {
            if (ChunkCoordWithinWorldBounds(new Coord3(cx,cy,cz))) {
                Asserter.assertFalseAndDie("how is it possible?? null chunk with world bounds? " + new Coord3(cx,cy,cz).toString());
            }
            return false;
        }
        Coord3 worldPos = Chunk.ToWorldPosition(new Coord3(cx, cy, cz));

        Coord3 relPos, absPos, chunkPos = null;
        Chunk possibleChunk = null;
        boolean foundDirt = false;
        for (int i = -1; i < Chunk.CHUNKDIMS.x + 1; ++i) {
            for (int j = -1; j < Chunk.CHUNKDIMS.z + 1; ++j) {
                for (int k = -1; k < Chunk.CHUNKDIMS.y + 1; ++k) {
                    relPos = new Coord3(i, k, j);
                    absPos = worldPos.add(relPos);
                    Coord3 nextChunkPos = Chunk.ToChunkPosition(absPos);
                    if (chunkPos == null || !chunkPos.equal(nextChunkPos)) {
                        chunkPos = nextChunkPos.clone();
                        possibleChunk = lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(absPos));
                    }

                    if (VoxelLandscape.DO_USE_TEST_GEOMETRY) return false;

                    if (possibleChunk == null)
                        continue; // must be at world limit?
                    int block;

                    // FAKE WATER
                    boolean IsFakeWaterChunk = cx == 0 && cy == MAX_CHUNK_DIM_VERTICAL - 1 && cz == 0;
                    if (IsFakeWaterChunk && i == 3 && j == 2 && k == Chunk.YLENGTH - 1) {
                        block = WATER.ordinal();
                    } else if (IsFakeWaterChunk && k > Chunk.YLENGTH - 5 ) {
                        block = AIR.ordinal();
                    }
                    else
                        // #FAKE WATER
                        block = lookupOrCreateBlock(absPos, _dataProvider);

                    // should be grass?
					/*
					 * CONSIDER: this is too simplistic! grass should grow only
					 * where there's light... Also, put this logic somewhere
					 * where it will also apply to newly created, destroyed blocks
					 * may need a 'block time step' concept...
					 */
                    if (DIRT.ordinal() == block) {
                        Coord3 upOne = absPos.add(Coord3.ypos);
                        if (lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(upOne)) == null ||
                                IsTranslucent( lookupOrCreateBlock(upOne))) {
                            block = GRASS.ordinal();
                        }
                    } else if (WATER.ordinal() == block) {
                        liquidLevelMap.SetWaterLevel(WaterFlowComputer.MAX_WATER_LEVEL, absPos);
                        possibleChunk.getChunkBrain().SetLiquidDirty();
                    }

                    relPos = Chunk.ToChunkLocalCoord(relPos);
                    possibleChunk.setBlockAt(block, relPos);

                    if (!foundDirt && k > -1 && j > -1 && i > -1 && k <= Chunk.YLENGTH && j <= Chunk.ZLENGTH && i <= Chunk.XLENGTH) {
                        foundDirt = IsSolid(block);
                    }
                }
            }
        }
        targetChunk.setIsAllAir(!foundDirt);
        return true;
    }


	/*
	 * A block changed somewhere.
	 * Update the map and check if anything else needs updating
	 * Credit: Mr. Wishmaster methods (YouTube)
	 */
	public void SetBlockAndRecompute(int block, Coord3 global) {
        if (!isGlobalCoordWithinWorldBounds(global)) return;

		Coord3 chunkPos = Chunk.ToChunkPosition(global);
		Coord3 localPos = Chunk.ToChunkLocalCoord(global);

        Chunk chunk = getChunk(chunkPos);
        int wasType = chunk.blockAt(localPos);
        if (wasType == block) return;

		setBlockAtWorldCoord(block, global);

        if (IsTranslucent(block)) {
            chunk.chunkBlockFaceMap.removeAllFacesUpdateNeighbors(global, this);
        }
        if (block != AIR.ordinal()){
            if (IsSolid(block)) {
                chunk.chunkBlockFaceMap.addExposedFacesUpdateNeighbors(global, this);
            }
            //TODO: add faces for the new block. remove occluded faces.
            else {} // TODO: if liquid or glass...
        }
        //need to flood fill?
        if (!IsTranslucent(wasType) && AIR.ordinal() == block) {
            chunk.setBlockAt(PLACEHOLDER_AIR.ordinal(), localPos); // must 'fool' flood fill
            chunk.chunkFloodFillSeedSet.addCoord(global);
            chunkCoordsToBePriorityFlooded.add(chunkPos);
        }

		SetDirty(chunkPos);

		if (localPos.x == 0)
			SetDirty(chunkPos.minus(Coord3.right));
		if (localPos.y == 0)
			SetDirty(chunkPos.minus(Coord3.up));
		if (localPos.z == 0)
			SetDirty(chunkPos.minus(Coord3.forward));

		if (localPos.x == Chunk.CHUNKDIMS.x - 1)
			SetDirty(chunkPos.add(Coord3.right));
		if (localPos.y == Chunk.CHUNKDIMS.y - 1)
			SetDirty(chunkPos.add(Coord3.up));
		if (localPos.z == Chunk.CHUNKDIMS.z - 1)
			SetDirty(chunkPos.add(Coord3.forward));

		SunLightComputer.RecomputeLightAtPosition(this, global.clone());
		LightComputer.RecomputeLightAtPosition(this, global.clone());

        //TODO: make logic 'more abstract' so TerrainMap doesn't have to deal with the following...
        if (WATER.ordinal() == block) {
            liquidUpdaterWater.addCoord(global.clone(), WaterFlowComputer.MAX_WATER_LEVEL);
        } else {
            WaterFlowComputer.RecomputeWaterAtPosition(this, global.clone());
        }
	}

	private void SetDirty(Coord3 chunkPos) {
		Chunk chunk = getChunk(chunkPos);
		if (chunk != null) {
            chunk.getChunkBrain().SetDirty();
        }
	}

	public int GetMaxY(int x, int z, boolean forceLookup) {
        if (!forceLookup) {
            int height = sunLightmap.GetSunHeight(x, z);
            if (height > 0) {
                return height;
            }
        }
		Coord3 chunkPos = Chunk.ToChunkPosition(x, 0, z);
		chunkPos.y = MAX_CHUNK_DIM_VERTICAL; // chunks.GetMax().y;
		Coord3 localPos = Chunk.ToChunkLocalCoord(x, 0, z);
		for (; chunkPos.y >= 0; chunkPos.y--) {
			localPos.y = Chunk.CHUNKDIMS.y - 1;
			for (; localPos.y >= 0; localPos.y--) {
				Chunk chunk = chunks.SafeGet(chunkPos);
				if (chunk == null)
					break;
				int block = chunk.blockAt(localPos);
				if (!IsAirOrNonExistent(block)) {
					return Chunk.ToWorldPosition(chunkPos, localPos).y;
				}
			}
		}
		return 0;
	}
	

	/*
	 * Chunk info
	 */
	public Chunk lookupOrCreateChunkAtPosition(int x, int y, int z) {
		return lookupOrCreateChunkAtPosition(new Coord3(x, y, z));
	}
	public Chunk lookupOrCreateChunkAtPosition(Coord3 chunkPos) {
        if (!ChunkCoordWithinWorldBounds(chunkPos)) return null;
        // Sets chunk at key if not there before. Returns previous value! (null if nothing was there)
        Chunk result = chunks.putIfKeyIsAbsent(chunkPos, new Chunk(chunkPos, this));
        lock.lock();
        try {
        	// new chunk?
	        if (result == null) {
	            result = chunks.Get(chunkPos); // re-get the Chunk if it was null. now guaranteed to be there.
	            if (VoxelLandscape.READ_CHUNKS_FROM_FILE) result.readFromFile(); // try to load the chunk from file
	        }
        } finally {
        	lock.unlock();
        }
        return result;
	}

	public Chunk getChunk(Coord3 chunkPos) {
		return chunks.Get(chunkPos);
	}
    public Chunk GetChunk(int x, int y, int z) {
        return getChunk(new Coord3(x,y,z));
    }

	public SunLightMap GetSunLightmap() {
		return sunLightmap;
	}

	public LightMap GetLightmap() {
		return lightmap;
	}

    public WaterLevelMap getLiquidLevelMap() {
        return liquidLevelMap;
    }

    public void setWater(Coord3 worldPos) {
        setBlockAtWorldCoord(WATER.ordinal(), worldPos);
    }

    public void setWaterRunOff(Coord3 worldPos) {
        setBlockAtWorldCoord(WATER_RUNOFF.ordinal(), worldPos);
    }

    public void unsetWater(Coord3 worldPos) {
        if (IsWaterType(lookupBlock(worldPos)))
            setBlockAtWorldCoord(AIR.ordinal(), worldPos);
    }

    /*
     * Block debug info
     */
    public String getBlockInfoString(Coord3 global) {
        Coord3 chunkCo = Chunk.ToChunkPosition(global);
        Coord3 local = Chunk.ToChunkLocalCoord(global);
        StringBuilder sb = new StringBuilder();
        sb.append("Block Info for: \n");
        sb.append(global.toString());
        sb.append("\n");
        Chunk ch = getChunk(chunkCo);
        if (ch != null) {
            if (ch.chunkBlockFaceMap != null) {

                sb.append(ch.chunkBlockFaceMap.infoFor(local));
            } else {
                sb.append("null chunk block face");
            }
        } else {
            sb.append("null chunk");
        }
        sb.append("\n");
        sb.append(chunkCo.toString());
        sb.append("\n");
        sb.append(local.toString());
        sb.append("\n");
        return sb.toString();
    }
    public String getChunkInfo(Coord3 chunkCoord) {
    	Chunk chunk = getChunk(chunkCoord);
    	if (chunk == null) return "null chunk at: " + chunkCoord.toString();
    	StringBuilder sb = new StringBuilder();
    	sb.append("Chunk Info: ");
    	sb.append(chunkCoord.toString());
    	sb.append(String.format("\nWrite dirty: %b", chunk.isWriteDirty()));
    	sb.append(String.format("\nHiding: %b", chunk.getChunkBrain().isHiding()));
    	sb.append(String.format("\nHas Ever started meshing: %b", chunk.getHasEverStartedMeshing()));
    	return sb.toString();
    }

    /*
     * Look up any light
     */
    public int lightAt(Coord3 global) {
        return Math.max(sunLightmap.GetLight(global), lightmap.GetLight(global));
    }


    
}
