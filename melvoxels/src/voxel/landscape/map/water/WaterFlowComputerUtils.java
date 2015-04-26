package voxel.landscape.map.water;

import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.chunkbuild.ChunkBrain;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;

/**
 * Created by didyouloseyourdog on 8/12/14.
 */
public class WaterFlowComputerUtils {
    public static final int WATER_STEP = 1;
    /*
    Unlike with light, water doesn't affect neighbor blocks (unless it spreads to those blocks)
    So don't set WaterDirty for water blocks on the edge of chunks
     */
    public static void SetWaterDirty(TerrainMap map, Coord3 pos) {
        Coord3 chunkPos = Chunk.ToChunkPosition(pos);
        SetChunkWaterDirty(map, chunkPos);
    }

    private static void SetChunkWaterDirty(TerrainMap map, Coord3 chunkPos) {
        Chunk chunk = map.GetChunk(chunkPos);
        if(chunk == null) return;
        ChunkBrain chunkBrain = chunk.getChunkBrainPassively();
        if(chunkBrain == null) return;
        chunkBrain.SetLiquidDirty();
    }

    public static int GetWaterStep(int block) {
        if(BlockType.AcceptsWater(block)) {
            return WATER_STEP;
        } else {
            return WaterFlowComputer.MAX_WATER_LEVEL; //blocks water (water is decremented all the way)
        }
    }
}
