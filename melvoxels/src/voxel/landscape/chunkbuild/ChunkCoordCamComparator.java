package voxel.landscape.chunkbuild;

import com.jme3.renderer.Camera;
import voxel.landscape.Chunk;
import voxel.landscape.chunkbuild.meshbuildasync.ChunkMeshBuildingSet;
import voxel.landscape.coord.Coord3;

import java.util.Comparator;

/**
 * Created by didyouloseyourdog on 8/17/14.
 */
public class ChunkCoordCamComparator implements Comparator<ChunkMeshBuildingSet> {

    private Camera cam;
    public ChunkCoordCamComparator(Camera _cam) {
        cam = _cam;
    }

    @Override
    public int compare(ChunkMeshBuildingSet cb1, ChunkMeshBuildingSet cb2) {
        if (cb1.chunkPosition == null || cb2.chunkPosition == null) return 0;
        Coord3 camxz = Coord3.FromVector3f(cam.getLocation());
        Coord3 woco1 = Chunk.ToWorldPosition(cb1.chunkPosition);
        Coord3 woco2 = Chunk.ToWorldPosition(cb2.chunkPosition);
        int dsq1 = (int) camxz.distanceSquared(woco1);
        int dsq2 = (int) camxz.distanceSquared(woco2);
        if (dsq1 < dsq2) {
            return -1;
        } else if (dsq2 < dsq1 ) {
            return 1;
        }
        return 0;
    }

    // CONSIDER: is this flawed since ordering won't
    // Update as the player moves?
    // CONSIDER: more sophisticated version of this: shift the measuring position to chunk geometric center
    // use the scalar product / (magnitudes) to get cos() for angle between distance vect and camera forward vec.
    // slightly favor values that are closer to 1 (still, this is kind of flawed as a a way of organizing chunks, but even so, might help)

}
