package voxel.landscape.noise;

import com.sudoplay.joise.module.*;
import voxel.landscape.BlockType;

public class TerrainNoiseSettings {

    public FractalSettings fractalSettings;
    public ModuleSelectSettings moduleSelectSettings;
    public float autoCorrectLow = 0f;
    public float autoCorrectHigh = 1f;
    public float xzscale = 1f;
    public float offset = 0f;
    public float yScale = 1f;
    public float xScale = 1f;
    public boolean renderWithBlockColors = false;

    public void setUpModuleSelectSettings(Module _controlSource) {
        moduleSelectSettings = new ModuleSelectSettings(_controlSource);
    }
    public TerrainNoiseSettings(long _seed) {
       fractalSettings = new FractalSettings(_seed);
    }

    public static TerrainNoiseSettings LowLandTerrainNoiseSettings(long _seed, boolean shouldCache) {
        TerrainNoiseSettings lowlandSettings = new TerrainNoiseSettings(_seed);
        lowlandSettings.fractalSettings.fractalType = ModuleFractal.FractalType.BILLOW;
        lowlandSettings.fractalSettings.octaves = 2;
        lowlandSettings.fractalSettings.frequency = 0.25f;
        lowlandSettings.fractalSettings.shouldCache = shouldCache;
        lowlandSettings.xzscale = 0.435f;
        lowlandSettings.offset = -0.45f;
        lowlandSettings.yScale = 0f;
        return lowlandSettings;
    }
    public static TerrainNoiseSettings HighLandTerrainNoiseSettings(long _seed, boolean shouldCache) {
        TerrainNoiseSettings highLandSettings = new TerrainNoiseSettings(_seed);
        highLandSettings.fractalSettings.fractalType = ModuleFractal.FractalType.FBM;
        highLandSettings.fractalSettings.octaves = 4;
        highLandSettings.fractalSettings.frequency = 2f;
        highLandSettings.fractalSettings.shouldCache = shouldCache;
        highLandSettings.xzscale = .45f;
        highLandSettings.offset = 0f;
        return highLandSettings;
    }
    public static TerrainNoiseSettings MountainTerrainNoiseSettings(long _seed, boolean shouldCache) {
        TerrainNoiseSettings mountainSettings = new TerrainNoiseSettings(_seed);
        mountainSettings.fractalSettings.fractalType = ModuleFractal.FractalType.RIDGEMULTI;
        mountainSettings.fractalSettings.octaves = 8;
        mountainSettings.fractalSettings.frequency = 1f;
        mountainSettings.fractalSettings.shouldCache = shouldCache;
        mountainSettings.autoCorrectLow = -1;
        mountainSettings.xzscale = .45f;
        mountainSettings.offset = -.45f;
        mountainSettings.yScale = .8f;
        return mountainSettings;
    }
    public static TerrainNoiseSettings TerrainSettingsWithZeroOneModuleSelect(long _seed, Module _controlSource) {
        TerrainNoiseSettings selectorSettings = new TerrainNoiseSettings(_seed);
        selectorSettings.setUpModuleSelectSettings(_controlSource);
        return selectorSettings;
    }

    public static TerrainNoiseSettings TerrainTypeSelectModuleNoiseSettings(long _seed, boolean shouldCache) {
        TerrainNoiseSettings typeSelectSettings = new TerrainNoiseSettings(_seed);
        typeSelectSettings.fractalSettings.fractalType = ModuleFractal.FractalType.FBM;
        typeSelectSettings.fractalSettings.octaves = 4;
        typeSelectSettings.fractalSettings.frequency = 1f;
        typeSelectSettings.fractalSettings.shouldCache = shouldCache;
        typeSelectSettings.yScale = 1f;
        return typeSelectSettings;
    }
    public static TerrainNoiseSettings BlockTypeSelectModuleSettings(long _seed, Module _controlSource, BlockType a, BlockType b) {
        TerrainNoiseSettings blockSelect = new TerrainNoiseSettings(_seed);
        blockSelect.moduleSelectSettings = ModuleSelectSettings.BlockTypeSelectSettings(_controlSource, a, b);
        blockSelect.renderWithBlockColors = true;
        return blockSelect;
    }

    /*
     * TerrainNoiseSettings makes a module based on itself
     * */
    public Module makeTerrainModule(Module groundGradient) {
        // land_shape_fractal
        Module landShape = fractalSettings.makeFractalModule();

        ModuleAutoCorrect landShapeAutoCorrect = new ModuleAutoCorrect(autoCorrectLow, autoCorrectHigh);
        landShapeAutoCorrect.setSource(landShape);
        landShapeAutoCorrect.calculate();

        // land_scale
        ModuleScaleOffset landScale = new ModuleScaleOffset();
        landScale.setScale(xzscale);
        landScale.setOffset(offset);
        landScale.setSource(landShape);

        // land_y_scale
        ModuleScaleDomain landYScale = new ModuleScaleDomain();
        landYScale.setScaleY(yScale);
        landYScale.setScaleX(xScale); // should probably 1 for terrain.
        landYScale.setSource(landScale);

        if (groundGradient == null) {
            return  landYScale;
        }
        // terrain
        ModuleTranslateDomain landTerrain = new ModuleTranslateDomain();
        landTerrain.setAxisYSource(landYScale);
        landTerrain.setSource(groundGradient);

        return landTerrain;
    }


}
