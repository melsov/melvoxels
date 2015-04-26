package voxel.landscape.chunkbuild;

import com.jme3.renderer.Camera;
import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;

import java.util.Comparator;

/**
 * Created by didyouloseyourdog on 8/4/14.
 */
public class ColumnCamComparator implements Comparator<Coord2> {

    private Camera cam;
    public ColumnCamComparator(Camera _cam) {
        cam = _cam;
    }
    @Override
    public int compare(Coord2 o1, Coord2 o2) {
        Coord3 camxz = Coord3.FromVector3f(cam.getLocation());
        Coord3 colWoco1 = Chunk.ToWorldPosition(new Coord3(o1.x, 0, o1.y));
        Coord3 colWoco2 = Chunk.ToWorldPosition(new Coord3(o2.x, 0, o2.y));
        int dsq1 = (int) camxz.distanceSquared(colWoco1);
        int dsq2 = (int) camxz.distanceSquared(colWoco2);
        if (dsq1 < dsq2) {
            return -1;
        } else if (dsq2 < dsq1 ) {
            return 1;
        }
        return 0;
    }
}
