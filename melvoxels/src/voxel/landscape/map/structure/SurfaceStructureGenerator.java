package voxel.landscape.map.structure;

import com.sudoplay.joise.ThresholdModuleSet;
import com.sudoplay.joise.module.*;
import voxel.landscape.VoxelLandscape;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.structure.structures.AbstractStructure;
import voxel.landscape.map.structure.structures.surface.SurfaceStructureType;

/**
 * Created by didyouloseyourdog on 3/28/15.
 */
public class SurfaceStructureGenerator {
    Module module;
    public SurfaceStructureGenerator() {
        setupModule();
    }

    public AbstractStructure structureAt(int x, int y, int z) {
        double value = module.get(x, y, z);
        return SurfaceStructureType.structureInstance((int) value, 123 ); //TODO: seed generation
//        Coord3 local = Chunk.ToChunkLocalCoord(x,y,z);
//        if (local.x == 4 && local.z == 4) {
//            return new Pyramid(123);
//        }
//        return null;
    }
    public AbstractStructure structureAt(Coord3 global) {
        return structureAt(global.x, global.y, global.z);
    }

    private void setupModule() {
        ModuleCellGen moduleCellGen = new ModuleCellGen();
        moduleCellGen.setSeed(VoxelLandscape.WorldSettings.seed);
        ModuleCellular moduleCellular = new ModuleCellular();
        moduleCellular.setCoefficients(12, .4, 0, 0); /*  */
        moduleCellular.setCellularSource(moduleCellGen);

        ModuleScaleOffset moduleScaleOffset = new ModuleScaleOffset();
        moduleScaleOffset.setScale(1);
        moduleScaleOffset.setOffset(0d);
        moduleScaleOffset.setSource(moduleCellular);

        ModuleScaleDomain moduleScaleDomain = new ModuleScaleDomain();
        moduleScaleDomain.setScaleX(1);
        moduleScaleDomain.setScaleY(1);
        moduleScaleDomain.setSource(moduleScaleOffset);

        ModuleCache moduleCache = new ModuleCache();
        moduleCache.setSource(moduleScaleDomain);

        double baseThreshold = .25d; //higher baseThreshold means more frequent structures
        ModuleSelect moduleSelect = new ModuleSelect();
        moduleSelect.setHighSource(0d); //no structure
        moduleSelect.setLowSource(setupStructureSelectModule(moduleCache, baseThreshold)); //which structure
        moduleSelect.setThreshold(baseThreshold);
        moduleSelect.setControlSource(moduleCache);

        module = moduleSelect;
    }
    private Module setupStructureSelectModule(Module source, double baseThreshold) {
        ModuleSelectChain moduleSelectChain = new ModuleSelectChain();
        moduleSelectChain.setChain(source, new ThresholdModuleSet[] {
                new ThresholdModuleSet(new ScalarParameter(SurfaceStructureType.TOTEM_POLE.index), .14),
                new ThresholdModuleSet(new ScalarParameter(SurfaceStructureType.PYRAMID.index), .18),
                new ThresholdModuleSet(new ScalarParameter(SurfaceStructureType.TREE.index), .08),
        });
        return moduleSelectChain;
    }


}
