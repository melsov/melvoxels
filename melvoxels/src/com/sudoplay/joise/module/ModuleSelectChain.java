package com.sudoplay.joise.module;

import com.sudoplay.joise.ModuleInstanceMap;
import com.sudoplay.joise.ModuleMap;
import com.sudoplay.joise.ModulePropertyMap;
import com.sudoplay.joise.ThresholdModuleSet;
import voxel.landscape.util.Asserter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by didyouloseyourdog on 4/18/15.
 */

public class ModuleSelectChain extends Module {
    private Module module;

    public void setChain(Module source, ThresholdModuleSet[] _thresholdModuleSets) {
        Asserter.assertTrue(_thresholdModuleSets.length > 1, "I need some modules");
        List<ThresholdModuleSet> thresholdModuleSets =  descendingOrderSets(_thresholdModuleSets);

        ModuleSelect result = null;
        ModuleCache moduleCache = new ModuleCache();
        moduleCache.setSource(source);
        while (true) {
            ThresholdModuleSet thresholdModuleSet = thresholdModuleSets.remove(0);
            ModuleSelect moduleSelect = new ModuleSelect();
            if (result == null){
                moduleSelect.setHighSource(thresholdModuleSet.scalarParameter.getValue());
            } else {
                result.setLowSource(thresholdModuleSet.scalarParameter.getValue());
                if (thresholdModuleSets.size() == 0) {
                    module = result;
                    return;
                }
                moduleSelect.setHighSource(result);
            }
            moduleSelect.setThreshold(thresholdModuleSet.threshold);
            moduleSelect.setControlSource(moduleCache);
            result = moduleSelect;
        }
    }
    private List<ThresholdModuleSet> descendingOrderSets(ThresholdModuleSet[] thresholdModuleSets) {

        Arrays.sort(thresholdModuleSets, new Comparator<ThresholdModuleSet>() {
            @Override
            public int compare(ThresholdModuleSet o, ThresholdModuleSet o1) {
                ThresholdModuleSet setA = (ThresholdModuleSet) o;
                ThresholdModuleSet setB = (ThresholdModuleSet) o1;
                if (setA.threshold > setB.threshold) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        List result = new ArrayList<>(thresholdModuleSets.length);
        Collections.addAll(result, thresholdModuleSets);
        return result;
    }

    @Override
    public double get(double x, double y) {
        return module.get(x,y);
    }

    @Override
    public double get(double x, double y, double z) {
        return module.get(x,y,z);
    }

    @Override
    public double get(double x, double y, double z, double w) {
        return module.get(x,y,z,w);
    }

    @Override
    public double get(double x, double y, double z, double w, double u, double v) {
        return module.get(x,y,z,w,u,v);
    }

    @Override
    protected void _writeToMap(ModuleMap map) {
        try {
            throw new Exception("this module doesn't know how to write property maps");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Module buildFromPropertyMap(ModulePropertyMap props, ModuleInstanceMap map) {
        try {
            throw new Exception("this module doesn't know how to read property maps");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
