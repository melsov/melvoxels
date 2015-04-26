package voxel.landscape.map.biome;

import voxel.landscape.map.structure.structures.surface.SurfaceStructureType;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 4/21/15.
 */
public enum BiomeType {

    MOUNTAINS(0),
    PASTURE(1),
    DESERT(2),
    ;
    public final int index;
    private final HashMap<SurfaceStructureType, Double> structurePrevalence =new HashMap<>(SurfaceStructureType.values().length);

    private BiomeType(int index) {
        this.index = index;
    }

    public HashMap<SurfaceStructureType, Double> getStructurePrevalence() {
        if (this.structurePrevalence.size() == 0) {
            switch (this) {
                case MOUNTAINS:
                    structurePrevalence.put(SurfaceStructureType.TREE, 10d);
                    break;
                case PASTURE:
                    structurePrevalence.put(SurfaceStructureType.TREE, 3d);
                    break;
                case DESERT:
                    structurePrevalence.put(SurfaceStructureType.TREE, 1d);
                    structurePrevalence.put(SurfaceStructureType.PYRAMID, 1d);
                    break;
                default:
                    break;
            }
        }
        return structurePrevalence;
    }
}
