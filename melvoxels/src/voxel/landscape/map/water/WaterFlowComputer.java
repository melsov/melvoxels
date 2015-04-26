package voxel.landscape.map.water;

import voxel.landscape.BlockType;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.map.TerrainMap;

import java.util.ArrayList;

/**
 * Created by didyouloseyourdog on 8/9/14.
 */
public class WaterFlowComputer
{
    public static final byte LACK_OF_WATER_LEVEL = 0;
    public static final byte MIN_WATER_LEVEL = 0;
    public static final byte MAX_WATER_LEVEL = 3;

    private static void Scatter(TerrainMap map, Coord3 pos) {
        ArrayList<Coord3> list = new ArrayList<Coord3>(24);
        list.add(pos);
        Scatter(map, list);
    }
    public static void Scatter(TerrainMap map, ArrayList<Coord3> list) {
        WaterLevelMap liquidLevelMap = map.getLiquidLevelMap();
        for(int i=0; i<list.size(); i++) {
            Coord3 pos = list.get(i);
            if(pos.y<0) continue;
            int block;
            int waterLevel = liquidLevelMap.GetWaterLevel(pos);
            for(Coord3 dir : Direction.DirectionCoordsXZAndDown) {
                Coord3 nextPos = pos.add(dir);
                block = map.lookupBlock(nextPos);
                byte levelToAdd = dir.y == -1 ?  (byte) waterLevel : (byte) (waterLevel - WaterFlowComputerUtils.GetWaterStep(block));
                if (levelToAdd <= MIN_WATER_LEVEL) {
                    break;
                }
                if( BlockType.AcceptsWater(block) && liquidLevelMap.SetWaterLevelIfPossible(levelToAdd, nextPos) ) {
                    list.add( nextPos );
                    if(!BlockType.IsEmpty(block)) {
                        map.setWaterRunOff(nextPos);
                        WaterFlowComputerUtils.SetWaterDirty(map, nextPos);
                    }
                    if (dir.y == -1) break; // don't add laterally if water flowed down
                }
            }
        }
    }

    public static void RecomputeWaterAtPosition(TerrainMap map, Coord3 pos) {
        WaterLevelMap liquidMap = map.getLiquidLevelMap();
        int oldWater = liquidMap.GetWaterLevel(pos);
        int block = map.lookupBlock(pos);
        int newWater = BlockType.WaterLevelForType(block);

        if (oldWater > newWater) {
            RemoveWater(map, pos);
        }
        if (newWater > MIN_WATER_LEVEL) {
            Scatter(map, pos);
        }
        // Did we add air (or lava??) just below water?
        else if (!BlockType.IsWaterType(block) && BlockType.AcceptsWater(block)
                && BlockType.IsWaterType(map.lookupBlock(pos.add(Coord3.ypos)))) {
            Scatter(map, pos.add(Coord3.ypos));
        }
    }

    private static void RemoveWater(TerrainMap map, Coord3 pos) {
        ArrayList<Coord3> list = new ArrayList<Coord3>(24);
        list.add(pos);
        RemoveWater(map, list);
    }

    private static void RemoveWater(TerrainMap map, ArrayList<Coord3> list) {
        WaterLevelMap liquidLevelMap = map.getLiquidLevelMap();
        /* set remove coords to MAX water level (counter-intuitively)
         * this way, we will iterate over any positions where these blocks had 'water influence'
         * we will set them all to zero water level soon right after checking influence (nextWater). */
        for(Coord3 pos : list) {
            liquidLevelMap.SetWaterLevel(MAX_WATER_LEVEL, pos);
        }

        ArrayList<Coord3> reAddWaterCoords = new ArrayList<Coord3>();
        for(int i=0; i<list.size(); i++) {
            Coord3 pos = list.get(i);
            if(pos.y<0) continue;
            //NOTE: this is ok because a water block that was just removed will already be set to another block type
            if(map.lookupBlock(pos.x, pos.y, pos.z) == BlockType.WATER.ordinal()) {
                reAddWaterCoords.add( pos );
                continue;
            }
            byte nextWater = (byte) (liquidLevelMap.GetWaterLevel(pos) - WaterFlowComputerUtils.WATER_STEP);
            liquidLevelMap.SetWaterLevel(LACK_OF_WATER_LEVEL, pos);
            map.unsetWater(pos);
            if (nextWater <= MIN_WATER_LEVEL) {
                continue;
            }

            for(Coord3 dir : Direction.DirectionCoordsXZAndDown) {
                Coord3 nextPos = pos.add(dir);
                int block = map.lookupBlock(nextPos);
                if(BlockType.AcceptsWater(block)) {
                    if(dir.y == -1 || liquidLevelMap.GetWaterLevel(nextPos) <= nextWater) {
                        list.add( nextPos );
                    } else {
                        reAddWaterCoords.add( nextPos );
                    }
                    if(!BlockType.IsEmpty(block)) WaterFlowComputerUtils.SetWaterDirty(map, nextPos);
                }
            }
        }

        Scatter(map, reAddWaterCoords);
    }

}
