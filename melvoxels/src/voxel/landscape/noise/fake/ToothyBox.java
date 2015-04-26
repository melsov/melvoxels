package voxel.landscape.noise.fake;

import voxel.landscape.coord.Axis;
import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord3;

/**
 * Created by didyouloseyourdog on 12/29/14.
 */
public class ToothyBox extends BorderBox {

    private Box teeth;
    private int toothAxis = Axis.Z;

    public ToothyBox(Box _box) {
        super(_box);
        teeth = new Box(box.start.add(Coord3.One), box.dimensions.minus(Coord3.One.multy(2)));
        openFaces = new boolean[] {false, false, true, false, true, true};
    }
    @Override
    public boolean isOnBorder(Coord3 co) {
        if (super.isOnBorder(co)) {
            return true;
        }
        int comp = co.componentForAxis(toothAxis);
        if ((comp & 1) == 1 && teeth.isOnBorder(co)) {
            return true;
        }
        return false;
    }
}
