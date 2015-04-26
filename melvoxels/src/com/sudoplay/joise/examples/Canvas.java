package com.sudoplay.joise.examples;

import com.sudoplay.joise.module.Module;
import voxel.landscape.player.B;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class Canvas extends JPanel {

    private BufferedImage image;

    private static final float SCALE = 1/16f;
    public final float gridSize = 32f;

    Canvas(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    void updateImage(Module mod) {
        float px, py, r;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (x % gridSize == 0 || y % gridSize == 0) {
                    image.setRGB(x, y, Color.RED.getRGB());
                    continue;
                }
                px = x / gridSize;
                py = y / gridSize;
//                px = (float) Math.floor(px);
//                py = (float) Math.floor(py);
                r = (float) mod.get(px, py);
                if (x == 1 && y == 1) {
                    B.bug(r);
                }
                float clampR = Math.max(0, Math.min(1, r));
                Color c = new Color(
                        r > 4 ? 1f : 0f,
                        r > 3 && r < 4 ? 1f : 0f,
                        r > 1 && r < 3 ? 1f : 0f);
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
