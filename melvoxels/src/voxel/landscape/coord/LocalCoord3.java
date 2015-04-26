package voxel.landscape.coord;

import voxel.landscape.Chunk;

/**
 * Created by didyouloseyourdog on 10/9/14.
 */
/*
 * TODO: CONSIDER deleting this whole class and just using ChunkLocalCoord
 */
public class LocalCoord3 extends Coord3
{
    private static final long serialVersionUID = 999L;
//    public ChunkLocalCoord(int _x, int _y, int _z) {
//        super(_x, _y, _z);
//    }

    public LocalCoord3(Coord3 coord3){
        super(0);
        Coord3 local = Chunk.ToChunkLocalCoord(coord3);
        x = local.x; y = local.y; z =local.z;
    }

    @Override
    public int hashCode() {
        return ((z & 15) << 8 ) | ((y & 15) << 4) | (x & 15);
    }
}
