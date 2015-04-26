package com.sudoplay.joise.examples;

import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleBasisFunction;
import com.sudoplay.joise.module.ModuleFractal;
import voxel.landscape.BlockType;
import voxel.landscape.noise.TerrainNoiseSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Created by didyouloseyourdog on 9/26/14.
 */
public class JoiseDemoPanel extends JPanel {
    int width, height;
    private static int settingsGUIStartHeight = 40;
    int marginRight;
    private BufferedImage image;
    private Module module;
    private float SCALE = 1f;
    TerrainSettingsGUI terrainSettingsGUI;
    NoiseImageHolder noiseImageHolder = new NoiseImageHolder();
    JoiseMultiPanelExample joiseMultiPanelExample;

    public TerrainNoiseSettings getTerrainNoiseSettings() {
        return terrainSettingsGUI.getTerrainNoiseSettings(); }

    public class NoiseImageHolder extends JPanel {
        public void updateImage() {
            int width = image.getWidth();
            int height = image.getHeight();
            float px, py, r;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    px = x / (float) height * SCALE;
                    py = y / (float) height * SCALE;

                    /*
                     * Sample the module chain like this...
                     */
                    r = (float) module.get(px, py);
                    Color c;
                    if (terrainSettingsGUI != null && getTerrainNoiseSettings() != null && getTerrainNoiseSettings().renderWithBlockColors) {
                        c = BlockType.terrainDemoColor((int) r);
                    } else {
                        r = Math.max(0, Math.min(1, r));
                        c = new Color(r, r, r);
                    }
                    image.setRGB(x, y, c.getRGB());
                }
            }
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(image, null, null);
            g2.dispose();
        }
    }
    public class TerrainSettingsGUI extends JPanel implements ActionListener {
        private TerrainNoiseSettings terrainNoiseSettings;
        public TerrainNoiseSettings getTerrainNoiseSettings() { return terrainNoiseSettings; }
        private int width, height;
        private String message;
        public TerrainSettingsGUI(int _w, int _h, TerrainNoiseSettings _terrainNoiseSettings, String _msg) {
            message = _msg;
            width = _w; height = _h; terrainNoiseSettings = _terrainNoiseSettings;
            this.setLayout(new GridLayout(6,2));
            this.setBackground(new Color(122,122,255));
            this.add(new JLabel(message));
            setUpControls();
        }
        public void removeGUI() {
            for (Component component : getComponents()) {
                if (component.getClass().equals(JLabel.class)) {
                    if (((JLabel) component).getText().equals(message)) continue;
                }
                remove(component);
            }
            add(new Label(message));
        }

        private void setUpControls() {
            if (terrainNoiseSettings == null) return;
            if (terrainNoiseSettings.moduleSelectSettings == null) {
                addLabelTextFieldPairScale();
                addLabelTextFieldPairOffset();
                addLabelTextFieldPairYDomainScale();
                addLabelTextFieldPairXDomainScale();
                addLabelTextFieldPairFrequency();
                addLabelTextFieldPairOctaves();
                this.add(fractalTypeMenu());
                this.add(interpolationTypeMenu());
                this.add(basisTypeMenu());
            } else {
                addLabelTextFieldPairSelectThreshold();
                addLabelTextFieldPairSelectFalloff();
            }
        }
        private void addLabelTextFieldPairFrequency() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("frequency"));
            final JTextField tf = new JTextField(10);
            tf.setText("" + terrainNoiseSettings.fractalSettings.frequency);
            tf.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    terrainNoiseSettings.fractalSettings.frequency = Double.parseDouble(tf.getText());
                    joiseMultiPanelExample.reloadModules();
                }
            });
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairSelectThreshold() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("threshold"));
            final JTextField tf = new JTextField(10);
            String text = terrainNoiseSettings.moduleSelectSettings.threshold.isModule() ?
                    terrainNoiseSettings.moduleSelectSettings.threshold.toString() :
                    "" + terrainNoiseSettings.moduleSelectSettings.threshold.getValue();
            tf.setText(text);
            if (!terrainNoiseSettings.moduleSelectSettings.threshold.isModule()) {
                tf.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        terrainNoiseSettings.moduleSelectSettings.threshold.set(Double.parseDouble(tf.getText()));
                        joiseMultiPanelExample.reloadModules();
                    }
                });
            }
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairSelectFalloff() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("falloff"));
            final JTextField tf = new JTextField(10);
            String text = terrainNoiseSettings.moduleSelectSettings.falloff.isModule() ?
                    terrainNoiseSettings.moduleSelectSettings.falloff.toString() :
                    "" + terrainNoiseSettings.moduleSelectSettings.falloff.getValue();
            tf.setText(text);
            if (!terrainNoiseSettings.moduleSelectSettings.falloff.isModule()) {
                tf.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        terrainNoiseSettings.moduleSelectSettings.falloff.set(Double.parseDouble(tf.getText()));
                        joiseMultiPanelExample.reloadModules();
                    }
                });
            }
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairOctaves() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("octaves"));
            final JTextField tf = new JTextField(10);
            tf.setText("" + terrainNoiseSettings.fractalSettings.octaves);
            tf.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    terrainNoiseSettings.fractalSettings.octaves = Integer.parseInt(tf.getText());
                    joiseMultiPanelExample.reloadModules();
                }
            });
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairScale() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("scale"));
            final JTextField tf = new JTextField(10);
            tf.setText("" + terrainNoiseSettings.xzscale);
            tf.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    terrainNoiseSettings.xzscale = Float.parseFloat(tf.getText());
                    joiseMultiPanelExample.reloadModules();
                }
            });
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairOffset() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("offset"));
            final JTextField tf = new JTextField(10);
            tf.setText("" + terrainNoiseSettings.offset);
            tf.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    terrainNoiseSettings.offset = Float.parseFloat(tf.getText());
                    joiseMultiPanelExample.reloadModules();
                }
            });
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairYDomainScale() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("y domain scale"));
            final JTextField tf = new JTextField(10);
            tf.setText("" + terrainNoiseSettings.yScale);
            tf.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    terrainNoiseSettings.yScale = Float.parseFloat(tf.getText());
                    joiseMultiPanelExample.reloadModules();
                }
            });
            pairPanel.add(tf);
            this.add(pairPanel);
        }
        private void addLabelTextFieldPairXDomainScale() {
            JPanel pairPanel = new JPanel(new GridLayout(1,2));
            pairPanel.add(new JLabel("x domain scale"));
            final JTextField tf = new JTextField(10);
            tf.setText("" + terrainNoiseSettings.xScale);
            tf.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    terrainNoiseSettings.xScale = Float.parseFloat(tf.getText());
                    joiseMultiPanelExample.reloadModules();
                }
            });
            pairPanel.add(tf);
            this.add(pairPanel);
        }

        private JToolBar fractalTypeMenu() {
            if (terrainNoiseSettings == null ) return null;
            final JToolBar toolBar = new JToolBar();

            final JButton button = new JButton(terrainNoiseSettings.fractalSettings.fractalType.name());
            button.setSize(70, 20);
            final JPanel thisPanel = this;
            //Create the popup menu.
            final JPopupMenu popup = new JPopupMenu();

            for (final ModuleFractal.FractalType fracType : ModuleFractal.FractalType.values()) {
                popup.add(new JMenuItem(new AbstractAction(fracType.name()) {
                    public void actionPerformed(ActionEvent e) {
                        terrainNoiseSettings.fractalSettings.fractalType = fracType;
                        joiseMultiPanelExample.reloadModules();
                        button.setText(terrainNoiseSettings.fractalSettings.fractalType.name());
                    }
                }));
            }

            button.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            });
            toolBar.add(button);
            return toolBar;
        }
        private JToolBar basisTypeMenu() {
            final JToolBar toolBar = new JToolBar();

            final JButton button = new JButton(terrainNoiseSettings.fractalSettings.basisType.name());
            button.setSize(70, 20);
            final JPanel thisPanel = this;
            //Create the popup menu.
            final JPopupMenu popup = new JPopupMenu();

            for (final ModuleBasisFunction.BasisType basisType : ModuleBasisFunction.BasisType.values()) {
                popup.add(new JMenuItem(new AbstractAction(basisType.name()) {
                    public void actionPerformed(ActionEvent e) {
                        terrainNoiseSettings.fractalSettings.basisType = basisType;
                        joiseMultiPanelExample.reloadModules();
                        button.setText(terrainNoiseSettings.fractalSettings.basisType.name());
                    }
                }));
            }

            button.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            });
            toolBar.add(button);
            return toolBar;
        }
        private JToolBar interpolationTypeMenu() {
            final JToolBar toolBar = new JToolBar();

            final JButton button = new JButton(terrainNoiseSettings.fractalSettings.interpolationType.name());
            button.setSize(70, 20);
            final JPanel thisPanel = this;
            //Create the popup menu.
            final JPopupMenu popup = new JPopupMenu();

            for (final ModuleBasisFunction.InterpolationType interpolationType : ModuleBasisFunction.InterpolationType.values()) {
                popup.add(new JMenuItem(new AbstractAction(interpolationType.name()) {
                    public void actionPerformed(ActionEvent e) {
                        terrainNoiseSettings.fractalSettings.interpolationType = interpolationType;
                        joiseMultiPanelExample.reloadModules();
                        button.setText(terrainNoiseSettings.fractalSettings.interpolationType.name());
                    }
                }));
            }

            button.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            });
            toolBar.add(button);
            return toolBar;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Terr Settings GUI got an action event: " + e.toString());
        }
    }


    public JoiseDemoPanel(JoiseMultiPanelExample joiseMultiPanelExample, Module mod, int _w, int _h, int  _marginRight, String _message) {
        this(joiseMultiPanelExample, mod, _w, _h, _marginRight, _message, null);
    }
    public JoiseDemoPanel(JoiseMultiPanelExample _joiseMultiPanelExample, Module mod,
                          int _w, int _h, int  _marginRight, String _message, TerrainNoiseSettings _terrainNoiseSettings) {
        super(new BorderLayout());
        joiseMultiPanelExample = _joiseMultiPanelExample;
        module = mod;
        width = _w; height = _h;
        this.setLayout(new GridLayout(2,1));
        terrainSettingsGUI = new TerrainSettingsGUI(_marginRight, height - settingsGUIStartHeight,  _terrainNoiseSettings, _message);
        this.add(noiseImageHolder);
        this.add(terrainSettingsGUI);
        marginRight = _marginRight;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.setSize(new Dimension(width + marginRight, height));
        this.setBackground(Color.CYAN);
        this.setBorder(BorderFactory.createLineBorder(Color.GREEN));
    }
    public void removeGUI() {
        terrainSettingsGUI.removeGUI();
    }
    public Module getModule() {
        return module;
    }
    public void setModule(Module mod) { module = mod; }

    public BufferedImage getImage() { return image; }

    public void updateImage() {
        noiseImageHolder.updateImage();
        repaint();
    }



}
