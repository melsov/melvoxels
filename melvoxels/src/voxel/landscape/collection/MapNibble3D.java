package voxel.landscape.collection;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.collection.chunkarray.ChunkNibble3D;
import voxel.landscape.collection.coordmap.HashMapCoord3D;

/**
 * Created by didyouloseyourdog on 7/28/14.
 */
public class MapNibble3D
{
//    private List3D<ChunkByte3D> chunks = new List3D<ChunkNibble3D>(ChunkNibble3D.class);
    private HashMapCoord3D<ChunkNibble3D> chunks = new HashMapCoord3D<ChunkNibble3D>(ChunkNibble3D.class);
    private byte defaultValue;

    public MapNibble3D() {
        this((byte) 0);
    }

    public MapNibble3D(byte defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void Set(byte val, Coord3 pos) {
        Set(val, pos.x, pos.y, pos.z);
    }
    public void Set(byte val, int x, int y, int z) {
        Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
        Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
        ChunkNibble3D chunk = GetChunkInstance(chunkPos);
        chunk.Set(val, localPos);
    }

    public int Get(Coord3 pos) {
        return Get(pos.x, pos.y, pos.z);
    }
    public int Get(int x, int y, int z) {
        Coord3 chunkPos = Chunk.ToChunkPosition(x, y, z);
        Coord3 localPos = Chunk.ToChunkLocalCoord(x, y, z);
        ChunkNibble3D chunk = GetChunk(chunkPos);
        if(chunk != null) return chunk.Get(localPos);
        return defaultValue;
    }

    public void RemoveChunk(int x, int y, int z) { RemoveChunk(new Coord3(x,y,z)); }
    public void RemoveChunk(Coord3 pos) {
        chunks.Remove(pos);
    }

    public ChunkNibble3D GetChunkInstance(Coord3 pos) {
        return chunks.GetInstance(pos);
    }
    public ChunkNibble3D GetChunkInstance(int x, int y, int z) {
        return chunks.GetInstance(x, y, z);
    }

    public ChunkNibble3D GetChunk(Coord3 pos) {
        return chunks.SafeGet(pos);
    }
    public ChunkNibble3D GetChunk(int x, int y, int z) {
        return chunks.SafeGet(x, y, z);
    }

}
