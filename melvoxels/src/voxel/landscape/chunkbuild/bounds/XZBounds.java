package voxel.landscape.chunkbuild.bounds;

import com.jme3.renderer.Camera;
import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Box2;
import voxel.landscape.util.Asserter;

/**
 * Created by didyouloseyourdog on 1/2/15.
 * Defines which chunks should be rendered
 * based on proximity to the player camera
 */
public class XZBounds {
    public final Camera camera;

    private Box2 bounds;
    public int radius;

    public XZBounds(Camera _camera, int _radius ) {
        Asserter.assertTrue(_radius > 0, "Not allowed: _radius <= 0 : " + _radius);
        camera = _camera; radius = _radius;
    }

    private void updateBounds() {
        Coord3 camloc = Chunk.ToChunkPosition(Coord3.FromVector3f(camera.getLocation()));
        bounds = new Box2(new Coord2(camloc.x - radius, camloc.z - radius), new Coord2(radius*2));
    }

    public boolean contains(Coord3 chunkCoord) {
        return Chunk.ToChunkPosition(Coord3.FromVector3f(camera.getLocation())).distanceSquared(chunkCoord) < radius*radius;
    }

}
