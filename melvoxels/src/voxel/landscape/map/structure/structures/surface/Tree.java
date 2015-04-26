package voxel.landscape.map.structure.structures.surface;

import voxel.landscape.BlockType;
import voxel.landscape.coord.Axis;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.structure.structures.AbstractStructure;
import voxel.landscape.map.structure.structures.StructureUtil;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 4/19/15.
 */
public class Tree extends AbstractStructure {

    private static int foliageHalfDimension = 5;
    protected static HashMap<Coord3, BlockType>[] outerBlocks;

    public Tree(int _seed) {
        super(_seed);
    }

    static {
        build();
    }

    private static void build() {
        outerBlocks = new HashMap[] {new HashMap<>(64)};
        StructureUtil.AddVertical(outerBlocks[0], BlockType.SAND, Coord3.Zero.clone(), 4 );
        StructureUtil.AddPyramid(outerBlocks[0], Coord3.ypos.multy(4), new Coord2(foliageHalfDimension), Axis.Y, BlockType.CAVESTONE, true, true);
    }

    @Override
    public Coord3 viablePlot(Coord3 global, TerrainMap map) {
        int block = map.lookupOrCreateBlock(global);
        if (BlockType.GRASS.ordinal() == block || BlockType.DIRT.ordinal() == block) {
            return new Coord3(0);
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
