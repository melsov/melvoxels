package voxel.landscape.map.water;

import voxel.landscape.Chunk;
import voxel.landscape.collection.MapHalfNibble3D;
import voxel.landscape.collection.chunkarray.ChunkHalfNibble3D;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 8/8/14.
 */
public class WaterLevelMap
{
    // Follows pattern of Light map, more or less
    // Like LightMap's "SetMaxLight()"
    // If there's 'less' water at this voxel than the value we want to set, set it and return TRUE
    // else don't bother setting it and return FALSE.

    private MapHalfNibble3D waterFlowLevels = new MapHalfNibble3D();

    /*
     * Set a new water level at position if appropriate (if block is air or water)
     * if current water was less than waterVal, set water val. return true
     */
    public boolean SetWaterLevelIfPossible(byte waterVal, Coord3 c) { return SetWaterLevelIfPossible(waterVal, c.x, c.y, c.z); }

    public boolean SetWaterLevelIfPossible(byte waterVal, int x, int y, int z) {
        if (waterFlowLevels.Get(x, y, z) < waterVal) {
            waterFlowLevels.Set((byte) waterVal, x,y,z);
            return true;
        }
        return false;
    }

    /*
     * GET
     */
    public byte GetWaterLevel(Coord3 pos) {
        return GetWaterLevel(pos.x, pos.y, pos.z);
    }
    public byte GetWaterLevel(int x, int y, int z) {
        byte waterLevel = (byte) waterFlowLevels.Get(x, y, z);
        if(waterLevel < WaterFlowComputer.MIN_WATER_LEVEL) return WaterFlowComputer.MIN_WATER_LEVEL;
        return waterLevel;
    }
    public byte GetWaterLevel(Coord3 chunkPos, Coord3 localPos) {
        return GetWaterLevel(Chunk.ToWorldPosition(chunkPos).add(localPos));
    }

    /*
     * GET A WATER LEVEL CHUNK ARRAY 3D
     */
    public ChunkHalfNibble3D GetChunk(Coord3 chunkPos) {
        return waterFlowLevels.GetChunk(chunkPos);
    }


    /*
     * SET
     */
    public void SetWaterLevel(byte water, Coord3 pos) {
        SetWaterLevel(water, pos.x, pos.y, pos.z);
    }
    public void SetWaterLevel(byte water, int x, int y, int z) {
        waterFlowLevels.Set(water, x, y, z);
    }


    /*
     * CLEAN-UP
     */
    public void RemoveWaterLevelData(int x, int y, int z) {
        waterFlowLevels.RemoveChunk(x,y,z);
    }
    public void RemoveWaterLevelData(Coord3 pos) { waterFlowLevels.RemoveChunk(pos); }
}


