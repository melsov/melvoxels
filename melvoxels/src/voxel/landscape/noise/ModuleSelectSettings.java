package voxel.landscape.noise;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleSelect;
import com.sudoplay.joise.module.ScalarParameter;
import voxel.landscape.BlockType;

import java.util.EnumSet;
import java.util.Map;

/**
 * Created by didyouloseyourdog on 9/27/14.
 */
public class ModuleSelectSettings {
    public ScalarParameter lowSource = new ScalarParameter(0d);
    public ScalarParameter highSource = new ScalarParameter(1d);
    public Module controlSource;
    public ScalarParameter threshold = new ScalarParameter(.5d);
    public ScalarParameter falloff = new ScalarParameter(0d);

    public ModuleSelectSettings(Module controlSource_) { controlSource = controlSource_; }

    public static ModuleSelectSettings BlockTypeSelectSettingsManualThreshold (Module controlSource_, BlockType lowSource, BlockType highSource, double _threshold ) {
        ModuleSelectSettings res = new ModuleSelectSettings(controlSource_);
        res.threshold = new ScalarParameter( _threshold);
        res.lowSource = new ScalarParameter(lowSource.ordinal());
        res.highSource = new ScalarParameter( highSource.ordinal() );
        return res;
    }

    public static ModuleSelectSettings BlockTypeSelectSettings (Module controlSource_, BlockType a, BlockType b ) {
        ModuleSelectSettings res = new ModuleSelectSettings(controlSource_);
        Map<BlockType, Double> prevalenceLookUp = BlockType.prevalenceTable(EnumSet.of(a, b));
        BlockType lowerType, higherType;
        if (prevalenceLookUp.get(a) < prevalenceLookUp.get(b) ) {
            lowerType = a; higherType = b;
        } else { lowerType = b; higherType = a; }
        res.threshold = new ScalarParameter( prevalenceLookUp.get(lowerType));
        res.lowSource = new ScalarParameter(lowerType.ordinal());
        res.highSource = new ScalarParameter( higherType.ordinal() );
        return res;
    }

    public Module makeSelectModule() {
        ModuleSelect selectModule = new ModuleSelect();

        if (lowSource.isModule()) selectModule.setLowSource(lowSource.getModule());
        else selectModule.setLowSource(lowSource.getValue());

        if (highSource.isModule()) selectModule.setHighSource(highSource.getModule());
        else selectModule.setHighSource(highSource.getValue());

        selectModule.setControlSource(controlSource);

        if (threshold.isModule()) selectModule.setThreshold(threshold.getModule());
        else selectModule.setThreshold(threshold.getValue());

        if (falloff.isModule()) selectModule.setFalloff(falloff.getModule());
        else selectModule.setFalloff(falloff.getValue());
        return selectModule;
    }
}