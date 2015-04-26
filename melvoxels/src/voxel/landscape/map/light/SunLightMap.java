package voxel.landscape.map.light;

import voxel.landscape.Chunk;
import voxel.landscape.collection.MapNibble3D;
import voxel.landscape.collection.MapPrimitive2D;
import voxel.landscape.collection.chunkarray.ChunkNibble3D;
import voxel.landscape.collection.chunkarray.ChunkUByte2D;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.fileutil.FileUtil;
import voxel.landscape.util.Asserter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SunLightMap 
{	
	private MapPrimitive2D<ChunkUByte2D> rays = new MapPrimitive2D<ChunkUByte2D>((byte) 0, ChunkUByte2D.class);
    private MapNibble3D lights = new MapNibble3D((byte) 0);
	
	public void SetSunHeight(int height, int x, int z) {
		rays.Set(height, x, z);
	}
    public int GetSunHeight(Coord3 coord3) { return GetSunHeight(coord3.x, coord3.z); }
	public int GetSunHeight(int x, int z) {
		return rays.Get(x, z);
	}
	public boolean IsSunLight(int x, int y, int z) {
		return GetSunHeight(x, z) <= y;
	}
	private boolean IsSunLight(Coord3 chunkPos, Coord3 localPos, int worldY) {
		ChunkUByte2D chunkRayMap = (ChunkUByte2D) rays.GetChunk(chunkPos.x, chunkPos.z);
		return chunkRayMap != null && chunkRayMap.Get(localPos.x, localPos.z) <= worldY;
	}
	public boolean SetMaxLight(byte light, Coord3 pos) {
		return SetMaxLight(light, pos.x, pos.y, pos.z);
	}
	public boolean SetMaxLight(byte light, int x, int y, int z) {
		Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
		Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
		
		if( IsSunLight(chunkPos, localPos, y) ) return false;

        ChunkNibble3D chunkLightMap = lights.GetChunkInstance(chunkPos);
		if(chunkLightMap.Get(localPos) < light) {
			chunkLightMap.Set(light, localPos);
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
	public void SetLight(byte light, Coord3 chunkPos, Coord3 localPos) {
		lights.GetChunkInstance(chunkPos).Set(light, localPos);
	}
	/*
	 * Get
	 */
	public byte GetLight(Coord3 pos) {
		return GetLight(pos.x, pos.y, pos.z);
	}
	public byte GetLight(int x, int y, int z) {
		Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
		Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
		return GetLight(chunkPos, localPos, y);
	}
	public byte GetLight(Coord3 chunkPos, Coord3 localPos, int worldY) {
		if(IsSunLight(chunkPos, localPos, worldY)) return SunLightComputer.MAX_LIGHT;

        ChunkNibble3D chunkLightMap = lights.GetChunk(chunkPos);
		if(chunkLightMap != null) {
			byte light = (byte) chunkLightMap.Get(localPos);
			return (byte) Math.max(SunLightComputer.MIN_LIGHT, light);
		}
		return SunLightComputer.MIN_LIGHT;
	}
    /*
     * Remove
     */
    public void RemoveRays(int x, int z) {
        rays.RemoveChunk(x,z);
    }
    public void RemoveLightData(int x, int y, int z) {
        lights.RemoveChunk(x,y,z);
    }
    public void RemoveLightData(Coord3 pos) { lights.RemoveChunk(pos); }

    /*
     * Read/Write
     */
    public void readRaysFromFile(Coord2 c) {
        Path path = Paths.get(FileUtil.RaysFile(c));
        if (Files.exists(path)) {
            ChunkUByte2D chunkUByte2D = rays.GetChunkInstance(c.getX(), c.getZ());
            chunkUByte2D.read(path);
        }
    }

    public void writeRaysToFile(Coord2 c) {
        Path path = Paths.get(FileUtil.RaysFile(c));
        rays.GetChunkInstance(c.getX(), c.getZ()).write(path);
    }

    public boolean raysWriteDirty(Coord2 c) {
        ChunkUByte2D chunk = rays.GetChunk(c);
        return chunk == null || chunk.writeDirty.get();
    }

    public void readLightsFromFile(Coord3 c) {
        // TODO: consider making lights storage with a hashmap or something else that expands according to need
        // TODO: alternately: turn into hash map before saving? (TODO: test speeds of fileIO strategies)
        Asserter.assertFalseAndDie("don't read lights yet. ");
    }
    public void writeLightsToFile(Coord3 c) {
        Asserter.assertFalseAndDie("don't write lights yet. ");
    }

}
