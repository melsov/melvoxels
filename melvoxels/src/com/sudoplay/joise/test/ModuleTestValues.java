package com.sudoplay.joise.test;

import com.sudoplay.joise.ModuleInstanceMap;
import com.sudoplay.joise.ModuleMap;
import com.sudoplay.joise.ModulePropertyMap;
import com.sudoplay.joise.module.Module;

/**
 * Created by didyouloseyourdog on 4/19/15.
 */
public class ModuleTestValues extends Module {
    public double testValue = .8;
    @Override
    public double get(double x, double y) {
        return testValue;
    }

    @Override
    public double get(double x, double y, double z) {
        return testValue;
    }

    @Override
    public double get(double x, double y, double z, double w) {
        return testValue;
    }

    @Override
    public double get(double x, double y, double z, double w, double u, double v) {
        return testValue;
    }

    @Override
    protected void _writeToMap(ModuleMap map) {

    }

    @Override
    public Module buildFromPropertyMap(ModulePropertyMap props, ModuleInstanceMap map) {
        return null;
    }
}
