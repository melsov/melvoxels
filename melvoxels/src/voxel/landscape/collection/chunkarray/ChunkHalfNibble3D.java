package voxel.landscape.collection.chunkarray;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 8/9/14.
 */
public class ChunkHalfNibble3D extends ChunkPrimitiveType3D
{
    /*
    Represents a 16 x 16 x 16 cube-grid of "1/4 bytes" (half nibbles).
    Backed by a 16 x 16 x 4 cube-grid of bytes.
     */
    private byte[] chunk = new byte[(Chunk.XLENGTH * Chunk.YLENGTH * Chunk.ZLENGTH) / 4];

    @Override
    public void Set(int val, Coord3 pos) {
        Set(val, pos.x, pos.y, pos.z);
    }
    @Override
    public void Set(int val, int x, int y, int z) {
        int lookup = y << 6 | z << 2 | ((x >> 2) & 3); //this is just multiplication and addition
        if ((x & 3) == 0) {
            chunk[lookup] = (byte)(chunk[lookup] & 0xfc | val & 3);
        } else if ((x & 3) == 1) {
            chunk[lookup] = (byte)(chunk[lookup] & 0xf0 | (val & 3) << 2 | chunk[lookup] & 3);
        } else if ((x & 3) == 2) {
            chunk[lookup] = (byte)(chunk[lookup] & 192 | (val & 3) << 4 | chunk[lookup] & 0xf);
        } else {
            chunk[lookup] = (byte)((val & 3) << 6 | chunk[lookup] & 63 );
        }
    }
    @Override
    public int Get(Coord3 pos) {
        return Get(pos.x, pos.y, pos.z);
    }
    @Override
    public int Get(int x, int y, int z) {
        int lookup = y << 6 | z << 2 | ((x >> 2) & 3); //this is just multiplication and addition
        if ((x & 3) == 0) {
            return chunk[lookup] & 3;
        } else if ((x & 3) == 1) {
            return chunk[lookup] >> 2 & 3;
        } else if ((x & 3) == 2) {
            return chunk[lookup] >> 4 & 3;
        } else {
            return chunk[lookup] >> 6 & 3;
        }
    }
}
