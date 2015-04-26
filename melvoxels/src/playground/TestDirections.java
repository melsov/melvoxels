package playground;

/**
 * Created by didyouloseyourdog on 7/31/14.
 */

import voxel.landscape.chunkbuild.ChunkFinder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class TestDirections extends JPanel {

    private static final float SCALE = 1.0f;
    private BufferedImage image;

    public TestDirections(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    void updateImage() {
        image = ChunkFinder.bufferedImageTest();
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(image, null, null);
        g2.dispose();
    }

    public static void main(String[] args) {
        int width = 800;
        int height = 800;

        JFrame frame = new JFrame("Directions");
        frame.setPreferredSize(new Dimension(width, height));

        TestDirections canvas = new TestDirections(width, height);
        frame.add(canvas);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas.updateImage();

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
