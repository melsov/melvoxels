package voxel.landscape.noise;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleCombiner;
import com.sudoplay.joise.module.ModuleTranslateDomain;
import com.sudoplay.joise.module.ScalarParameter;

/**
 * Created by didyouloseyourdog on 10/19/14.
 */
public class CaveNoiseSettings
{
    public ModuleCombinerSettings caveShapeABSettings;
    public ScaleOffsetSettings abScaleOffsetSettings = new ScaleOffsetSettings();
    public Module perturbModule;
    public ScaleOffsetSettings perturbModuleScaleOffsetSettings = new ScaleOffsetSettings();
    public ModuleSelectSettings caveSelect;
    public long seed;

    public CaveNoiseSettings(long _seed) {
        seed = _seed;
    }

    public Module makeModule() {
        ModuleCombiner caveShapeAB = caveShapeABSettings.makeCombinerModule();
        abScaleOffsetSettings.sourceModule = caveShapeAB;
        abScaleOffsetSettings.scale = 1.4;
        abScaleOffsetSettings.offset = -.3;
        perturbModuleScaleOffsetSettings.sourceModule = perturbModule;
        // TODO: consider control cave scale with a gradient source for offset
        Module perturbOffset = perturbModuleScaleOffsetSettings.makeModule();

        ModuleTranslateDomain perturbed = new ModuleTranslateDomain();
        perturbed.setAxisXSource(perturbOffset);
        perturbed.setAxisZSource(perturbOffset);
        perturbed.setAxisYSource(perturbOffset);
        perturbed.setSource(abScaleOffsetSettings.makeModule());

        caveSelect.controlSource = perturbed;
        return caveSelect.makeSelectModule();

    }

    private Module getPerturbModule() {
        if (perturbModule == null) {
            FractalSettings perturbSettings = FractalSettings.FBMHighlandSettings(seed);
            perturbModule = perturbSettings.makeFractalModule();
        }
        return perturbModule;
    }
    public static CaveNoiseSettings CaveSettingsForTerrain(long _seed) {
        return CaveSettingsForTerrain(_seed, null);
    }
    public static CaveNoiseSettings CaveSettingsForTerrain(long _seed, Module _perturbModule) {
        CaveNoiseSettings caveNoiseSettings = new CaveNoiseSettings(_seed);
        FractalSettings caveASettings = FractalSettings.CaveRidgedMultiSettings(_seed);
        FractalSettings caveBSettings = FractalSettings.CaveRidgedMultiSettings(_seed + 1000);
        caveNoiseSettings.caveShapeABSettings = new ModuleCombinerSettings(caveASettings, caveBSettings);
        caveNoiseSettings.perturbModule = _perturbModule;
        caveNoiseSettings.caveSelect = new ModuleSelectSettings(caveNoiseSettings.getPerturbModule());
        caveNoiseSettings.caveSelect.threshold = new ScalarParameter(.5);
        caveNoiseSettings.caveSelect.highSource = new ScalarParameter(1);
        caveNoiseSettings.caveSelect.lowSource = new ScalarParameter(0);
        //TODO: tweak cave select settings
        return caveNoiseSettings;
    }
}
/*


ModuleFractal caveShape = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
        caveShape.setNumOctaves(1);
        caveShape.setFrequency(4);
        caveShape.setSeed(seed);

        ModuleFractal caveShape2 = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
        caveShape2.setNumOctaves(1);
        caveShape2.setFrequency(4);
        caveShape2.setSeed(seed + 1000);

        ModuleCombiner caveShape22 = new ModuleCombiner(CombinerType.MULT);
        caveShape22.setSource(0, caveShape2);
        caveShape22.setSource(1, caveShape2);

        // combined, 'pre-perturbed' cave shape
        ModuleCombiner caveShapeA = new ModuleCombiner(CombinerType.MULT);
        caveShapeA.setSource(0, caveShape);
        caveShapeA.setSource(1, caveShape22);

        ModuleCache caveShapeCache = new ModuleCache();
        caveShapeCache.setSource(caveShapeA); // use for terrain types as well


        int moduleSelect = 0;

        // cave_perturb_fractal
        ModuleFractal cavePerturbFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
        cavePerturbFractal.setNumOctaves(6);
        cavePerturbFractal.setFrequency(3);
        cavePerturbFractal.setSeed(seed); //CONSIDER: adjust seed??

        // cave_perturb_scale
        ModuleScaleOffset cavePerturbScale = new ModuleScaleOffset();
        cavePerturbScale.setScale(0.25);
        cavePerturbScale.setOffset(0);
        cavePerturbScale.setSource(cavePerturbFractal);

        // cave_perturb
        ModuleTranslateDomain cavePerturb = new ModuleTranslateDomain();
        cavePerturb.setAxisXSource(cavePerturbScale);
//        cavePerturb.setAxisZSource(cavePerturbScale);
//        cavePerturb.setAxisYSource(cavePerturbScale);
        cavePerturb.setSource(caveShapeA);


         reduce caves at lower Ys with gradient

        ModuleGradient caveDepthGradient = new ModuleGradient();
        caveDepthGradient.setGradient(0, 0, .85, 1);
        ModuleBias caveGradientBias = new ModuleBias();
        caveGradientBias.setSource(caveDepthGradient);
        caveGradientBias.setBias(.75);
        ModuleScaleOffset flipCaveDepthGradient = new ModuleScaleOffset();
        flipCaveDepthGradient.setScale(-3.5);
        flipCaveDepthGradient.setOffset(1.5);
        flipCaveDepthGradient.setSource(caveDepthGradient);

        ModuleCombiner minCombiner = new ModuleCombiner(CombinerType.MIN);
        minCombiner.setSource(0, 1);
        minCombiner.setSource(1, flipCaveDepthGradient);

        ModuleCombiner caveDepthCombiner = new ModuleCombiner(CombinerType.MULT);
        caveDepthCombiner.setSource(0, cavePerturb);
        caveDepthCombiner.setSource(1, minCombiner);

        // cave_select
        ModuleSelect caveSelect = new ModuleSelect();
        caveSelect.setLowSource(1);
        caveSelect.setHighSource(2);
        caveSelect.setControlSource(caveDepthCombiner);
        caveSelect.setThreshold(0.8);
        caveSelect.setFalloff(0);

        return caveSelect;
 */
