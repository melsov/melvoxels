package voxel.landscape.noise.fake;

import voxel.landscape.coord.Axis;
import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;

/**
 * Created by didyouloseyourdog on 12/23/14.
 */
public class Cone extends AbstractTestShape {
    public Box box;
    public int taperDirection = Direction.ZPOS;
    public boolean sureFloor = false;
    public boolean fatSideIsOpen = true;

    public Cone(Box _box) {
        box = _box;
    }

    @Override
    public boolean isOnBorder(Coord3 co) {
        if (!box.contains(co)) {
            return false;
        }
        if (sureFloor && box.start.y == co.y) return true;
        boolean onBorder = false;
        int oppTaperDirection = Direction.OppositeDirection(taperDirection);
        int fatBorderLocation = box.borderLocation(oppTaperDirection);
        int taperComponent = co.componentForDirection(taperDirection);
        if (!fatSideIsOpen && taperComponent == fatBorderLocation) {
            return true;
        }
        int taperAxis = Direction.AxisForDirection(taperDirection);

        for(int dir : Direction.PositiveDirections) {
            if (dir != taperDirection && dir != oppTaperDirection) {
                int dirAxis = Direction.AxisForDirection(dir);
                int otherAxis = Axis.OtherAxis(taperAxis, dirAxis);
                int taperOffset = Math.abs(taperComponent - fatBorderLocation);
                int dirSize = box.dimensions.componentForAxis(dirAxis);
                int otherSize = box.dimensions.componentForAxis(otherAxis);
                int dirComponent = co.componentForAxis(dirAxis);
                int otherComponent = co.componentForAxis(otherAxis);
                int coneRingHeightForDir = Math.max(dirSize - taperOffset, 0);
                int coneRingWidthForDir = Math.max(otherSize - taperOffset, 0);
                int boxStartDir = box.start.componentForAxis(dirAxis);
                int boxStartOther = box.start.componentForAxis(otherAxis);
                if (dirComponent == boxStartDir + coneRingHeightForDir || dirComponent == boxStartDir ) {
                    onBorder = boxStartOther + coneRingWidthForDir >= otherComponent;
                }
                if (onBorder) break;
            }
        }
        return onBorder;
    }
}
