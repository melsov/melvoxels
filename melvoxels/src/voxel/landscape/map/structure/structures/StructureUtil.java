package voxel.landscape.map.structure.structures;

import voxel.landscape.coord.Axis;
import voxel.landscape.BlockType;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 3/29/15.
 */
public class StructureUtil {

    public static void AddPyramid(HashMap<Coord3, BlockType> blocks, Coord3 baseCenter, Coord2 baseHalfDimensions, int normal, BlockType blockType, boolean hollow) {
        AddPyramid(blocks, baseCenter, baseHalfDimensions, normal, blockType, hollow, false);
    }

    public static void AddPyramid(HashMap<Coord3, BlockType> blocks, Coord3 baseCenter, Coord2 baseHalfDimensions, int normal, BlockType blockType, boolean hollow, boolean oddBase) {
        Coord3 startCorner = baseCenter.add(new Coord3(baseHalfDimensions.getX(), 0, baseHalfDimensions.getZ()).multy(-1));
        for (int i = 0; i < baseHalfDimensions.getX(); ++i) {
            Coord2 dims = (baseHalfDimensions.minus(i,i)).multy(2);
            if (oddBase) { dims = dims.add(new Coord2(1)); }
            if (dims.getX() == 0 || dims.getZ() == 0) break;
            StructureUtil.AddRectangle(blocks, startCorner.add(i,i,i), dims, Axis.Y, blockType, !hollow || (i == 0));
        }
    }

    public static void AddRectangle(HashMap<Coord3, BlockType> blocks, Coord3 negativeCorner, Coord2 dimensions, int normal, BlockType blockType, boolean filledIn) {
        Coord3[] directions = Direction.UnitCoordsForNormalAxis(normal);
        int startDimOne = GetComponentWithNormal(negativeCorner, normal, false);
        int startDimTwo = GetComponentWithNormal(negativeCorner, normal, true);
        for (Coord3 co = negativeCorner.clone(); GetComponentWithNormal(co, normal, false) < startDimOne + dimensions.getX(); AddToComponentWithNormal(co, 1, normal, false)) {
            for(SetComponentWithNormal(co, startDimTwo, normal, true) ; GetComponentWithNormal(co, normal, true) < startDimTwo + dimensions.getZ(); AddToComponentWithNormal(co, 1, normal, true)) {
                blocks.put(co.clone(), blockType);
                if (!filledIn) {
                    int compOne = GetComponentWithNormal(co, normal, false);
                    if (!(compOne == startDimOne || compOne == startDimOne + dimensions.getX() - 1)){
                        AddToComponentWithNormal(co, dimensions.getZ() - 2, normal, true);
                    }
                }
            }
        }
    }
    public static void AddVertical(HashMap<Coord3, BlockType> blocks, BlockType blockType, Coord3 start, int height) {
        for (int y = start.y; y < start.y + height; ++y) {
            blocks.put(new Coord3(start.x, y, start.z), blockType);
        }
    }

    private static int GetComponentWithNormal(Coord3 coord3, int normal, boolean wantSecondComponent) {
        if (!wantSecondComponent) {
            return normal == Axis.X ? coord3.y : coord3.x;
        }
        return normal == Axis.Z ? coord3.y : coord3.z;
    }
    private static void AddToComponentWithNormal(Coord3 coord3, int value, int normal, boolean addToSecond) {
        if (!addToSecond) {
            if (normal == Axis.X) {
                coord3.y += value;
            } else {
                coord3.x += value;
            }
        } else {
            if (normal == Axis.Z) {
                coord3.y += value;
            } else {
                coord3.z += value;
            }
        }
    }
    private static void SetComponentWithNormal(Coord3 coord3, int value, int normal, boolean addToSecond) {
        if (!addToSecond) {
            if (normal == Axis.X) {
                coord3.y = value;
            } else {
                coord3.x = value;
            }
        } else {
            if (normal == Axis.Z) {
                coord3.y = value;
            } else {
                coord3.z = value;
            }
        }
    }

}
