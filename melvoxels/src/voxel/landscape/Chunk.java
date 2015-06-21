package voxel.landscape;

import com.jme3.scene.Node;

import voxel.landscape.chunkbuild.ChunkBrain;
import voxel.landscape.chunkbuild.ChunkBuildStatus;
import voxel.landscape.chunkbuild.blockfacefind.ChunkLocalCoord;
import voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkseeds.ChunkFloodFillSeedSet;
import voxel.landscape.collection.LocalBlockMap;
import voxel.landscape.collection.chunkface.ChunkBlockFaceMap;
import voxel.landscape.collection.coordmap.managepages.ConcurrentHashMapCoord3D;
import voxel.landscape.coord.Axis;
import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.fileutil.FileUtil;
import voxel.landscape.map.TerrainMap;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static voxel.landscape.player.B.bug;

/**
 * Owns a mesh representing a XLEN by YLEN by ZLEN piece of a voxel landscape.
 * 
 */
public class Chunk {
	// TODO: make an exit method for chunks.
	// TODO: In chunk culling, include stages for 'go into background' and
	// 'totally leave memory (save to disk)'
	// TODO: would be good if: chunks saved on a separate thread and then
	// notified when done and then deleted themselves

	public Coord3 position;

	private LocalBlockMap blocks = new LocalBlockMap(new Coord3(XLENGTH,
			YLENGTH, ZLENGTH));

	// SYNCHRONIZING LOCKS FOR BLOCKS
	private ConcurrentHashMapCoord3D<Object> blockLocks = new ConcurrentHashMapCoord3D<Object>(
			Object.class);

	public static final int SIZE_X_BITS = 4;
	public static final int SIZE_Y_BITS = 4;
	public static final int SIZE_Z_BITS = 4;
	/*
	 * bitwise multiplication by a power of 2. literally we are sliding 1 to the
	 * left by SIZE_X_BITS. Or in other words, 1 becomes binary 10000 which is
	 * decimal 16
	 */
	public static final int XLENGTH = 1 << SIZE_X_BITS;
	public static final int YLENGTH = 1 << SIZE_Y_BITS;
	public static final int ZLENGTH = 1 << SIZE_Z_BITS;

	// TODO: unfortunately, purge this var. make XYZLENGTH public instead
	public static Coord3 CHUNKDIMS = new Coord3(XLENGTH, YLENGTH, ZLENGTH);
	private ChunkBrain chunkBrain;
	private TerrainMap terrainMap;

	public final ChunkBlockFaceMap chunkBlockFaceMap = new ChunkBlockFaceMap();
	public ChunkFloodFillSeedSet chunkFloodFillSeedSet;

	private ChunkBuildStatus chunkBuildStatus = ChunkBuildStatus.NOT_TOUCHED;

	public synchronized void setStatusMeshNeedsUpdate() {
		chunkBuildStatus = ChunkBuildStatus.MESH_NEEDS_UPDATE;
	}

	public synchronized void setMeshUpToDate() {
		chunkBuildStatus = ChunkBuildStatus.MESH_UPDATED;
	}

	public synchronized void setMeshWillUpdate() {
		chunkBuildStatus = ChunkBuildStatus.MESH_WILL_UPDATE;
	}

	public boolean meshNeedsUpdate() {
		return chunkBuildStatus.meshNeedsUpdate();
	}

	public synchronized boolean setMeshShouldUpdate() {
		if (chunkBuildStatus.meshNeedsUpdate()
				&& !chunkBuildStatus.meshWillUpdate()) {
			setMeshWillUpdate();
			return true;
		}
		return false;
	}

	public boolean meshWillUpdate() {
		return chunkBuildStatus.meshWillUpdate();
	}

	// CONSIDER: decide whether we can just use hasNoBlocks and get rid of
	// isAllAir
	private volatile boolean isAllAir = false;

	public void setIsAllAir(boolean _allAir) {
		isAllAir = _allAir;
	}

	public boolean getIsAllAir() {
		return isAllAir;
	}

	public boolean hasNoBlocks() {
		return blocks.isEmpty();
	}

	/*
	 * Flags
	 */
	private volatile boolean hasEverBuiltMesh = false;

	public void setHasEverStartedMeshingTrue() {
		hasEverBuiltMesh = true;
	}

	public boolean getHasEverStartedMeshing() {
		return hasEverBuiltMesh;
	}

	public volatile AtomicBoolean hasStartedWriting = new AtomicBoolean(false);
	public AtomicBoolean hasAddedStructures = new AtomicBoolean(false);

	private volatile boolean hasGenerated = false;

	public void setHasGeneratedTrue() {
		hasGenerated = true;
	}

	public boolean getHasGenerated() {
		return hasGenerated;
	}

	public boolean isWriteDirty() {
		return VoxelLandscape.WRITE_CHUNKS_TO_FILE
				&& (blocks.writeDirty.get() || chunkBlockFaceMap.writeDirty() || chunkFloodFillSeedSet.writeDirty.get());
	}

	private final Lock lock = new ReentrantLock(true);

	public boolean testBool = false;

	// public static boolean USE_TEST_GEOMETRY = false;

	public Chunk(Coord3 _coord, TerrainMap _terrainMap) {
		position = _coord;
		terrainMap = _terrainMap;
		chunkFloodFillSeedSet = new ChunkFloodFillSeedSet(position);
	}

	/*
	 * Read/Write
	 */
	public void readFromFile() {
		if (!VoxelLandscape.READ_CHUNKS_FROM_FILE) {
			return;
		}
		lock.lock();
		try {
			blocks.readFromFile(position);
			chunkBlockFaceMap.readFromFile(position);
			chunkFloodFillSeedSet.readFromFile(position);
			AtomicBoolean hasAddedStructureO = (AtomicBoolean) FileUtil
					.DeserializeChunkObject(position,
							FileUtil.HasAddedStructuresExtension);
			hasAddedStructures = hasAddedStructureO != null ? hasAddedStructureO
					: hasAddedStructures;
			buildLog("read from file");
		} finally {
			lock.unlock();
		}
	}

	public void writeToFile() {
		if (!VoxelLandscape.WRITE_CHUNKS_TO_FILE) {
			return;
		}
		lock.lock();
		try {
			if (blocks.writeDirty.get()) { // NOTE: duck-tape condition TODO:
											// consider serializing the whole
											// chunk instead?
				try {
					FileUtil.SerializeChunkObject(hasAddedStructures, position,
							FileUtil.HasAddedStructuresExtension);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (blocks.writeDirty.get()) {
				blocks.writeToFile(position);
			}
			if (chunkBlockFaceMap.writeDirty()) {
				chunkBlockFaceMap.writeToFile(position);
			}
			if (chunkFloodFillSeedSet.writeDirty.get()) {
				chunkFloodFillSeedSet.writeToFile(position);
			}
			buildLog("wrote to file");

		} finally {
			hasStartedWriting.set(false);
			lock.unlock();
		}
	}

	public Node getRootSpatial() {
		if (chunkBrain == null)
			return null;
		return chunkBrain.getRootSpatial();
	}

	public ChunkBrain getChunkBrain() {
		if (chunkBrain == null) {
			chunkBrain = new ChunkBrain(this);
		}
		return chunkBrain;
	}

	public ChunkBrain getChunkBrainPassively() {
		return chunkBrain;
	}

	public boolean canUnhide() {
		return getChunkBrain().isHiding() && getChunkBrain().hasMeshData()
				&& chunkBlockFaceMap.meshReady();
	}

	public boolean blockFaceMapReady() {
		return chunkBlockFaceMap.meshReady();
	}

	public TerrainMap getTerrainMap() {
		return terrainMap;
	}

	/*
	 * Coord3 mapping
	 */
	public static Coord3 ToChunkPosition(Coord3 point) {
		return ToChunkPosition(point.x, point.y, point.z);
	}

	public static Coord3 ToChunkPosition(int pointX, int pointY, int pointZ) {
		/*
		 * Bit-wise division: this is equivalent to pointX / (2 to the power of
		 * SIZE_X_BITS) in other words pointX divided by 16. (only works for
		 * powers of 2 divisors) This operation is much faster than the normal
		 * division operation ("/")
		 */
		int chunkX = pointX >> SIZE_X_BITS;
		int chunkY = pointY >> SIZE_Y_BITS;
		int chunkZ = pointZ >> SIZE_Z_BITS;
		return new Coord3(chunkX, chunkY, chunkZ);
	}

	public static int ToChunkLocalY(int worldCoordY) {
		return worldCoordY & (YLENGTH - 1);
	}

	public static int ToChunkPositionY(int worldCoordY) {
		return worldCoordY >> SIZE_Y_BITS;
	}

	public static Coord3 ToChunkLocalCoord(Coord3 woco) {
		return ToChunkLocalCoord(woco.x, woco.y, woco.z);
	}

	public static Coord3 ToChunkLocalCoord(int x, int y, int z) {
		/*
		 * Bitwise mod (%) by X/Y/ZLENGTH. but better. since this is much faster
		 * than '%' and as a bonus will always return positive numbers. the
		 * normal modulo operator ("%") will return negative for negative
		 * left-side numbers. (for example -17 % 16 becomes -1. <--bad. since
		 * all local coords are positive we, want -17 mod 16 to be 15.)
		 */
		int xlocal = x & (XLENGTH - 1);
		int ylocal = y & (YLENGTH - 1);
		int zlocal = z & (ZLENGTH - 1);
		return new Coord3(xlocal, ylocal, zlocal);
	}

	public static Coord3 ToWorldPosition(Coord3 chunkPosition) {
		return ToWorldPosition(chunkPosition, Coord3.Zero);
	}

	public static Coord3 ToWorldPosition(Coord2 columnPosition) {
		return ToWorldPosition(new Coord3(columnPosition.getX(), 0,
				columnPosition.getZ()), Coord3.Zero);
	}

	public static Coord3 ToWorldPosition(Coord3 chunkPosition,
			Coord3 localPosition) {
		/*
		 * Opposite of ToChunkPosition
		 */
		int worldX = (chunkPosition.x << SIZE_X_BITS) + localPosition.x;
		int worldY = (chunkPosition.y << SIZE_Y_BITS) + localPosition.y;
		int worldZ = (chunkPosition.z << SIZE_Z_BITS) + localPosition.z;
		return new Coord3(worldX, worldY, worldZ);
	}

	public static boolean IsOutsideFacing(ChunkLocalCoord local, int direction) {
		if (Direction.IsNegDir(direction)) {
			return Direction.ComponentForDirection(local.toCoord3(), direction) == 0;
		}
		return Direction.ComponentForDirection(local.toCoord3(), direction) == Direction
				.ComponentForDirection(Chunk.CHUNKDIMS, direction) - 1;
	}

	public static Box ChunkLocalBox = new Box(new Coord3(0), new Coord3(
			XLENGTH, YLENGTH, ZLENGTH));

	public static int ToWorldPositionY(int chunkPositionY, int relPositionY) {
		return chunkPositionY << SIZE_Y_BITS + relPositionY;
	}

	public int blockAt(Coord3 co) {
		return blockAt(co.x, co.y, co.z);
	}

	public int blockAt(int x, int y, int z) {
		lock.lock();
		int result;
		try {
			result = blocks.SafeGet(x, y, z);
		} finally {
			lock.unlock();
		}
		return result;
		// return blocks.SafeGet(x, y, z);
	}

	public void setBlockAt(int block, Coord3 co) {
		lock.lock();
		try {
			blocks.Set(block, co);
		} finally {
			lock.unlock();
		}
	}

	public void setBlockAt(int block, int x, int y, int z) {
		setBlockAt(block, new Coord3(x, z, y));
	}

	public Coord3 originInBlockCoords() {
		return Chunk.ToWorldPosition(position);
	}

	public Object blockLockInstanceAt(Coord3 localCo) {
		return blockLocks.GetInstance(localCo);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Chunk: ");
		sb.append(position.toString());
		sb.append(String.format("\nWrite dirty: %b", isWriteDirty()));
		sb.append(String.format("\nHiding: %b", getChunkBrain().isHiding()));
		sb.append(String.format("\nHas Ever started meshing: %b",
				getHasEverStartedMeshing()));
		sb.append("\nBUILD LOG:\n");
		sb.append(buildLog.toString());
		return sb.toString();
	}

	private final StringBuilder buildLog = new StringBuilder(50);

	public StringBuilder getBuildLog() {
		return buildLog;
	}

	public void buildLog(String s) {
		buildLog.append(String.format("%s \n", s));
	}

}
