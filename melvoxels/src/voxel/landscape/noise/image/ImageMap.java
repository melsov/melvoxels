package voxel.landscape.noise.image;

import voxel.landscape.BlockType;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.util.Asserter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by didyouloseyourdog on 3/28/15.
 */
public class ImageMap {
    private static String heightSrc = "src/Textures/heightMap.jpg";
    private static String blockTypeSrc = "src/Textures/typeMap.jpg";
    private BufferedImage heightMap;
    private BufferedImage blockTypeMap;
    private static float heightMapScale = 0.4f;
    private static int groundOffset = 20;

    public ImageMap(String heightMapSrc, String blockTypeMapSrc) {
        heightMap = GetBufferedImage(heightMapSrc);
        blockTypeMap = GetBufferedImage(blockTypeMapSrc);
    }
    public ImageMap() {
        this(heightSrc, blockTypeSrc);
    }

    public int blockTypeAt(int xin, int yin, int zin) {
        int result = BlockType.AIR.ordinal();
        if (xin < heightMap.getWidth() && zin < heightMap.getHeight() && xin >= 0 && zin >= 0) {
            int height = (int)(TerrainMap.GetWorldHeightInBlocks() * heightMapScale * ((heightMap.getRGB(xin, zin)&0xFF) + 1)/ 256) + groundOffset; //red channel
            if (yin < height) {
                result = mapBlockTypeFromImage(xin, yin, zin);
            }
        }
        return result;
    }

    private int mapBlockTypeFromImage(int xin, int yin, int zin) {
        Color color = new Color(blockTypeMap.getRGB(xin, zin));
        double lowestDistance = Double.MAX_VALUE;
        BlockType result = BlockType.GRASS;
//        for(BlockType blockType : BlockType.class.getEnumConstants()) {
//            double dist = colorDistance(color, blockType.color());
//            if (dist < lowestDistance) {
//                lowestDistance = dist;
//                result = blockType;
//            }
//        }
        return result.ordinal();
    }

    private static BufferedImage GetBufferedImage(String src) {
        File imFile = new File(src);
        Asserter.assertTrue(imFile.exists(), "bad file name: " + src);
        BufferedImage buffi = null;
        try {
            buffi = ImageIO.read(imFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return buffi;
    }

    // courtesy: http://stackoverflow.com/questions/6334311/whats-the-best-way-to-round-a-color-object-to-the-nearest-color-constant
    double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }

}
