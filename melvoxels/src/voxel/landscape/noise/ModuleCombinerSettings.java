package voxel.landscape.noise;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleCombiner;

/**
 * Created by didyouloseyourdog on 10/3/14.
 */
public class ModuleCombinerSettings {
    FractalSettings fractalSettingsA;
    FractalSettings fractalSettingsB;
    ModuleCombiner.CombinerType combinerType = ModuleCombiner.CombinerType.MULT;

    public ModuleCombinerSettings(FractalSettings _fractalSettingsA, FractalSettings _fractalSettingsB) {
        this(_fractalSettingsA, _fractalSettingsB, ModuleCombiner.CombinerType.MULT);
    }
    public ModuleCombinerSettings(FractalSettings _fractalSettingsA, FractalSettings _fractalSettingsB, ModuleCombiner.CombinerType _combinerType) {
        fractalSettingsA = _fractalSettingsA;
        fractalSettingsB = _fractalSettingsB;
        combinerType = _combinerType;
    }

    public ModuleCombiner makeCombinerModule() {
        Module fractalA = fractalSettingsA.makeFractalModule();
        Module fractalB = fractalSettingsB.makeFractalModule();
        ModuleCombiner combiner = new ModuleCombiner(combinerType);
        combiner.setSource(0, fractalA);
        combiner.setSource(1, fractalB);
        return combiner;
    }
}
