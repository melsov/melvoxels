package voxel.landscape.map.structure.structures.surface;

import voxel.landscape.coord.Axis;
import voxel.landscape.BlockType;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.structure.structures.AbstractStructure;
import voxel.landscape.map.structure.structures.StructureUtil;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 3/29/15.
 */
public class Pyramid extends AbstractStructure {

    private static int baseHalfDimension = 5;
    protected static HashMap<Coord3, BlockType>[] outerBlocks;
    protected static HashMap<Coord3, BlockType>[] innerBlocks;

    static {
        buildPyramids();
    }

    private static void buildPyramids() {
        outerBlocks = new HashMap[1];
        innerBlocks = new HashMap[1];
        outerBlocks[0] = new HashMap<>(256);
        int level = 4;
        StructureUtil.AddPyramid(outerBlocks[0], Coord3.ypos.multy(level).clone(), new Coord2(baseHalfDimension), Axis.Y, BlockType.SAND, true);
        while (--level > 0) {
            StructureUtil.AddRectangle(outerBlocks[0], new Coord3(-baseHalfDimension + 1, level, -baseHalfDimension + 1), new Coord2(baseHalfDimension * 2 - 2), Axis.Y, BlockType.CAVESTONE, false);
        }
    }

    public Pyramid(int _seed) {
        super(_seed);
    }

    @Override
    public Coord3 viablePlot(Coord3 global, TerrainMap map) {
        return Coord3.Zero.clone();
    }

    @Override
    public HashMap<Coord3, BlockType> getBlocks() {
        if (seed % 2 == 0) {
            return outerBlocks[0];
        } else {
            return outerBlocks[0];
        }
    }

    @Override
    public HashMap<Coord3, BlockType> getInnerBlocks() {
        return null;
    }

}
