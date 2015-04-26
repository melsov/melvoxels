package voxel.landscape.map.light;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.collection.MapNibble3D;
import voxel.landscape.collection.chunkarray.ChunkNibble3D;

public class LightMap {

    private MapNibble3D lights = new MapNibble3D();

	public boolean SetMaxLight(byte light, Coord3 pos) {
		return SetMaxLight(light, pos.x, pos.y, pos.z);
	}
	public boolean SetMaxLight(byte light, int x, int y, int z) {
		Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
		Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
        ChunkNibble3D chunk = lights.GetChunkInstance(chunkPos);
		byte oldLight = (byte) chunk.Get(localPos);
		if(oldLight < light) {
			chunk.Set(light, localPos);
			return true;
		}
		return false;
	}
	/*
	 * Set
	 */
	public void SetLight(byte light, Coord3 pos) {
		SetLight(light, pos.x, pos.y, pos.z);
	}
	public void SetLight(byte light, int x, int y, int z) {
		lights.Set(light, x, y, z);
	}
	/*
	 * Get
	 */
	public byte GetLight(Coord3 pos) {
		return GetLight(pos.x, pos.y, pos.z);
	}
	public byte GetLight(int x, int y, int z) {
		byte light = (byte) lights.Get(x, y, z);
		if(light < LightComputer.MIN_LIGHT) return LightComputer.MIN_LIGHT;
		return light;
	}
	public byte GetLight(Coord3 chunkPos, Coord3 localPos) {
		byte light = (byte) lights.GetChunkInstance(chunkPos).Get(localPos);
		if(light < LightComputer.MIN_LIGHT) return LightComputer.MIN_LIGHT;
		return light;
	}

    /*
     * Remove / Clean-up
     */
    public void RemoveLightData(int x, int y, int z) {
        lights.RemoveChunk(x,y,z);
    }
    public void RemoveLightData(Coord3 pos) { lights.RemoveChunk(pos); }
	
}
