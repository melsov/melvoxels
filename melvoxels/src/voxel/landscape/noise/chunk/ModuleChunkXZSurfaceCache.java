package voxel.landscape.noise.chunk;

import com.sudoplay.joise.ModuleInstanceMap;
import com.sudoplay.joise.ModuleMap;
import com.sudoplay.joise.ModulePropertyMap;
import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.SourcedModule;

/**
 * Created by didyouloseyourdog on 3/31/15.
 * Cache's values for a 16x16 chunk surface
 * TODO: flesh out this strategy a little further: how to guarantee a coherent square of noise?
 */
public class ModuleChunkXZSurfaceCache extends SourcedModule {

    @Override
    public double get(double x, double y) {
        return 0;
    }

    @Override
    public double get(double x, double y, double z) {
        return 0;
    }

    @Override
    public double get(double x, double y, double z, double w) {
        return 0;
    }

    @Override
    public double get(double x, double y, double z, double w, double u, double v) {
        return 0;
    }

    @Override
    protected void _writeToMap(ModuleMap map) {

    }

    @Override
    public Module buildFromPropertyMap(ModulePropertyMap props, ModuleInstanceMap map) {
        return null;
    }
}
