package voxel.landscape.chunkbuild.meshbuildasync;

import voxel.landscape.MeshSet;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 8/18/14.
 */
public class ChunkMeshBuildingSet {
    public MeshSet meshSet;
    public MeshSet liquidMeshSet;
    public boolean isOnlyLight;
    public boolean isOnlyLiquid;
    public Coord3 chunkPosition;

    public static final ChunkMeshBuildingSet POISON_PILL = new ChunkMeshBuildingSet();
}
