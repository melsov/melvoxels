package voxel.landscape.settings;

import com.jme3.math.Vector3f;
import voxel.landscape.Chunk;
import voxel.landscape.coord.*;

/**
 * Created by didyouloseyourdog on 4/9/15.
 */
public class BuildSettings {

    public static int ADD_COLUMN_RADIUS = 2;
    public static int PREPARE_COLUMN_RADIUS = ADD_COLUMN_RADIUS + 1;
    public static int STORE_COLUMN_RADIUS = PREPARE_COLUMN_RADIUS + 2;

    private static Box2 AddBox = new Box2(new Coord2(-ADD_COLUMN_RADIUS), new Coord2(ADD_COLUMN_RADIUS * 2));
    private static Box2 PrepareBox = new Box2(new Coord2(-PREPARE_COLUMN_RADIUS), new Coord2(PREPARE_COLUMN_RADIUS * 2));

    public static boolean ChunkCoordWithinPrepareRadius(Vector3f camera, Coord3 chunkCoord) {
        return ChunkCoordWithinBox(camera, chunkCoord, PrepareBox);
//        return !ChunkCoordWithinRadius(camera, chunkCoord, PREPARE_COLUMN_RADIUS);
    }
    
    public static boolean ChunkCoordWithinAddRadius(Vector3f camera, ICoordXZ chunkCoord) {
        return ChunkCoordWithinBox(camera, chunkCoord, AddBox);
//        return ChunkCoordWithinRadius(camera, chunkCoord, ADD_COLUMN_RADIUS);
    }
    private static boolean ChunkCoordWithinBox(Vector3f camera, ICoordXZ chunkCoord, Box2 box2) {
        return cameraCenteredBox(camera, box2).contains(new Coord2(chunkCoord.getX(), chunkCoord.getZ()));
    }

    private static boolean ChunkCoordWithinRadius(Vector3f camera, ICoordXZ chunkCoord, int radius) {
        Coord3 cameraCoord = Chunk.ToChunkPosition(Coord3.FromVector3f(camera));
        return cameraCoord.distanceXZSquared(chunkCoord) < radius * radius;
    }
    private static Box2 cameraCenteredBox(Vector3f camera, Box2 box2) {
        box2 = box2.clone();
        box2.centerOver(Coord2.FromCoord3XZ(Chunk.ToChunkPosition(Coord3.FromVector3f(camera))));
        return box2;
    }
    public static Box2 addColumnsBox(Vector3f camera) {
        return cameraCenteredBox(camera, AddBox);
    }
    public static BoxBorder2 prepareColumnsBorder(Vector3f camera) {
        return cameraCenteredBoxOuterBorder(camera, AddBox, PREPARE_COLUMN_RADIUS - ADD_COLUMN_RADIUS);
    }
    public static BoxBorder2 storeColumnsBorder(Vector3f camera) {
        return cameraCenteredBoxOuterBorder(camera, PrepareBox, STORE_COLUMN_RADIUS - PREPARE_COLUMN_RADIUS);
    }
    private static BoxBorder2 cameraCenteredBoxOuterBorder(Vector3f camera, Box2 box2, int border) {
        return new BoxBorder2(cameraCenteredBox(camera, box2), border, true);
    }



}
