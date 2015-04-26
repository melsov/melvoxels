package voxel.landscape.collection;

import voxel.landscape.Chunk;
import voxel.landscape.collection.chunkarray.ChunkHalfNibble3D;
import voxel.landscape.collection.coordmap.HashMapCoord3D;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 8/9/14.
 */
public class MapHalfNibble3D
{
    private HashMapCoord3D<ChunkHalfNibble3D> chunks = new HashMapCoord3D<ChunkHalfNibble3D>(ChunkHalfNibble3D.class);
    private byte defaultValue;

    public MapHalfNibble3D() {
        this((byte) 0);
    }

    public MapHalfNibble3D(byte defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void Set(byte val, Coord3 pos) {
        Set(val, pos.x, pos.y, pos.z);
    }
    public void Set(byte val, int x, int y, int z) {
        Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
        Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
        ChunkHalfNibble3D chunk = GetChunkInstance(chunkPos);
        chunk.Set(val, localPos);
    }

    public int Get(Coord3 pos) {
        return Get(pos.x, pos.y, pos.z);
    }
    public int Get(int x, int y, int z) {
        Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
        Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
        ChunkHalfNibble3D chunk = GetChunk(chunkPos);
        if(chunk != null) return chunk.Get(localPos);
        return defaultValue;
    }

    public void RemoveChunk(int x, int y, int z) { RemoveChunk(new Coord3(x,y,z)); }
    public void RemoveChunk(Coord3 pos) {
        chunks.Remove(pos);
    }

    public ChunkHalfNibble3D GetChunkInstance(Coord3 pos) {
        return chunks.GetInstance(pos);
    }
    public ChunkHalfNibble3D GetChunkInstance(int x, int y, int z) {
        return chunks.GetInstance(x, y, z);
    }

    public ChunkHalfNibble3D GetChunk(Coord3 pos) {
        return chunks.Get(pos);
    }
    public ChunkHalfNibble3D GetChunk(int x, int y, int z) {
        return chunks.SafeGet(x, y, z);
    }
}
