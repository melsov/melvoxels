package voxel.landscape.collection.unused;

import voxel.landscape.Chunk;
import voxel.landscape.collection.chunkarray.ChunkPrimitiveType3D;
import voxel.landscape.coord.Coord3;

public class ChunkByte3D extends ChunkPrimitiveType3D
{
	private byte[] chunk = new byte[Chunk.XLENGTH*Chunk.YLENGTH*Chunk.ZLENGTH];
	
//	@Override
//	public void Set(int val, Coord3 pos) {
//		Set(val, pos.x, pos.y, pos.z);
//	}
//	@Override
//	public void Set(int val, int x, int y, int z) {
//		chunk[z][y][x] = (byte) val;
//	}
//	@Override
//	public int Get(Coord3 pos) {
//		return Get(pos.x, pos.y, pos.z);
//	}
//	@Override
//	public int Get(int x, int y, int z) {
//		return chunk[z][y][x];
//	}
    @Override
    public void Set(int val, Coord3 pos) {

    }
    @Override
    public void Set(int val, int x, int y, int z) {

    }
    @Override
    public int Get(Coord3 pos) {
        return Integer.MIN_VALUE;
    }
    @Override
    public int Get(int x, int y, int z) {
        return Integer.MIN_VALUE;
    }
}
