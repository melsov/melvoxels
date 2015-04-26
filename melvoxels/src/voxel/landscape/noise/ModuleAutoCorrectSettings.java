package voxel.landscape.noise;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleAutoCorrect;

/**
 * Created by didyouloseyourdog on 10/19/14.
 */
public class ModuleAutoCorrectSettings {
    public double low = 0, high = 1;
    public Module source;

    public ModuleAutoCorrectSettings(Module _source) {
        source = _source;
    }

    public Module makeModule() {
        ModuleAutoCorrect autoCorrect = new ModuleAutoCorrect(low, high);
        autoCorrect.setSource(source);
        autoCorrect.calculate();
        return autoCorrect;
    }

}
