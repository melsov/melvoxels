package com.sudoplay.joise.examples;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JFrame;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleAutoCorrect;
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType;
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType;
import com.sudoplay.joise.module.ModuleBias;
import com.sudoplay.joise.module.ModuleCache;
import com.sudoplay.joise.module.ModuleCombiner;
import com.sudoplay.joise.module.ModuleCombiner.CombinerType;
import com.sudoplay.joise.module.ModuleFractal;
import com.sudoplay.joise.module.ModuleFractal.FractalType;
import com.sudoplay.joise.module.ModuleGradient;
import com.sudoplay.joise.module.ModuleScaleDomain;
import com.sudoplay.joise.module.ModuleScaleOffset;
import com.sudoplay.joise.module.ModuleSelect;
import com.sudoplay.joise.module.ModuleTranslateDomain;

/**
 * This example is derived from:
 * http://accidentalnoise.sourceforge.net/minecraftworlds.html
 * <p>
 * This will get you started on the path to creating Terraria-style 2D noise for
 * use in a game. For an in-depth explanation of how the functions affect the
 * output, study the information located at the above link.
 * <p>
 * You can click in the window that pops up to re-generate new noise.
 * <p>
 * 'It all just comes down to thinking about what you want, understanding what
 * effect the various functions will have on the output, and experimenting with
 * the various parameters until you get something you like. It's not a perfect
 * science, and there are often many ways you can accomplish any given effect.'
 * -Joshua Tippetts
 * 
 * @author Jason Taylor
 * 
 */
public class Example_04 {

	static int caveModuleLevel = 0;
	static boolean ModuleSelectModeIsOn = true;
	static long seed;
	
	static double test_variable = 0.0;
	public static boolean fakeTrue() {return true; }
  public static void main(String[] args) {
    int width = 400;
    int height = 400;
    
    Random random = new Random();
    seed = random.nextLong();

    JFrame frame = new JFrame("Joise Example 02");
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
        if (ModuleSelectModeIsOn) {
	        caveModuleLevel++;
        } else {
        	if (arg0.getY() > 200) {
        		test_variable -= .1;
        	} else {
	        	test_variable += .1;
        	}
        	System.out.println(test_variable);
        }
      }
    });

    updateImage(canvas);
//    caveExperi(canvas);
//    updateImageTwo(canvas);

    frame.pack();
    frame.setLocationRelativeTo(null);
  }
  private static boolean updateCanvasReturnTrue(Module mod, Canvas can) {
	  can.updateImage(mod);
	  return true;
  }
  private static Module caveExperi(Module caveShapeA) {
	  int moduleSelect = 0;
	  // cave_shape


	    // cave_perturb_fractal
	    ModuleFractal cavePerturbFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
	    cavePerturbFractal.setNumOctaves(6);
	    cavePerturbFractal.setFrequency(3);
	    cavePerturbFractal.setSeed(seed);

	    // cave_perturb_scale
	    ModuleScaleOffset cavePerturbScale = new ModuleScaleOffset();
	    cavePerturbScale.setScale(0.25);
	    cavePerturbScale.setOffset(0); // 0 was orig val
	    cavePerturbScale.setSource(cavePerturbFractal);

	    // cave_perturb
	    ModuleTranslateDomain cavePerturb = new ModuleTranslateDomain();
	    cavePerturb.setAxisXSource(cavePerturbScale);
	    cavePerturb.setSource(caveShapeA);
	    
	    /*
	     * reduce caves at low Y with gradient
	     */
	    ModuleGradient caveDepthGradient = new ModuleGradient();
	    caveDepthGradient.setGradient(0,0,.85,1);
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
	    caveSelect.setHighSource(0);
//	    caveSelect.setControlSource(cavePerturb);
	    caveSelect.setControlSource(caveDepthCombiner);
	    caveSelect.setThreshold(0.8);
	    caveSelect.setFalloff(0);
	    
	    return caveSelect;
//	    canvas.updateImage(caveSelect);
//	    canvas.updateImage(caveDepthCombiner);
//	    canvas.updateImage(caveGradientBias);
//	    canvas.updateImage(minCombiner);
//	    canvas.updateImage(flipCaveDepthGradient);
//	    canvas.updateImage(caveShapeAttenuate);
//	    canvas.updateImage(caveAttenuateBias);
  }
  private static void updateImageTwo(Canvas canvas) {
	  ModuleGradient groundGradient = new ModuleGradient();
	    groundGradient.setGradient(0, 0, 0, 1);
	    
//	    if (updateCanvasReturnTrue(groundGradient, canvas)) return;
	    
	 // lowland_shape_fractal
	    ModuleFractal lowlandShapeFractal = new ModuleFractal(FractalType.BILLOW, BasisType.GRADIENT, InterpolationType.QUINTIC);
	    lowlandShapeFractal.setNumOctaves(2);
	    lowlandShapeFractal.setFrequency(1.25);
	    lowlandShapeFractal.setSeed(seed);
	    

	    // lowland_autocorrect
	    ModuleAutoCorrect lowlandAutoCorrect = new ModuleAutoCorrect(0, 1);
	    lowlandAutoCorrect.setSource(lowlandShapeFractal);
	    lowlandAutoCorrect.calculate();
	    
	    // lowland_scale
	    ModuleScaleOffset lowlandScale = new ModuleScaleOffset();
	    lowlandScale.setScale(0.05);
//	    lowlandScale.setOffset(-0.15);
	    lowlandScale.setSource(lowlandAutoCorrect);
	    
//	    if (updateCanvasReturnTrue(lowlandScale, canvas)) return;
	    // lowland_y_scale
	    ModuleScaleDomain lowlandYScale = new ModuleScaleDomain();
	    lowlandYScale.setScaleY(1.0);
	    lowlandYScale.setSource(lowlandScale);

	    // lowland_terrain
	    ModuleTranslateDomain lowlandTerrain = new ModuleTranslateDomain();
	    lowlandTerrain.setAxisYSource(lowlandYScale);
//	    lowlandTerrain.setAxisXSource(lowlandYScale);
	    lowlandTerrain.setSource(groundGradient);
	    
	    
	    ModuleSelect caveSelect = new ModuleSelect();
	    caveSelect.setLowSource(1);
	    caveSelect.setHighSource(0);
	    caveSelect.setControlSource(lowlandTerrain);
	    caveSelect.setThreshold(0.5);
	    caveSelect.setFalloff(0);
	    
//	    canvas.updateImage(groundGradient);
	    canvas.updateImage(caveSelect);
	    
  }

  private static void updateImage(Canvas canvas) {
    // ========================================================================
    // = Joise module chain
    // ========================================================================
	  int moduleSelect = 0; //MMP
    
    /*
     * ground_gradient
     */

    // ground_gradient
    ModuleGradient groundGradient = new ModuleGradient();
    groundGradient.setGradient(0, 0, 0, 1);

    /*
     * lowland
     */
    
    // lowland_shape_fractal
    ModuleFractal lowlandShapeFractal = new ModuleFractal(FractalType.BILLOW, BasisType.GRADIENT, InterpolationType.QUINTIC);
    lowlandShapeFractal.setNumOctaves(2);
    lowlandShapeFractal.setFrequency(0.25);
    lowlandShapeFractal.setSeed(seed);
    
    if (updateCanvasSelect(canvas, moduleSelect++, lowlandShapeFractal, "lowlandShapeFractal")) { return; }

    // lowland_autocorrect
    ModuleAutoCorrect lowlandAutoCorrect = new ModuleAutoCorrect(0, 1);
    lowlandAutoCorrect.setSource(lowlandShapeFractal);
    lowlandAutoCorrect.calculate();

    if (updateCanvasSelect(canvas, moduleSelect++, lowlandAutoCorrect, "lowlandAutoCorrect")) { return; }
    
    // lowland_scale
    ModuleScaleOffset lowlandScale = new ModuleScaleOffset();
    lowlandScale.setScale(0.125);
    lowlandScale.setOffset(-0.45);
    lowlandScale.setSource(lowlandAutoCorrect);
    
    if (updateCanvasSelect(canvas, moduleSelect++, lowlandScale, "lowlandScale")) { return; }

    // lowland_y_scale
    ModuleScaleDomain lowlandYScale = new ModuleScaleDomain();
    lowlandYScale.setScaleY(0);
    lowlandYScale.setSource(lowlandScale);
    
    if (updateCanvasSelect(canvas, moduleSelect++, lowlandYScale, "lowlandYScale")) { return; }

    // lowland_terrain
    ModuleTranslateDomain lowlandTerrain = new ModuleTranslateDomain();
    lowlandTerrain.setAxisYSource(lowlandYScale);
    lowlandTerrain.setSource(groundGradient);
    
    if (updateCanvasSelect(canvas, moduleSelect++, lowlandTerrain, "lowlandTerrain")) { return; }

    /*
     * highland
     */

    // highland_shape_fractal
    ModuleFractal highlandShapeFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
    highlandShapeFractal.setNumOctaves(4);
    highlandShapeFractal.setFrequency(2);
    highlandShapeFractal.setSeed(seed);

    // highland_autocorrect
    ModuleAutoCorrect highlandAutoCorrect = new ModuleAutoCorrect(-1, 1);
    highlandAutoCorrect.setSource(highlandShapeFractal);
    highlandAutoCorrect.calculate();

    // highland_scale
    ModuleScaleOffset highlandScale = new ModuleScaleOffset();
    highlandScale.setScale(0.25);
    highlandScale.setOffset(0);
    highlandScale.setSource(highlandAutoCorrect);

    // highland_y_scale
    ModuleScaleDomain highlandYScale = new ModuleScaleDomain();
    highlandYScale.setScaleY(0);
    highlandYScale.setSource(highlandScale);

    // highland_terrain
    ModuleTranslateDomain highlandTerrain = new ModuleTranslateDomain();
    highlandTerrain.setAxisYSource(highlandYScale);
    highlandTerrain.setSource(groundGradient);
    
    /*
     * mountain
     */

    // mountain_shape_fractal
    ModuleFractal mountainShapeFractal = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
    mountainShapeFractal.setNumOctaves(8);
    mountainShapeFractal.setFrequency(1);
    mountainShapeFractal.setSeed(seed);
    /*
     * MMP: cache for bedrock
     */
    ModuleCache mp_mountainCache = new ModuleCache();
    mp_mountainCache.setSource(mountainShapeFractal);

    // mountain_autocorrect
    ModuleAutoCorrect mountainAutoCorrect = new ModuleAutoCorrect(-1, 1);
    mountainAutoCorrect.setSource(mountainShapeFractal);
    mountainAutoCorrect.setSource(mp_mountainCache);
    mountainAutoCorrect.calculate();
    

    // mountain_scale
    ModuleScaleOffset mountainScale = new ModuleScaleOffset();
    mountainScale.setScale(0.45);
    mountainScale.setOffset(0.15);
    mountainScale.setSource(mountainAutoCorrect);

    // mountain_y_scale
    ModuleScaleDomain mountainYScale = new ModuleScaleDomain();
    mountainYScale.setScaleY(0.1);
    mountainYScale.setSource(mountainScale);

    // mountain_terrain
    ModuleTranslateDomain mountainTerrain = new ModuleTranslateDomain();
    mountainTerrain.setAxisYSource(mountainYScale);
    mountainTerrain.setSource(groundGradient);

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

    // highland_mountain_select
    ModuleSelect highlandMountainSelect = new ModuleSelect();
    highlandMountainSelect.setLowSource(highlandTerrain);
    highlandMountainSelect.setHighSource(mountainTerrain);
    highlandMountainSelect.setControlSource(terrainTypeCache);
    highlandMountainSelect.setThreshold(0.65);
    highlandMountainSelect.setFalloff(0.2);

    // highland_lowland_select
    ModuleSelect highlandLowlandSelect = new ModuleSelect();
    highlandLowlandSelect.setLowSource(lowlandTerrain);
    highlandLowlandSelect.setHighSource(highlandMountainSelect);
    highlandLowlandSelect.setControlSource(terrainTypeCache);
    highlandLowlandSelect.setThreshold(0.25);
    highlandLowlandSelect.setFalloff(0.15);

    // highland_lowland_select_cache
    ModuleCache highlandLowlandSelectCache = new ModuleCache();
    highlandLowlandSelectCache.setSource(highlandLowlandSelect);

    // ground_select
    ModuleSelect groundSelect = new ModuleSelect();
    groundSelect.setLowSource(0);
    groundSelect.setHighSource(1);
    groundSelect.setThreshold(0.5);
    groundSelect.setControlSource(highlandLowlandSelectCache);
    
//    ModuleCache groundSelectCache = new ModuleCache();
//    groundSelectCache.setSource(groundSelect);

    /*
     * cave
     * TODO: caves by intersecting two RMFractals
     */
   
    // cave_shape
//    ModuleFractal caveShape = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
//    caveShape.setNumOctaves(1);
//    caveShape.setFrequency(8);
//    caveShape.setSeed(seed);
//
//    if (updateCanvasSelect(canvas, moduleSelect++, caveShape, "cave shape")) { return; }
//
//    // cave_attenuate_bias
//    ModuleBias caveAttenuateBias = new ModuleBias(0.825);
////    ModuleBias caveAttenuateBias = new ModuleBias(test_variable);
//    caveAttenuateBias.setSource(highlandLowlandSelectCache);
//
//    // cave_shape_attenuate
//    ModuleCombiner caveShapeAttenuate = new ModuleCombiner(CombinerType.MULT);
//    caveShapeAttenuate.setSource(0, caveShape);
//    caveShapeAttenuate.setSource(1, caveAttenuateBias);
//
//    if (updateCanvasSelect(canvas, moduleSelect++, caveShapeAttenuate, "cave shape attenuate")) { return; }
//
//    // cave_perturb_fractal
//    ModuleFractal cavePerturbFractal = new ModuleFractal(FractalType.FBM, BasisType.GRADIENT, InterpolationType.QUINTIC);
//    cavePerturbFractal.setNumOctaves(6);
//    cavePerturbFractal.setFrequency(3);
//    cavePerturbFractal.setSeed(seed);
//    
//    if (updateCanvasSelect(canvas, moduleSelect++, cavePerturbFractal, "cave perturb fractal")) { return; }
//
//    // cave_perturb_scale
//    ModuleScaleOffset cavePerturbScale = new ModuleScaleOffset();
//    cavePerturbScale.setScale(0.25);
//    cavePerturbScale.setOffset(0); // 0 was orig val
//    cavePerturbScale.setSource(cavePerturbFractal);
//    
//    if (updateCanvasSelect(canvas, moduleSelect++, cavePerturbScale, "cave perturb scale")) { return; }
//
//    // cave_perturb
//    ModuleTranslateDomain cavePerturb = new ModuleTranslateDomain();
//    cavePerturb.setAxisXSource(cavePerturbScale);
//    cavePerturb.setSource(caveShapeAttenuate);
//    
//    
//    if (updateCanvasSelect(canvas, moduleSelect++, cavePerturb, "cave perturb")) { return; }
//
//    // cave_select
//    ModuleSelect caveSelect = new ModuleSelect();
//    caveSelect.setLowSource(1);
//    caveSelect.setHighSource(0);
//    caveSelect.setControlSource(cavePerturb);
//    caveSelect.setThreshold(0.8);
//    caveSelect.setFalloff(0);
    
    /*
     * Cave modules
     */
    ModuleFractal caveShape = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
    caveShape.setNumOctaves(1);
    caveShape.setFrequency(4);
    caveShape.setSeed(seed);

    ModuleFractal caveShape2 = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
    caveShape2.setNumOctaves(1);
    caveShape2.setFrequency(4);
    caveShape2.setSeed(seed+1000);
    
    ModuleCombiner caveShape22 = new ModuleCombiner(CombinerType.MULT);
    caveShape22.setSource(0, caveShape2);
    caveShape22.setSource(1, caveShape2);
    
    // combined, 'pre-perturbed' cave shape
    ModuleCombiner caveShapeA = new ModuleCombiner(CombinerType.MULT);
    caveShapeA.setSource(0, caveShape);
    caveShapeA.setSource(1, caveShape22);
    
    ModuleCache caveShapeCache = new ModuleCache();
    caveShapeCache.setSource(caveShapeA); // use for terrain types as well 
    
    Module mp_caveModule = caveExperi(caveShapeCache);

    /*
     * Terrain Type
     */
    ModuleFractal terrainTypeHelperModule = new ModuleFractal(FractalType.RIDGEMULTI, BasisType.GRADIENT, InterpolationType.QUINTIC);
    terrainTypeHelperModule.setNumOctaves(1);
    terrainTypeHelperModule.setFrequency(2); //high for testing
    terrainTypeHelperModule.setSeed(seed);
    
    ModuleAutoCorrect terrAutoCorrect = new ModuleAutoCorrect(0, 1);
    terrAutoCorrect.setSource(terrainTypeHelperModule);
    terrAutoCorrect.calculate();

    // lowland_scale
    ModuleScaleOffset terrScaleOffset = new ModuleScaleOffset();
    terrScaleOffset.setScale(.5);
    terrScaleOffset.setOffset(-0.1);
    terrScaleOffset.setSource(terrAutoCorrect);
    
    ModuleScaleOffset caveTampDown = new ModuleScaleOffset();
    caveTampDown.setScale(.2);
    caveTampDown.setSource(caveShapeCache);
    
    ModuleScaleOffset terrOffByCaveFrac = new ModuleScaleOffset();
    terrOffByCaveFrac.setOffset(caveTampDown);
    terrOffByCaveFrac.setSource(terrScaleOffset);
    
    // lowland_y_scale
    ModuleScaleDomain terrScaleYDomain = new ModuleScaleDomain();
    terrScaleYDomain.setScaleY(.2);
    terrScaleYDomain.setSource(terrOffByCaveFrac);
    
//    // sand or grass ?
    ModuleCombiner terrTypePlusMountainsNoise = new ModuleCombiner(CombinerType.ADD);
    terrTypePlusMountainsNoise.setSource(0, terrainTypeCache);
    terrTypePlusMountainsNoise.setSource(1, mp_mountainCache); //ever useful
    
    ModuleScaleOffset scaleTerrMountain = new ModuleScaleOffset();
    scaleTerrMountain.setScale(.5);
    scaleTerrMountain.setSource(terrTypePlusMountainsNoise);
    
    ModuleSelect terrainSelect = new ModuleSelect();
    terrainSelect.setLowSource(3.5);
    terrainSelect.setHighSource(4.5);
    terrainSelect.setControlSource(scaleTerrMountain);
    terrainSelect.setThreshold(.9);
    terrainSelect.setFalloff(0);
    
    ModuleTranslateDomain strataGradientPerturb = new ModuleTranslateDomain();
    strataGradientPerturb.setAxisYSource(terrScaleYDomain);
    strataGradientPerturb.setSource(groundGradient);
    
    //SELECT SAND/GRASS or STONE
    ModuleSelect stoneSandGrassSelect = new ModuleSelect();
    stoneSandGrassSelect.setLowSource(terrainSelect); //stone value
    stoneSandGrassSelect.setHighSource(2.3);
    stoneSandGrassSelect.setControlSource(strataGradientPerturb);
    stoneSandGrassSelect.setThreshold(.94);
    stoneSandGrassSelect.setFalloff(0);
    
    //ADD AREAS NEAR CAVES AS CAVE-ISH STONE
    ModuleSelect stoneSandGrassCaveSelect = new ModuleSelect();
    stoneSandGrassCaveSelect.setLowSource(stoneSandGrassSelect); //stone value
    stoneSandGrassCaveSelect.setHighSource(2.5);
    stoneSandGrassCaveSelect.setControlSource(caveShapeCache);
    stoneSandGrassCaveSelect.setThreshold(.75);
    stoneSandGrassCaveSelect.setFalloff(0);
        
    ModuleCache terrSelectCache = new ModuleCache();
    terrSelectCache.setSource(terrainSelect);

    /*
     * final-almost
     */
    ModuleCombiner groundCaveMultiply = new ModuleCombiner(CombinerType.MULT);
    groundCaveMultiply.setSource(0, mp_caveModule);
    groundCaveMultiply.setSource(1, groundSelect);
    groundCaveMultiply.setSource(2, stoneSandGrassCaveSelect);
    
    /*
     * Bedrock
     */
    ModuleGradient bedrockGradient = new ModuleGradient();
    bedrockGradient.setGradient(0, 0, .95, 1);
    
    ModuleScaleOffset bedrockScaleOffset = new ModuleScaleOffset();
    bedrockScaleOffset.setScale(.05);
    bedrockScaleOffset.setOffset(0.0);
    bedrockScaleOffset.setSource(mp_mountainCache);
    
    ModuleScaleDomain bedrockYScale = new ModuleScaleDomain();
    bedrockYScale.setScaleY(0);
    bedrockYScale.setSource(bedrockScaleOffset);

    ModuleTranslateDomain bedrockTerrain = new ModuleTranslateDomain();
    bedrockTerrain.setAxisYSource(bedrockYScale);
    bedrockTerrain.setSource(bedrockGradient);
    
    ModuleSelect bedrockSelect = new ModuleSelect();
    bedrockSelect.setLowSource(groundCaveMultiply);
    bedrockSelect.setHighSource(.5);
    bedrockSelect.setControlSource(bedrockTerrain);
    bedrockSelect.setThreshold(0.9);
    bedrockSelect.setFalloff(0);

    /*
     * Draw it
     */
    canvas.updateImage(bedrockSelect);

  }
  private static boolean updateCanvasSelect(Canvas canvas, int select, Module module) {
	  return updateCanvasSelect(canvas, select, module, "");
  }
  private static boolean updateCanvasSelect(Canvas canvas, int select, Module module, String msg) {
	  return false;
//	  if (!ModuleSelectModeIsOn) return false;
//	  if (select++ == caveModuleLevel) {
//	    	canvas.updateImage(module);
//	    	System.out.println("Module: " + msg + " : " + module.toString());
//	    	return true;
//	    }
//	  return false;
  }

}
