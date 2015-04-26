package voxel.landscape.map.structure.structures.surface;

import voxel.landscape.map.structure.structures.AbstractStructure;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 4/19/15.
 */
public enum SurfaceStructureType {
    LACK_OF_STRUCTURE(0, null),
    TREE(12, Tree.class),
    TOTEM_POLE(10, TotemPole.class),
    PYRAMID(11, Pyramid.class),
    ;

    private final Class structureClass;
    public final int index;

    private static final HashMap<Integer, SurfaceStructureType> lookup = new HashMap<Integer, SurfaceStructureType>();
    static {
        for (SurfaceStructureType surfaceStructureType : EnumSet.allOf(SurfaceStructureType.class)) {
            lookup.put(surfaceStructureType.index, surfaceStructureType);
        }
    }

    private SurfaceStructureType(int index, Class structureClass) {
        this.index = index;
        this.structureClass = structureClass;
    }

    public static SurfaceStructureType getType(int index) {
        return lookup.get(index);
    }

    public static AbstractStructure structureInstance(int index, int seed) {
        switch (getType(index)) {
            case TOTEM_POLE:
                return new Tree(seed);
            case PYRAMID:
                return new Tree(seed);
            case TREE:
                return new Tree(seed);
            default:
                return null;
        }
    }
}
