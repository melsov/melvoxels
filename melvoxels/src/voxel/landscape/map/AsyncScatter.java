package voxel.landscape.map;

import voxel.landscape.collection.ColumnMap;
import voxel.landscape.coord.Coord2;
import voxel.landscape.map.light.ChunkSunLightComputer;
import voxel.landscape.map.water.ChunkWaterLevelComputer;
import voxel.landscape.player.B;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

/**
 * Created by didyouloseyourdog on 11/20/14.
 */
public class AsyncScatter implements Runnable {
    private BlockingQueue<Coord2> columnsToBeScattered;
    AtomicBoolean keepGoing;
    Coord2 colCoord;
    TerrainMap terrainMap;
    ColumnMap columnMap;
    public AsyncScatter(final TerrainMap _terrainMap, final ColumnMap _columnMap, BlockingQueue<Coord2> _columnsToBeScattered, AtomicBoolean _keepGoing) {
        columnsToBeScattered = _columnsToBeScattered;
        keepGoing = _keepGoing;
        terrainMap = _terrainMap;
        columnMap = _columnMap;
    }
    @Override
    public void run() {
        while(keepGoing.get()) {
            try {
                colCoord = columnsToBeScattered.take(); //thread will block while nothing is available...maybe forever...
                if (colCoord.equals(Coord2.SPECIAL_FLAG)) {
                    B.bug("time to quit: " + Thread.currentThread().getName());
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (columnMap.SetIsBuildingOrReturnFalseIfStartedAlready(colCoord.getX(), colCoord.getZ())) {
                ChunkSunLightComputer.Scatter(terrainMap, columnMap, colCoord.getX(), colCoord.getZ()); //WANT
                ChunkWaterLevelComputer.Scatter(terrainMap, columnMap, colCoord.getX(), colCoord.getZ()); //WANT
                columnMap.SetBuilt(colCoord.getX(), colCoord.getZ());

                try { sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }

}
