package voxel.landscape.collection.unused;
import voxel.landscape.Chunk;
import voxel.landscape.collection.chunkarray.ChunkPrimitiveType2D;

public class ChunkByte2D extends ChunkPrimitiveType2D
{
	private byte[] chunk = new byte [Chunk.CHUNKDIMS.x*Chunk.CHUNKDIMS.z];
	
	@Override
	public void Set(int val, int x, int z) {
//        chunk[z*x] = (byte) val;
	}
	@Override
	public int Get(int x, int z) {
		return Integer.MIN_VALUE; // chunk[z][x];
	}
}