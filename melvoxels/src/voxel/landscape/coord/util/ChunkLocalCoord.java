package voxel.landscape.coord.util;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 2/7/15.
 */
public class ChunkLocalCoord {
    public static Coord3[] Locals = new Coord3[Chunk.XLENGTH * Chunk.XLENGTH * Chunk.XLENGTH];
    static {
        for(int i=0;i<Chunk.XLENGTH; ++i)
            for (int j=0;j<Chunk.XLENGTH; ++j)
                for (int k=0;k<Chunk.XLENGTH; ++k) {
                    Locals[i * Chunk.XLENGTH *Chunk.XLENGTH + j * Chunk.XLENGTH + k] = new Coord3(i,j,k);
                }
    }
}
