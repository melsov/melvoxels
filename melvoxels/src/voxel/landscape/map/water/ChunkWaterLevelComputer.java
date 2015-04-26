package voxel.landscape.map.water;

import voxel.landscape.Chunk;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.collection.chunkarray.ChunkHalfNibble3D;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.light.SunLightComputer;

import java.util.ArrayList;

/**
 * Created by didyouloseyourdog on 8/12/14.
 */
public class ChunkWaterLevelComputer
{
    public static void ComputeRays(TerrainMap map, int cx, int cz) {
        int x1 = cx* Chunk.CHUNKDIMS.x - 1; // SIZE_X-1;
        int z1 = cz*Chunk.CHUNKDIMS.z - 1; // SIZE_Z-1;

        int x2 = x1+Chunk.CHUNKDIMS.x + 2; //.SIZE_X+2;
        int z2 = z1+Chunk.CHUNKDIMS.z + 2; // SIZE_Z+2;

        for(int z=z1; z<z2; z++) {
            for(int x=x1; x<x2; x++) {
                SunLightComputer.ComputeRayAtPosition(map, x, z, false);
            }
        }
    }

    public static void Scatter(TerrainMap map, ColumnMap columnMap, int cx, int cz) {

        WaterLevelMap waterLevelMap = map.getLiquidLevelMap();
        ArrayList<Coord3> list = new ArrayList<Coord3>((int) (Chunk.XLENGTH*Chunk.ZLENGTH*Chunk.YLENGTH * .4));

        for(int cy=TerrainMap.MIN_CHUNK_COORD.y; cy<TerrainMap.GetWorldHeightInChunks(); cy++) {
            //CONSIDER: THIS MIGHT BE A LOT QUICKER IF WE KEPT WATER IN A HASHMAP COORD3
            Coord3 chunkCoord = new Coord3(cx, cy, cz);
            Coord3 chunkWorldPos = Chunk.ToWorldPosition(chunkCoord);
            ChunkHalfNibble3D waterLevelChunk = waterLevelMap.GetChunk(chunkCoord);

            if (waterLevelChunk == null) continue;

            for(int x = 0; x < Chunk.XLENGTH; ++x) {
                for(int y = 0; y < Chunk.YLENGTH; ++y) {
                    for (int z = 0; z < Chunk.ZLENGTH; ++z) {
                        Coord3 worldPos = chunkWorldPos.add(new Coord3(x,y,z));
                        int level = waterLevelChunk.Get(x,y,z);
                        if (level == WaterFlowComputer.MAX_WATER_LEVEL) {
                            list.add(worldPos);
                        }
                    }
                }
            }
        }
        WaterFlowComputer.Scatter(map, list);
    }


}
