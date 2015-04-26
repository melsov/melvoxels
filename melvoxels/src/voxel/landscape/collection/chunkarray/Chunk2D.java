package voxel.landscape.collection.chunkarray;

import voxel.landscape.Chunk;

import java.lang.reflect.Array;

public class Chunk2D<K> 
{
	private K[] chunk;
	private Class<K> type;
	
	@SuppressWarnings("unchecked")
	public Chunk2D(Class<K> _type) {
		type = _type;
		chunk = (K[]) Array.newInstance(type, Chunk.ZLENGTH * Chunk.XLENGTH);
	}
	public Chunk2D(Class<K> _type, boolean noInstantiation) {
		type = _type;
	}
	
	public void Set(K val, int x, int z) {
		chunk[z * Chunk.XLENGTH + x] = val;
	}
	
	public K Get(int x, int z) {
		return chunk[z * Chunk.XLENGTH + x];
	}
	
}