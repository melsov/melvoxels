package voxel.landscape.map.water;

import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by didyouloseyourdog on 8/14/14.
 */
public class LiquidUpdater
{
    private HashMap<Coord3, Integer> instantUpdates = new HashMap<Coord3, Integer>(6);
    private HashMap<Coord3, Integer> updates = new HashMap<Coord3, Integer>(6);

    private float timeStep;
    private float lastUpdateTime;

    public LiquidUpdater(float _timeStep) { timeStep = _timeStep; }

    public void addCoord(Coord3 pos, int targetWaterLevel) {
        instantUpdates.put(pos, new Integer(targetWaterLevel));
    }

    public void StepScatter(TerrainMap map, float tpf) {

        lastUpdateTime += tpf;
        if (lastUpdateTime > timeStep) {
            lastUpdateTime = -.01f;
        }
        if (instantUpdates.size() == 0 && updates.size() == 0) {
            return;
        }

        WaterLevelMap liquidMap = map.getLiquidLevelMap();
        ArrayList<Coord3> liquidPositions = new ArrayList<Coord3>(instantUpdates.size() + updates.size());

        if (lastUpdateTime < 0) {
            Iterator iterator = updates.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Coord3, Integer> entry = (Map.Entry<Coord3, Integer>) iterator.next();
                Coord3 pos = entry.getKey();
                Integer targetValue = entry.getValue();
                int currentValue = liquidMap.GetWaterLevel(pos);
                if (targetValue.intValue() > currentValue) {
                    liquidMap.SetWaterLevel((byte) (Math.min(WaterFlowComputer.MAX_WATER_LEVEL, currentValue + WaterFlowComputerUtils.WATER_STEP)), pos);
                    WaterFlowComputerUtils.SetWaterDirty(map, pos);
                    liquidPositions.add(pos.clone());
                } else {
                    iterator.remove();
                }
            }
        }

        Iterator instantIterator = instantUpdates.entrySet().iterator();
        while (instantIterator.hasNext()) {
            Map.Entry<Coord3, Integer> entry = (Map.Entry<Coord3, Integer>) instantIterator.next();
            Coord3 pos = entry.getKey();
            Integer targetValue = entry.getValue();
            int currentValue = liquidMap.GetWaterLevel(pos);
            if (targetValue.intValue() > currentValue) {
                liquidMap.SetWaterLevel((byte) (Math.min(WaterFlowComputer.MAX_WATER_LEVEL, currentValue + WaterFlowComputerUtils.WATER_STEP)), pos);
                map.setWater(pos);
                WaterFlowComputerUtils.SetWaterDirty(map, pos);
                liquidPositions.add(pos.clone());
                updates.put(pos.clone(), targetValue);
            }
            instantIterator.remove();
        }


        WaterFlowComputer.Scatter(map, liquidPositions);
    }

}
