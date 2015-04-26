package voxel.landscape.noise;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleScaleOffset;

/**
 * Created by didyouloseyourdog on 10/19/14.
 */
public class ScaleOffsetSettings {
    public double scale = 1d;
    public Module scaleModule;
    public double offset = 0d;
    public Module offsetModule;
    public Module sourceModule;

    public Module makeModule() {
        ModuleScaleOffset scaleOffset = new ModuleScaleOffset();
        if (scaleModule!=null) scaleOffset.setScale(scaleModule);
        else scaleOffset.setScale(scale);
        if (offsetModule!=null) scaleOffset.setOffset(offsetModule);
        else scaleOffset.setOffset(offset);
        scaleOffset.setSource(sourceModule);
        return scaleOffset;
    }
}
