package voxel.landscape.map.structure.structures.surface;

import voxel.landscape.BlockType;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.structure.structures.AbstractStructure;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 3/28/15.
 */
public class TotemPole extends AbstractStructure {

    public TotemPole(int _seed) {
        super(_seed);
    }

    protected static HashMap<Coord3, BlockType>[] outerBlocks = new HashMap[1];

    static {
        build();
    }

    private static void build() {
        for(int y = 0; y < 5; ++y) {
            outerBlocks[0].put(new Coord3(0, y, 0), BlockType.SAND);
        }
    }

    @Override
    public Coord3 viablePlot(Coord3 global, TerrainMap map) {
        if (BlockType.IsSolid(map.lookupOrCreateBlock(global.minus(Coord3.up)))) {
            return Coord3.Zero.clone();
        }
        return null;
    }

    @Override
    public HashMap<Coord3, BlockType> getBlocks() {
        return outerBlocks[0];
    }

    @Override
    public HashMap<Coord3, BlockType> getInnerBlocks() {
        return null;
    }

}
