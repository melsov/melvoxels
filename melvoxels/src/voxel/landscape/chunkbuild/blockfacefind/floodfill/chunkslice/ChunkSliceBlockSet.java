package voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkslice;

import voxel.landscape.Chunk;
//import voxel.landscape.coord.LocalCoord3;
import voxel.landscape.chunkbuild.blockfacefind.ChunkLocalCoord;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;

import java.util.HashSet;

public class ChunkSliceBlockSet
{
    private Coord3 seedGlobal;
    public Coord3 getSeedGlobal() { return seedGlobal; }
    private int axis;
    public int getAxis(){ return axis; }
    public final HashSet<ChunkLocalCoord> coords = new HashSet<ChunkLocalCoord>(128);

    public ChunkSliceBlockSet(Coord3 _seed, int _axis) {
        seedGlobal = _seed; axis = _axis;
        coords.add(new ChunkLocalCoord(seedGlobal));
    }

    public boolean addCoord(Coord3 global) {
        Coord3 local = Chunk.ToChunkLocalCoord(global);
        if (isCoordAdjacent(local)) {
            coords.add(new ChunkLocalCoord(local));
            return true;
        }
        return false;
    }

    public void addMembersOfSet(ChunkSliceBlockSet other) {
        coords.addAll(other.coords);
    }

    public boolean isCoordAdjacent(Coord3 global) {
        Coord3 local = Chunk.ToChunkLocalCoord(global);
        for(Coord3 nudge : Direction.NudgeDirectionsNormalToAxis(axis)) {
            if (coords.contains(new ChunkLocalCoord(local.add(nudge)))) {
                return true;
            }
        }
        return false;
    }
}
