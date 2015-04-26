package voxel.landscape.map.structure.structures;

import voxel.landscape.BlockType;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 3/28/15.
 */
public abstract class AbstractStructure {

    protected final int seed;

    public AbstractStructure(int _seed) {
        seed = _seed;
    }

    public abstract Coord3 viablePlot(Coord3 global, TerrainMap map);
    public abstract HashMap<Coord3, BlockType> getBlocks();
    public abstract HashMap<Coord3, BlockType> getInnerBlocks();
}
