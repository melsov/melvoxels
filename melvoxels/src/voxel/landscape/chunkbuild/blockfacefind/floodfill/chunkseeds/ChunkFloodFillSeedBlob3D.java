package voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkseeds;

import voxel.landscape.chunkbuild.blockfacefind.ChunkLocalCoord;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by didyouloseyourdog on 10/14/14.
 * A collection of 'chunk local' block coords that
 * are all contiguous, where contiguity is valid in
 * any direction x, y or z
 */
public class ChunkFloodFillSeedBlob3D implements Serializable {

    private Coord3 seed;
    public Coord3 getSeed() { return  seed; }
    public final HashSet<ChunkLocalCoord> coords = new HashSet<>(128);

    public ChunkFloodFillSeedBlob3D(Coord3 _seed) {
        seed = _seed;
        coords.add(new ChunkLocalCoord(seed));
    }

    public boolean addCoord(Coord3 co) {
        if (isCoordAdjacent(co)) {
            coords.add(new ChunkLocalCoord(co));
            return true;
        }
        return false;
    }

    public void addMembersOfSet(ChunkFloodFillSeedBlob3D other) {
        coords.addAll(other.coords);
    }

    public boolean isCoordAdjacent(Coord3 co) {
        for(Coord3 nudge : Direction.DirectionCoords) {
            if (coords.contains(new ChunkLocalCoord(co.add(nudge)))) {
                return true;
            }
        }
        return false;
    }
}
