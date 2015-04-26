package voxel.landscape.chunkbuild;

import voxel.landscape.VoxelLandscape;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.structure.StructureBuilder;
import voxel.landscape.noise.TerrainDataProvider;
import voxel.landscape.player.B;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 8/4/14.
 */
public class AsyncGenerateColumnDataInfinite implements Runnable // extends ResponsiveRunnable
{

    private int x,z;
    private TerrainMap terrainMap;
    private ColumnMap columnMap;
    private TerrainDataProvider dataProvider = new TerrainDataProvider(VoxelLandscape.WorldSettings.seed);
    BlockingQueue<Coord2> columnsToBeBuilt;
    AtomicBoolean keepGoing;

    private HashSet<Coord3> touchedChunkCoords;

    private StructureBuilder structureBuilder = new StructureBuilder();
    private static int instanceCount = 0;

    public AsyncGenerateColumnDataInfinite(final TerrainMap _terrainMap, final ColumnMap _columnMap, final BlockingQueue<Coord2> _columnsToBeBuilt, AtomicBoolean _keepGoing) {
        columnsToBeBuilt = _columnsToBeBuilt;
        columnMap = _columnMap;
        terrainMap = _terrainMap;
        keepGoing = _keepGoing;
        touchedChunkCoords = new HashSet<>(terrainMap.getMaxChunkCoordY() - terrainMap.getMaxChunkCoordY());
    }
    @Override
    public void run() {
        Thread.currentThread().setName("Async Gen Column Data Thread-" + instanceCount++);
        while(keepGoing.get()) {
            Coord2 colCoord = null;
            try {
                colCoord = columnsToBeBuilt.take(); //thread will block while nothing is available...maybe forever...
                if (colCoord.equals(Coord2.SPECIAL_FLAG)) {
                    B.bugln("time to quit: " + Thread.currentThread().getName());
                    return;
                }
                x = colCoord.x; z = colCoord.y;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (columnMap.SetIsBuildingOrReturnFalseIfStartedAlready(x,z)) {
                touchedChunkCoords.clear();
                terrainMap.generateSurface(x, z, dataProvider, touchedChunkCoords);
                structureBuilder.addStructures(colCoord, terrainMap, dataProvider, touchedChunkCoords);

                //WE MAY HAVE CRAWLED BACK OVER AN ALREADY PROCESSED COLUMN
                //WE COULD BE ITERATING OVER ITS ENTIRE X,Z SURFACE WHEN (SAY) ONLY
                //ONE X,Z HEIGHT COORD WAS CHANGED. THIS SEEMS WASTEFUL BUT, TO GET AROUND
                //IT, WE'D NEED TO MAKE A PER-CHUNK 'DIRTY' HEIGHT COORD2 LIST: UGH?
                columnMap.SetBuiltSurface(x, z);
                removeSurfaceNotBuilt(touchedChunkCoords);
                for (Coord2 col2 : columnsFromChunkCoords(touchedChunkCoords)) {
                    terrainMap.populateFloodFillSeedsUpdateFaceMapsInChunkColumn(col2.getX(), col2.getZ(), dataProvider, touchedChunkCoords);
                }

                if (!keepGoing.get()) break; //PREVENT FREEZE AT END OF RUN??

//                ChunkSunLightComputer.Scatter(terrainMap, columnMap, x, z); //WANT
//                ChunkWaterLevelComputer.Scatter(terrainMap, columnMap, x, z); //WANT

                //try { sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
                /*
                 * TODO: ?? AWKWARD BUT: IF THERE WERE NO TOUCHED-CHUNK-COORDS AT ALL,
                 * ADD A 'DUMMY' CHUNK COORD, SO THAT FLOOD FILL 4D WILL BE PROMPTED
                 * TO PUT ANY CHUNK SLICES IN THE CHUNK COLUMN IN QUESTION INTO ITS 'IN BOUNDS BAG'
                 */

                terrainMap.updateChunksToBeFlooded(touchedChunkCoords);
            }
        }
    }

    private void removeSurfaceNotBuilt(HashSet<Coord3> chunkCoords) {
        Object[] chunkCos = chunkCoords.toArray();
        for (Object coord : chunkCos) {
            Coord3 coord3 = (Coord3) coord;
            if (!columnMap.HasBuiltSurface(coord3.x, coord3.z)) {
                chunkCoords.remove(coord3);
            }
        }
    }

    private HashSet<Coord2> columnsFromChunkCoords(HashSet<Coord3> chunkCoords) {
        HashSet<Coord2> columns = new HashSet<>(6);
        for (Coord3 co : chunkCoords) {
            columns.add(new Coord2(co.x, co.z));
        }
        return columns;
    }

    public int getX() { return x; }
    public int getZ() { return z; }

}
