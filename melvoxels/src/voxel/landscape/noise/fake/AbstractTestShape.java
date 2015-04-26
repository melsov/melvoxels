package voxel.landscape.noise.fake;

import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 12/28/14.
 */
public abstract class AbstractTestShape {
    public Box box;

    public abstract boolean isOnBorder(Coord3 coord3);
}
