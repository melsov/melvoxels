package com.sudoplay.joise.examples;

import com.sudoplay.joise.module.*;
import voxel.landscape.BlockType;
import voxel.landscape.noise.TerrainNoiseSettings;
import voxel.landscape.player.B;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 9/26/14.
 */
public class JoiseMultiPanelExample extends JPanel {
    private static int panelWidth = 750, panelHeight = 250, panelMarginRight = 100;
    private static int panelsPerRow = 1;
    private static int panelsPerColumn = 2;
    private static long seed = 1236;

    private HashMap<String, JoiseDemoPanel> demoPanels = new HashMap<String, JoiseDemoPanel> (20);

    public JoiseMultiPanelExample() {
        loadModules();
        for (JoiseDemoPanel demoPanel : demoPanels.values()) {
            demoPanel.updateImage();
        }
    }
    private void addDemoPanelWithModule(Module mod, String message, TerrainNoiseSettings terrainNoiseSettings) {
        JoiseDemoPanel demoPanel = demoPanels.get(message);
        if (demoPanel == null) {
            demoPanel = new JoiseDemoPanel(this, mod, panelWidth, panelHeight, panelMarginRight, message, terrainNoiseSettings);
            demoPanels.put(message, demoPanel);
        } else {
            demoPanel.setModule(mod);
        }
    }
    private TerrainNoiseSettings getDemoPanelSettings(String key) {
        JoiseDemoPanel demoPanel = demoPanels.get(key);
        if (demoPanel == null) return null;
        return demoPanel.getTerrainNoiseSettings();
    }

    public void reloadModules() {
        loadModules();
        B.bugln("reloading");
        for (JoiseDemoPanel demoPanel : demoPanels.values()) {
            demoPanel.updateImage();
        }
    }

    private void addModuleToDisplay(Module mod, TerrainNoiseSettings terrainNoiseSettings, String noiseSettingsKey) {
        TerrainNoiseSettings noiseSettings = getDemoPanelSettings(noiseSettingsKey);
        if (terrainNoiseSettings == null) {
            noiseSettings = terrainNoiseSettings; // TerrainNoiseSettings.MountainTerrainNoiseSettings(seed);
        }
        Module mountainTerrain = mod;
        addDemoPanelWithModule(mountainTerrain, noiseSettingsKey, noiseSettings);
    }

    private void loadModules() {
        /*
         * Caves!
         */
//        String caveKey = "caves";
//        CaveNoiseSettings caveSettings = CaveNoiseSettings.CaveSettingsForTerrain(seed);
//        caveSettings.caveSelect.highSource = new ScalarParameter(1);
//        caveSettings.caveSelect.lowSource = new ScalarParameter(0);
//        Module cave = caveSettings.makeModule();
//        TerrainNoiseSettings dummy = new TerrainNoiseSettings(seed);
//        addDemoPanelWithModule(cave,caveKey, dummy);
//        String cavePreKey = "cave pre select";
//        Module caveCombinedPreSelect = caveSettings.caveShapeABSettings.makeCombinerModule();
//        addDemoPanelWithModule(caveCombinedPreSelect, cavePreKey, dummy);

        /*
         * Test spheres
         */

        ModuleTiers moduleTiers = new ModuleTiers();
        moduleTiers.setSource(.5);
        moduleTiers.setNumTiers(5);
        String tiersKey = "module tiers";
        addDemoPanelWithModule(moduleTiers, tiersKey, null);
        demoPanels.get(tiersKey).removeGUI();

        ModuleCellular moduleCellular = new ModuleCellular();
        moduleCellular.setCoefficients(9, .5, 3, 7);
        String cellularKey = "module cellular";
        addDemoPanelWithModule(moduleCellular, cellularKey, null);
        demoPanels.get(cellularKey).removeGUI();

        /*
         * ground_gradient
         */
        // ground_gradient
        ModuleGradient groundGradient =  new ModuleGradient();
        groundGradient.setGradient(0, 0, 0, 1, 0, 0);
        String gradientKey = "Ground Gradient";
        addDemoPanelWithModule(groundGradient, gradientKey, null);
        demoPanels.get(gradientKey).removeGUI();

        String mountainsKey = "mountains before gradient";
        TerrainNoiseSettings mountainSettings = getDemoPanelSettings(mountainsKey);
        if (mountainSettings == null) {
            mountainSettings = TerrainNoiseSettings.MountainTerrainNoiseSettings(seed, false);
        }
        Module mountainTerrainNoGradient = mountainSettings.makeTerrainModule(null);
        addDemoPanelWithModule(mountainTerrainNoGradient, mountainsKey, mountainSettings);

        String mountainsWithGradientKey = "translate gradient domain with mountains";
        TerrainNoiseSettings gradMountainSettings = getDemoPanelSettings(mountainsWithGradientKey);
        if (gradMountainSettings == null) {
            gradMountainSettings = TerrainNoiseSettings.MountainTerrainNoiseSettings(seed, false);
        }
        ModuleTranslateDomain mountainTerrain = new ModuleTranslateDomain();
        mountainTerrain.setAxisYSource(mountainTerrainNoGradient);
        mountainTerrain.setSource(groundGradient);

        addDemoPanelWithModule(mountainTerrain, mountainsWithGradientKey, gradMountainSettings);
        demoPanels.get(mountainsWithGradientKey).removeGUI();

        /*
        String highlandKey = "highlands";
        TerrainNoiseSettings highlandSettings = getDemoPanelSettings(highlandKey);
        if (highlandSettings == null) {
            highlandSettings = TerrainNoiseSettings.HighLandTerrainNoiseSettings(seed);
        }
        Module highlandTerrain = TerrainDataProvider.MakeTerrainNoise(groundGradient, highlandSettings );
        addDemoPanelWithModule(highlandTerrain, highlandKey, highlandSettings);
        */
        /*
         * select air or solid with mountains
         */
        String terrainSelectKey = "terrain Select";
        TerrainNoiseSettings terrSelectSettings = getDemoPanelSettings(terrainSelectKey);
        if (terrSelectSettings == null) {
            terrSelectSettings = TerrainNoiseSettings.TerrainSettingsWithZeroOneModuleSelect(seed, mountainTerrain);
        } else {
            terrSelectSettings.moduleSelectSettings.controlSource = mountainTerrain;
        }
        Module terrSelectModule = terrSelectSettings.moduleSelectSettings.makeSelectModule();
        addDemoPanelWithModule(terrSelectModule, terrainSelectKey, terrSelectSettings);

        /*
         * noise to determine which kind of solid block
         */
        String typeSelectSettingKey = "terrain type select";
        TerrainNoiseSettings typeSelectSettings = getDemoPanelSettings(typeSelectSettingKey);
        if (typeSelectSettings == null) {
            typeSelectSettings = TerrainNoiseSettings.TerrainTypeSelectModuleNoiseSettings(seed, false);
        }
        Module selectTypeTerr = typeSelectSettings.makeTerrainModule(null);
        //addDemoPanelWithModule(selectTypeTerr, typeSelectSettingKey, typeSelectSettings);

        String dirtOrStoneSelectSettingsKey = "dirt or stone";
        TerrainNoiseSettings dirtOrStoneSelectSettings = getDemoPanelSettings(dirtOrStoneSelectSettingsKey);
        if (dirtOrStoneSelectSettings == null) {
            dirtOrStoneSelectSettings = TerrainNoiseSettings.BlockTypeSelectModuleSettings(seed, selectTypeTerr, BlockType.DIRT, BlockType.STONE);
        } else {
            dirtOrStoneSelectSettings.moduleSelectSettings.controlSource = selectTypeTerr;
        }
        Module dirtOrStoneModule = dirtOrStoneSelectSettings.moduleSelectSettings.makeSelectModule();
        //addDemoPanelWithModule(dirtOrStoneModule, dirtOrStoneSelectSettingsKey, dirtOrStoneSelectSettings);

        /*
         * combine terrain select and block type select
         */
        String combineTerrainAndBlockTypeKey = "Terrain And BlockType";
        ModuleCombiner terrAndBlockTypeMod = new ModuleCombiner(ModuleCombiner.CombinerType.MULT);
        terrAndBlockTypeMod.setSource(0, terrSelectModule);
        terrAndBlockTypeMod.setSource(1, dirtOrStoneModule);
        TerrainNoiseSettings combineSettings = new TerrainNoiseSettings(seed); //defaults
        combineSettings.renderWithBlockColors = true;
        //addDemoPanelWithModule(terrAndBlockTypeMod, combineTerrainAndBlockTypeKey, combineSettings);
        //demoPanels.get(combineTerrainAndBlockTypeKey).removeGUI();



    }

    public static void main(String[] args) {
        int N = 4;
        int width = (panelWidth + panelMarginRight) * panelsPerRow;
        int height = panelHeight * panelsPerColumn;

        JFrame frame = new JFrame("Joise Example-Multi Panel");
        frame.setPreferredSize(new Dimension(width, height));
        frame.setLayout(new GridLayout(panelsPerColumn,panelsPerRow,N,N));

        final JoiseMultiPanelExample multiPanelExample = new JoiseMultiPanelExample();
        for( JoiseDemoPanel demoPanel : multiPanelExample.demoPanels.values()) {
            frame.add(demoPanel);
        }
//        frame.add(multiPanelExample);

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
//                updateImage(canvas);
            }
        });

//        updateImage(canvas);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
