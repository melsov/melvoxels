package com.sudoplay.joise.examples;

import com.sudoplay.joise.ThresholdModuleSet;
import com.sudoplay.joise.module.*;
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType;
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType;
import com.sudoplay.joise.module.ModuleFractal.FractalType;
import com.sudoplay.joise.test.ModuleTestValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

/**
 * This example is derived from:
 * http://accidentalnoise.sourceforge.net/minecraftworlds.html
 * <p/>
 * This will get you started on the path to creating Terraria-style 2D noise for
 * use in a game. For an in-depth explanation of how the functions affect the
 * output, study the information located at the above link.
 * <p/>
 * You can click in the window that pops up to re-generate new noise.
 * <p/>
 * 'It all just comes down to thinking about what you want, understanding what
 * effect the various functions will have on the output, and experimenting with
 * the various parameters until you get something you like. It's not a perfect
 * science, and there are often many ways you can accomplish any given effect.'
 * -Joshua Tippetts
 *
 * @author Jason Taylor
 */
public class Example_02_MP {

    private int numberOfStages = 4;

    public static void main(String[] args) {

        int width = 400;
        int height = 300;

        JFrame frame = new JFrame("Joise Example 021");
        frame.setPreferredSize(new Dimension(width, height));

        final Canvas canvas = new Canvas(width, height);
        frame.add(canvas);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent arg0) {
                //
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                //
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                //
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                //
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                updateImage(canvas);
            }
        });

        updateImage(canvas);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private static void updateImage(Canvas canvas) {
        // ========================================================================
        // = Joise module chain
        // ========================================================================



        Random random = new Random();
        long seed = 1236; // random.nextLong();

        /*
         * ground_gradient
         */
        // ground_gradient
        ModuleGradient groundGradient = new ModuleGradient();
        groundGradient.setGradient(0, 0, 0, 1, 0, 0);

        ModuleCellGen moduleCellGen = new ModuleCellGen();
        moduleCellGen.setSeed(seed);
        ModuleCellular moduleCellular = new ModuleCellular();
        moduleCellular.setCoefficients(12, .4, 0, 0);
        moduleCellular.setCellularSource(moduleCellGen);

        ModuleScaleOffset moduleScaleOffset = new ModuleScaleOffset();
        moduleScaleOffset.setScale(1);
        moduleScaleOffset.setOffset(0d);
        moduleScaleOffset.setSource(moduleCellular);

        ModuleScaleDomain moduleScaleDomain = new ModuleScaleDomain();
        moduleScaleDomain.setScaleX(1);
        moduleScaleDomain.setScaleY(1);
        moduleScaleDomain.setSource(moduleScaleOffset);

//        canvas.updateImage(moduleScaleDomain);
//        if (true) return;
        ModuleTestValues moduleTestValues = new ModuleTestValues();
        moduleTestValues.testValue = .21d;
        ModuleSelectChain moduleSelectChain = new ModuleSelectChain();
        ThresholdModuleSet[] treeTypes = new ThresholdModuleSet[] {
                new ThresholdModuleSet(new ScalarParameter(0d), .4),
                new ThresholdModuleSet(new ScalarParameter(3.1d), .14),
                new ThresholdModuleSet(new ScalarParameter(2.1d), .18),
                new ThresholdModuleSet(new ScalarParameter(1.1d), .2),
                new ThresholdModuleSet(new ScalarParameter(4.1d), .05),
        };
        moduleSelectChain.setChain(moduleScaleDomain, treeTypes);

        ModuleSelect moduleSelect = new ModuleSelect();
        moduleSelect.setHighSource(0d);
        moduleSelect.setLowSource(moduleSelectChain);
        moduleSelect.setThreshold(.4d);
        moduleSelect.setControlSource(moduleScaleDomain); // should be cached

//        ModuleCombiner moduleCombiner = new ModuleCombiner(ModuleCombiner.CombinerType.MULT);
//        moduleCombiner.setSource(0, moduleSelect);
//        moduleCombiner.setSource(1, moduleSelectChain);

        canvas.updateImage(moduleSelectChain);
        if (true) return;

        /*
         * lowland
         */
        // lowland_shape_fractal
        ModuleFractal lowlandShapeFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
        lowlandShapeFractal.setNumOctaves(2);
        lowlandShapeFractal.setFrequency(.0325);
        lowlandShapeFractal.setSeed(seed);

        // lowland_autocorrect
        ModuleAutoCorrect lowlandAutoCorrect = new ModuleAutoCorrect(0, 1);
        lowlandAutoCorrect.setSource(lowlandShapeFractal);
        lowlandAutoCorrect.calculate();



        ModuleTranslateDomain moduleTranslateDomain = new ModuleTranslateDomain();
//        moduleTranslateDomain.setAxisXSource(lowlandScale);
//        moduleTranslateDomain.setAxisYSource(lowlandScale);
        moduleTranslateDomain.setAxisXSource(.002);
        moduleTranslateDomain.setAxisYSource(.002);
        moduleTranslateDomain.setSource(moduleCellular);

        canvas.updateImage(moduleTranslateDomain);
        if (true) return;

        // lowland_scale
        ModuleScaleOffset lowlandScale = new ModuleScaleOffset();
        lowlandScale.setScale(2);
        lowlandScale.setOffset(123);
        lowlandScale.setSource(moduleCellular);


        // lowland_y_scale
        ModuleScaleDomain lowlandYScale = new ModuleScaleDomain();
//        lowlandYScale.setScaleY(0);
        lowlandYScale.setScaleY(1);
        lowlandYScale.setSource(lowlandScale);

        // lowland_terrain
        ModuleTranslateDomain lowlandTerrain = new ModuleTranslateDomain();
        lowlandTerrain.setAxisYSource(lowlandYScale);
        lowlandTerrain.setSource(groundGradient);

        canvas.updateImage(lowlandAutoCorrect);

        /*
         * terrain
         */
        // terrain_type_fractal
        ModuleFractal terrainTypeFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
        terrainTypeFractal.setNumOctaves(3);
        terrainTypeFractal.setFrequency(0.125);
        terrainTypeFractal.setSeed(seed);

        // terrain_autocorrect
        ModuleAutoCorrect terrainAutoCorrect = new ModuleAutoCorrect(0, 1);
        terrainAutoCorrect.setSource(terrainTypeFractal);
        terrainAutoCorrect.calculate();

        // terrain_type_y_scale
        ModuleScaleDomain terrainTypeYScale = new ModuleScaleDomain();
        terrainTypeYScale.setScaleY(0);
        terrainTypeYScale.setSource(terrainAutoCorrect);

        // terrain_type_cache
        ModuleCache terrainTypeCache = new ModuleCache();
        terrainTypeCache.setSource(terrainTypeYScale);
//
//    // highland_mountain_select
//    ModuleSelect highlandMountainSelect = new ModuleSelect();
//    highlandMountainSelect.setLowSource(highlandTerrain);
//    highlandMountainSelect.setHighSource(mountainTerrain);
//    highlandMountainSelect.setControlSource(terrainTypeCache);
//    highlandMountainSelect.setThreshold(0.65);
//    highlandMountainSelect.setFalloff(0.2);
//
//    // highland_lowland_select
//    ModuleSelect highlandLowlandSelect = new ModuleSelect();
//    highlandLowlandSelect.setLowSource(lowlandTerrain);
//    highlandLowlandSelect.setHighSource(highlandMountainSelect);
//    highlandLowlandSelect.setControlSource(terrainTypeCache);
//    highlandLowlandSelect.setThreshold(0.25);
//    highlandLowlandSelect.setFalloff(0.15);

        // highland_lowland_select_cache
        ModuleCache highlandLowlandSelectCache = new ModuleCache();
        highlandLowlandSelectCache.setSource(lowlandTerrain);

        // ground_select
        ModuleSelect groundSelect = new ModuleSelect();
        groundSelect.setLowSource(0);
        groundSelect.setHighSource(1);
        groundSelect.setThreshold(0.1);
        groundSelect.setControlSource(highlandLowlandSelectCache);

//        canvas.updateImage(groundSelect);
    /*
     * cave
     */
 /*
    // cave_shape
    ModuleFractal caveShape = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
    caveShape.setNumOctaves(1);
    caveShape.setFrequency(8);
    caveShape.setSeed(seed);

    // cave_attenuate_bias
    ModuleBias caveAttenuateBias = new ModuleBias(0.825);
    caveAttenuateBias.setSource(highlandLowlandSelectCache);

    // cave_shape_attenuate
    ModuleCombiner caveShapeAttenuate = new ModuleCombiner(CombinerType.MULT);
    caveShapeAttenuate.setSource(0, caveShape);
    caveShapeAttenuate.setSource(1, caveAttenuateBias);

    // cave_perturb_fractal
    ModuleFractal cavePerturbFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
    cavePerturbFractal.setNumOctaves(6);
    cavePerturbFractal.setFrequency(3);
    cavePerturbFractal.setSeed(seed);

    // cave_perturb_scale
    ModuleScaleOffset cavePerturbScale = new ModuleScaleOffset();
    cavePerturbScale.setScale(0.25);
    cavePerturbScale.setOffset(0);
    cavePerturbScale.setSource(cavePerturbFractal);

    // cave_perturb
    ModuleTranslateDomain cavePerturb = new ModuleTranslateDomain();
    cavePerturb.setAxisXSource(cavePerturbScale);
    cavePerturb.setSource(caveShapeAttenuate);

    // cave_select
    ModuleSelect caveSelect = new ModuleSelect();
    caveSelect.setLowSource(1);
    caveSelect.setHighSource(0);
    caveSelect.setControlSource(cavePerturb);
    caveSelect.setThreshold(0.8);
    caveSelect.setFalloff(0);
      */

    /*
     * final
     */
/*
    // ground_cave_multiply
    ModuleCombiner groundCaveMultiply = new ModuleCombiner(CombinerType.MULT);
    groundCaveMultiply.setSource(0, caveSelect);
    groundCaveMultiply.setSource(1, groundSelect);
*/
    /*
     * Draw it
     */

//    canvas.updateImage(groundCaveMultiply);

    }

}
