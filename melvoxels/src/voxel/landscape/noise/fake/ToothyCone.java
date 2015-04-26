package voxel.landscape.noise.fake;

import voxel.landscape.coord.Axis;
import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 12/23/14.
 */
public class ToothyCone extends Cone {
    public int toothAxis;
    public ToothyCone(Box _box) {
        this(_box, Axis.Y);
    }
    public ToothyCone(Box _box, int _toothAxis) {
        super(_box);
        toothAxis = _toothAxis;
    }
    @Override
    public boolean isOnBorder(Coord3 co) {
        co = co.clone();
        int val = (co.componentForAxis(toothAxis) % 2 == 0 ? 1 : 0) + co.componentForAxis(toothAxis);
        co.setComponentInAxis(toothAxis, val);

        return super.isOnBorder(co);
    }
}
