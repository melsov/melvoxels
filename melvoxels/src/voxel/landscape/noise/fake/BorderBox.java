package voxel.landscape.noise.fake;

import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;

/**
 * Created by didyouloseyourdog on 12/23/14.
 */
public class BorderBox extends AbstractTestShape {
    public Box box;
    public boolean[] openFaces = new boolean[6];

    public BorderBox(Box b) {
        box = b;
    }

    @Override
    public boolean isOnBorder(Coord3 co) {
        boolean onBorder = box.isOnBorder(co);
        if (onBorder)
            for(int dir : Direction.Directions) {
                if (openFaces[dir]) {
                    onBorder = co.componentForDirection(dir) != box.borderLocation(dir);
                    if (!onBorder) break;
                }
            }
        return onBorder;
    }
}
