package voxel.landscape.noise;

import com.sudoplay.joise.module.*;

/**
 * Created by didyouloseyourdog on 10/3/14.
 */
public class FractalSettings {
    public ModuleFractal.FractalType fractalType = ModuleFractal.FractalType.BILLOW;
    public int octaves = 2;
    public double frequency = 0.25f;
    public double lacunarity = ModuleFractal.DEFAULT_LACUNARITY;
    public ModuleBasisFunction.BasisType basisType = ModuleBasisFunction.BasisType.GRADIENT;
    public ModuleBasisFunction.InterpolationType interpolationType = ModuleBasisFunction.InterpolationType.QUINTIC;
    public long seed;

    public boolean shouldCache;
    public ModuleCache moduleCache;

    public FractalSettings (long _seed) {
        seed = _seed;
    }


    public Module makeFractalModule() {
        ModuleFractal landShape = new ModuleFractal(fractalType, basisType, interpolationType);
        landShape.setNumOctaves(octaves);
        landShape.setFrequency(frequency);
        landShape.setLacunarity(lacunarity);
        landShape.setSeed(seed);

        if (shouldCache) {
            moduleCache = new ModuleCache();
            moduleCache.setSource(landShape);
        }
        return landShape;
    }

    public static FractalSettings CaveRidgedMultiSettings(long _seed) {
        FractalSettings caveASettings = new FractalSettings(_seed);
        caveASettings.fractalType = ModuleFractal.FractalType.RIDGEMULTI;
        caveASettings.octaves = 1;
        caveASettings.frequency = 1d;
        caveASettings.shouldCache = false;
        return caveASettings;
    }
    public static FractalSettings FBMHighlandSettings(long _seed) {
        FractalSettings fbmSettings = new FractalSettings(_seed);
        fbmSettings.fractalType = ModuleFractal.FractalType.FBM;
        fbmSettings.octaves = 4;
        fbmSettings.frequency = 1d;
        fbmSettings.shouldCache = false;
        return fbmSettings;
    }
}