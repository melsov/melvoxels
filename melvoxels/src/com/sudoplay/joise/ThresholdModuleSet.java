package com.sudoplay.joise;

import com.sudoplay.joise.module.ScalarParameter;

/**
 * Created by didyouloseyourdog on 4/18/15.
 */
public class ThresholdModuleSet {
    public final ScalarParameter scalarParameter;
    public final double threshold;
    public ThresholdModuleSet(ScalarParameter scalarParameter, double threshold) {
        this.scalarParameter = scalarParameter;
        this.threshold = threshold;
    }
    @Override
    public String toString() {
        return String.format("ThresholdModuleSet: %s : threshold: %d ", scalarParameter.toString(), threshold);
    }
}
