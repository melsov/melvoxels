package voxel.landscape;

import com.jme3.asset.AssetManager;

import static voxel.landscape.player.B.bug;

import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import voxel.landscape.chunkbuild.AsyncGenerateColumnDataInfinite;
import voxel.landscape.chunkbuild.ChunkCoordCamComparator;
import voxel.landscape.chunkbuild.MaterialLibrarian;
import voxel.landscape.chunkbuild.blockfacefind.FloodFillBoss;
import voxel.landscape.chunkbuild.bounds.XZBounds;
import voxel.landscape.chunkbuild.meshbuildasync.AsyncMeshBuilder;
import voxel.landscape.chunkbuild.meshbuildasync.ChunkMeshBuildingSet;
import voxel.landscape.chunkbuild.unload.ChunkUnloader;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.collection.coordmap.managepages.FurthestChunkFinder;
import voxel.landscape.coord.Box2;
import voxel.landscape.coord.ColumnRange;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.debug.DebugGeometry;
import voxel.landscape.map.AsyncScatter;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.player.B;
import voxel.landscape.settings.BuildSettings;
import voxel.landscape.util.Asserter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 10/16/14. manages world generation related
 * thread pools
 */
public class WorldGenerator {

	private Camera camera;

	private BlockingQueue<Coord2> columnsToBeBuilt = new LinkedBlockingQueue<Coord2>(400);
	private BlockingQueue<Coord2> columnsToBeScattered;
	private BlockingQueue<ChunkMeshBuildingSet> chunksToBeMeshed;
	private BlockingQueue<ChunkMeshBuildingSet> completedChunkMeshSets;

	// Chunk disposal
	private BlockingQueue<Coord3> unloadChunks = new LinkedBlockingQueue<>(540);
	private BlockingQueue<Coord3> deletableChunks = new LinkedBlockingQueue<>(540);
	ExecutorService chunkUnloadService;

	private static final int COLUMN_DATA_BUILDER_THREAD_COUNT = 1;
	private static final int CHUNK_MESH_BUILD_THREAD_COUNT = 1;
	private static final int CHUNK_UNLOAD_THREAD_COUNT = 1;

	private ExecutorService colDataPool;
	private ExecutorService chunkMeshBuildPool;
	private ExecutorService scatterBuildPool;

	private AtomicBoolean columnBuildingThreadsShouldKeepGoing = new AtomicBoolean(true);

	private FurthestChunkFinder furthestChunkFinder = new FurthestChunkFinder();

	private Node worldNode;
	private TerrainMap map;
	public final ColumnMap columnMap;
	public final MaterialLibrarian materialLibrarian;

	public final FloodFillBoss floodFillBoss;
	public final FloodFillBoss shortOrderFloodFillBoss;

	public static final String BGFloodFillThreadName = "Background Flood-Fill Thread";
	public static final String ShortOrderFloodFillThreadName = "Short-order Flood-Fill Thread";

	public static final boolean TEST_DONT_BUILD = false;
	public static final boolean TEST_DONT_RENDER = false;

	public WorldGenerator(Node _worldNode, Camera _camera, TerrainMap _map, final ColumnMap _columnMap,
			AssetManager _assetManager) {
		worldNode = _worldNode;
		camera = _camera;
		map = _map;
		columnMap = _columnMap;
		floodFillBoss = new FloodFillBoss(map, map.chunkCoordsToBeFlooded, camera, null, BGFloodFillThreadName);
		shortOrderFloodFillBoss = new FloodFillBoss(map, map.chunkCoordsToBePriorityFlooded, camera, null,
				ShortOrderFloodFillThreadName);
		materialLibrarian = new MaterialLibrarian(_assetManager);

		initThreadPools();
	}

	private void initThreadPools() {
		initColumnDataThreadExecutorService();
		// initLightAndWaterScatterService();
		floodFillBoss.start();
		shortOrderFloodFillBoss.start();
		initChunkMeshBuildThreadExecutorService();
		initUnloadService();
	}

	public void update(float tpf) {
		addColumns();
		if (!VoxelLandscape.DONT_BUILD_CHUNK_MESHES) {
			buildChunks();
		}
		renderChunks();
		cull();
	}

	private void initUnloadService() {
		chunkUnloadService = Executors.newFixedThreadPool(CHUNK_UNLOAD_THREAD_COUNT);
		for (int i = 0; i < CHUNK_UNLOAD_THREAD_COUNT; ++i) {
			ChunkUnloader chunkUnloader = new ChunkUnloader(map, columnMap, unloadChunks, deletableChunks);
			chunkUnloadService.execute(chunkUnloader);
		}
	}

	private void initColumnDataThreadExecutorService() {
		colDataPool = Executors.newFixedThreadPool(COLUMN_DATA_BUILDER_THREAD_COUNT);
		for (int i = 0; i < COLUMN_DATA_BUILDER_THREAD_COUNT; ++i) {
			AsyncGenerateColumnDataInfinite infinColDataThread = new AsyncGenerateColumnDataInfinite(map, columnMap,
					columnsToBeBuilt, columnBuildingThreadsShouldKeepGoing);
			colDataPool.execute(infinColDataThread);
		}
	}

	private void initChunkMeshBuildThreadExecutorService() {
		ChunkCoordCamComparator chunkCoordCamComparator = new ChunkCoordCamComparator(camera);
		chunksToBeMeshed = new PriorityBlockingQueue<>(50, chunkCoordCamComparator);
		completedChunkMeshSets = new LinkedBlockingQueue<>(50);
		chunkMeshBuildPool = Executors.newFixedThreadPool(CHUNK_MESH_BUILD_THREAD_COUNT);
		for (int i = 0; i < CHUNK_MESH_BUILD_THREAD_COUNT; ++i) {
			AsyncMeshBuilder asyncMeshBuilder = new AsyncMeshBuilder(map, chunksToBeMeshed, completedChunkMeshSets);
			chunkMeshBuildPool.execute(asyncMeshBuilder);
		}
	}

	// TODO: YA ASYNC CLASS/SERVICE: SCATTER LIGHT AND WATER... CONSUMES FROM
	// FLOODFILLDCHUNKCOORDS. PRODUCES SCATTEREDCHUNKS
	// TODO: CHUNK BUILD TIMER TEST CLASS
	// OR>>> ADD AFTER? (AND THEN DO A LIGHT UPDATE????)
	private void initLightAndWaterScatterService() {
		columnsToBeScattered = new LinkedBlockingQueue<Coord2>(50);
		scatterBuildPool = Executors.newFixedThreadPool(COLUMN_DATA_BUILDER_THREAD_COUNT);
		for (int i = 0; i < COLUMN_DATA_BUILDER_THREAD_COUNT; ++i) {
			AsyncScatter asyncScatter = new AsyncScatter(map, columnMap, columnsToBeScattered,
					columnBuildingThreadsShouldKeepGoing);
			scatterBuildPool.execute(asyncScatter);
		}
	}

	// TODO: new remove and add policy: do complete add/write-purge in every
	// frame
	/*
	 * Update
	 */
	private void addColumns() {
		if (columnsToBeBuilt == null)
			return;
		// TODO: figure why using prepareColumnsBox() creates problems
		// TODO: consider in some BlockingQueues etc. put (like
		// ChunksToBeMeshed, FloodFilledChunks) put Chunks, not just their
		// Coords
		// would ensure that Chunks that wanted to be in memory still, still
		// were in memory?
		for (Coord2 column : BuildSettings.prepareColumnsBox(camera.getLocation())) {
			Coord3 emptyCol = new Coord3(column.getX(), 0, column.getZ());
			removeFromUnloadLists(new Coord2(emptyCol.x, emptyCol.z));
			/* column not yet seen? submit column for build */
			if (!columnsToBeBuilt.contains(column) && !columnMap.IsBuiltOrIsBuilding(emptyCol.x, emptyCol.z)) {
				columnsToBeBuilt.add(new Coord2(emptyCol.x, emptyCol.z));
			} else {
				/*
				 * TODO: CONSIDER: this logic may be bad. since we iterate over the same coords a lot
				 * its easy to buildThisChunk and do other things to not yet processed chunks
				 * MAYBE: use a set to keep track of any chunk coords that are known to be ready?
				 * Or figure out why this would be redundant (such a set) and use the 'set' you already have.
				 */
				if (BuildSettings.ChunkCoordWithinAddArea(camera.getLocation(), emptyCol)) {
					for (Coord3 coord3 : new ColumnRange(emptyCol)) {
						Chunk chunk = map.getChunk(coord3);
						if (chunk == null) continue;
						if (chunk.canUnhide()) {
							DebugGeometry.AddChunk(chunk.position, ColorRGBA.Magenta);
							attachMeshToScene(chunk);
						}
						else {
							if (!chunk.hasNoBlocks()) {
								if (chunk.blockFaceMapReady()) {
									//IT CAN AND DOES HAPPEN THAT CHUNKS GET OUT OF ADD AREA BY THE TIME THE ARE IN 'RENDER CHUNKS'
									//This is as it should be, and yet: (1) if chunks were only slated for unload if outside of prepare area
									//we'd avoid some re-processing 'naturally'. (2) Moreover, we need a way of (actually) knowing a chunk's
									//build status. (2.5) Give chunks a "standby chunkMeshBuildingSet" which will be nulled if any block changes
									// and when the chunkMeshBuildingSet is applied? (seems dicey? inviting concurrency bugs?)
									if (chunk.meshShouldUpdateOrHasAlready()) {
										meshThisChunk(chunk);
									}
//									if (!chunk.testBool) {
//										chunk.testBool = true;
//										meshThisChunk(chunk);
//									}
//									if (!chunk.getHasEverStartedMeshing()){										
//										buildThisChunk(chunk);
//									}
									
								}
//								if(!chunk.getChunkBrain().hasMeshData()) {
//									if (!chunk.getHasEverStartedMeshing()){
//										DebugGeometry.AddChunk(chunk.position, ColorRGBA.Blue);										
//										chunk.setHasEverStartedMeshingTrue();
//									}
//								}

							}
						}
					}
				}
			}
		}
		Asserter.assertTrue(columnsToBeBuilt.size() < 300, "lots of cols to be built?");
	}

	private void removeFromUnloadLists(Coord2 column) {
		for (Coord3 c : new ColumnRange(column)) {
			// deletableChunks.remove(c);
			// unloadChunks.remove(c);
		}
	}

	//DBUG
	Set<Coord3> testDupesSet = new HashSet<>(50);
	private void addTestDupes(Coord3 co) {
		if (testDupesSet.contains(co)) {
			DebugGeometry.AddChunk(co, ColorRGBA.Orange);
		}
		testDupesSet.add(co);
	}
	
	private void buildChunks() {
		for (int i = 0; i < 20; ++i) {
			Coord3 chunkCoord = shortOrderFloodFillBoss.floodFilledChunkCoords.poll();
			if (chunkCoord == null) {
				// TODO: test whether this gets duplicate chunk coords sometimes... 
				// if it does...consequences...
				chunkCoord = floodFillBoss.floodFilledChunkCoords.poll();
			} if (chunkCoord == null) {
				return;
			}
			if (map.getChunk(chunkCoord) == null) {
				DebugGeometry.AddAddChunk(chunkCoord);
				return;
			}
			Asserter.assertTrue(map.getChunk(chunkCoord) != null,
					"chunk not in map! at chunk coord: " + chunkCoord.toString());
			meshThisChunk(map.getChunk(chunkCoord));
		}
	}

	/* what this really does:
	 * makes and attaches an empty mesh for the chunk
	 * makes a chunkMeshBuildingSet for the chunk
	 * and adds that chunkMeshBuildingSet to 'chunksToBeMeshed'
	 * CONSIDER: simplify this process? (last step is the only essential part at this point) */
	private void meshThisChunk(Chunk chunk) {
//		if (chunk.getHasEverStartedMeshing()) {
//			DebugGeometry.AddSolidChunk(chunk.position, ColorRGBA.Red);
//		}
		chunk.setHasEverStartedMeshingTrue();
		if (!chunk.getIsAllAir()) {
			chunk.getChunkBrain().SetDirty();
			chunk.getChunkBrain().wakeUp();
			attachMeshToScene(chunk); // NOTE: attaching empty mesh, no mesh geometry yet
//			addTestDupes(chunk.position);
		} else {
			chunk.getChunkBrain().setMeshEmpty();
		}
	}

	private void attachMeshToScene(Chunk chunk) {
		chunk.getChunkBrain().attachTerrainMaterial(materialLibrarian.getBlockMaterial());
		chunk.getChunkBrain().attachWaterMaterial(materialLibrarian.getBlockMaterialTranslucentAnimated());
		chunk.getChunkBrain().attachToTerrainNode(worldNode);
	}

	public void enqueueChunkMeshSets(ChunkMeshBuildingSet chunkMeshBuildingSet) {
		chunksToBeMeshed.add(chunkMeshBuildingSet);
	}

	private void renderChunks() {
		for (int count = 0; count < 5; ++count) {
			ChunkMeshBuildingSet chunkMeshBuildingSet = completedChunkMeshSets.poll();
			if (chunkMeshBuildingSet == null) {
				//bug("*"); //HAPPENS A LOT. WHY?
				continue;
			}
			// write to file and don't mesh chunk?
			Chunk chunk = map.getChunk(chunkMeshBuildingSet.chunkPosition);
			if (chunk == null) {
				B.bugln("null chunk: render chunks");
				DebugGeometry.AddChunk(chunkMeshBuildingSet.chunkPosition, ColorRGBA.Magenta);
				continue;
			}
			if (!BuildSettings.ChunkCoordWithinAddArea(camera.getLocation(), chunkMeshBuildingSet.chunkPosition)) {
				if (chunk.hasNoBlocks()) {
					DebugGeometry.AddChunk(chunk.position, ColorRGBA.Gray);
				} else if (!chunk.getChunkBrain().hasMeshData()) {
					// THIS HAPPENS: TODO: HOW TO REACT? (AND HOW DOES IT HAPPEN: ASYNCHRONOUS UNINTENDEDNESS)
//					DebugGeometry.AddSolidChunk(chunk.position, ColorRGBA.Brown);
				} 
				chunk.buildLog("chunk not w/in add area");
				
				chunk.setStatusMeshNeedsUpdate();
				slateForUnload(map.getChunk(chunkMeshBuildingSet.chunkPosition));
				continue;
			}
			Asserter.assertTrue(chunk != null, "null chunk in renderChunks...");
			DebugGeometry.DeleteAddChunk(chunk.position);
			chunk.getChunkBrain().applyMeshBuildingSet(chunkMeshBuildingSet);
			
		}
	}

	// TODO: make and read chunks on the border of add area
	/*
	 * Remove columns
	 */
	private void cull() {
		if (!VoxelLandscape.CULLING_ON)
			return;
		unloadChunks();
		removeChunks();
	}

	private void unloadChunks() {
		if (!VoxelLandscape.CULLING_ON)
			return;
		for (Coord3 chunkCoord : furthestChunkFinder.outsideOfAddRangeChunks(map, camera, columnMap)) {
			if (!BuildSettings.ChunkCoordWithinAddArea(camera.getLocation(), chunkCoord)) {
				slateForUnload(map.getChunk(chunkCoord));
			}
		}
	}

	private void slateForUnload(Chunk chunk) {
		if (!VoxelLandscape.CULLING_ON)
			return;
		if (chunk == null) {
			return;
		}
		if (!columnMap.IsBuilt(chunk.position.x, chunk.position.z)) {
			// B.bugln("col not built: " + chunk.position.toString());
		}
		if (!chunk.isWriteDirty()) {
			// DebugGeometry.AddAddChunk(chunk.position);
		}
		if (chunk.isWriteDirty()) {
			if (!unloadChunks.contains(chunk.position) && !chunk.hasStartedWriting.get()) {
				if (unloadChunks.remainingCapacity() > 0) {
					chunk.hasStartedWriting.set(true);
//					chunk.getChunkBrain().detachNodeFromParent();
					unloadChunks.add(chunk.position);
				} else {
					B.bugln("unloadChunks capacity 0");
				}
			}
		} else {
			if (!deletableChunks.contains(chunk.position)) {
				if (deletableChunks.remainingCapacity() > 0) {
					deletableChunks.add(chunk.position);
				} else {
					B.bugln("deletable capacity 0");
				}
			}
		}
	}

	private void removeChunks() {
		if (!VoxelLandscape.CULLING_ON)
			return;
		while (deletableChunks.size() > 0) {
			removeChunk(deletableChunks.poll());
		}
	}

	private void removeChunk(Coord3 chunkCoord) {
		if (chunkCoord == null)
			return;
		if (BuildSettings.ChunkCoordWithinPrepareArea(camera.getLocation(), chunkCoord)) {
			return;
		}

		map.removeChunk(chunkCoord);
		map.removeColumn(chunkCoord);
	}

	/*
	 * Clean-up
	 */
	public void killThreadPools() {
		poisonThreads();

		columnBuildingThreadsShouldKeepGoing.set(false);
		if (colDataPool != null)
			colDataPool.shutdownNow();
		if (scatterBuildPool != null)
			scatterBuildPool.shutdownNow();

		// asyncChunkMeshThreadsShouldKeepGoing.set(false);
		chunkMeshBuildPool.shutdownNow();

		floodFillBoss.shutdown();
		shortOrderFloodFillBoss.shutdown();

		// columnRemovalShouldKeepGoing.set(false);
		if (chunkUnloadService != null) {
			chunkUnloadService.shutdownNow();
		}
	}

	private void poisonThreads() {
		columnsToBeBuilt.add(Coord2.SPECIAL_FLAG);
		map.chunkCoordsToBeFlooded.add(Coord3.SPECIAL_FLAG);
		map.chunkCoordsToBePriorityFlooded.add(Coord3.SPECIAL_FLAG);
		unloadChunks.add(Coord3.SPECIAL_FLAG);
		chunksToBeMeshed.add(ChunkMeshBuildingSet.POISON_PILL);
	}
	
	private Coord3 camChunkCoord = new Coord3(0);
	private void debugCameraLocation() {
		Coord3 nextCamChunkCoord = Chunk.ToChunkPosition(Coord3.FromVector3f(camera.getLocation()));
		if (!nextCamChunkCoord.equal(camChunkCoord)) {
			camChunkCoord = nextCamChunkCoord;
			bug(camChunkCoord);
		}
	}

}
