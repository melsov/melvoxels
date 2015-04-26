package voxel.landscape.noise.fake;

import voxel.landscape.coord.Axis;
import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.chunkbuild.ChunkFinder;
import voxel.landscape.coord.Box;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.map.structure.structures.surface.Pyramid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by didyouloseyourdog on 12/23/14.
 */
public class BorderBoxMaker {
    private BorderBox fakeCaveBorderBox;
    private BorderBox fakeTallCaveBorderBox;
    private BorderBox enclosure;
    private static List<BorderBox> enclosures;
    private Cone theCone;
    private List<Cone> cones;
    private List<BorderBox> columns;
    private static final int ENCLOSURE_COUNT = 8;

    private Pyramid pyramid = new Pyramid(123);

    private List<BorderBox> getColumns() {
        if (columns == null) {
            columns = new ArrayList<>(ENCLOSURE_COUNT);
            int xzdim = 1;
            int startxz = 2;
            int incrxz = 1;
            int yDim = 10;
            for (int i = 0; i < ENCLOSURE_COUNT; ++i) {
                BorderBox b = new BorderBox(new Box(new Coord3(startxz, 9, startxz), new Coord3(xzdim, 4, xzdim)));
                b.openFaces = new boolean[] { false, false, false, false, true, false};
                startxz += incrxz;
                yDim = Math.min(50, yDim + 6);
                columns.add(b);
            }
        }
        return columns;
    }

    private static List<AbstractTestShape> shapeMix = new ArrayList<>(48);
    static {
//        setupShapeMixPyramid();
        setupToothyBoxes();
    }

    private static void setupToothyBoxes() {
        Coord2 start2 = ChunkFinder.GetTestColumns().start;
        Coord3 start = Chunk.ToWorldPosition(new Coord3(start2.x, 0, start2.getZ() ));
        int height = 7;
        int dim = 16;
        int xoffset = 5, zoffset = 5;


        Box box = new Box(new Coord3(start.x + xoffset , height, start.getZ() + zoffset ), new Coord3(dim));
//        ToothyBox toothyBox = new ToothyBox(box);
        BorderBox toothyBox = new BorderBox(box);
        toothyBox.openFaces = new boolean[] {false, false, false, false, true, false };
        shapeMix.add(toothyBox);
    }

    private static void setupShapeMixPyramid() {
        //set up shape mix list //open pyramid configuration
        // todo: try a set up with only two toothy boxes
        int xzdim = 16;
        int ydim = 16;
        int baseSize = 5;
        Coord2 start = ChunkFinder.GetTestColumns().start;
        for(int k = baseSize; k >= baseSize/2; --k) {
            int level = baseSize - k;
            for (int i = level; i < k; i += k - 1) {
                for (int j = level; j < k; ++j) {
                    boolean[] openfaces = new boolean[]{true, false, false, true, true, false};
                    Box cbox = new Box(
                            new Coord3(
                                start.x + (xzdim * i * .35) + 2 + i + j*3,
                                (level * ydim * .5) + 7,
                                start.getZ() + (xzdim * j) + 2),
                            new Coord3(
                                xzdim + 2,
                                ydim,
                                xzdim + 2));
//                    ToothyCone bcone = new ToothyCone(cbox, Axis.Z);
                    ToothyBox tbox = new ToothyBox(cbox);
                    shapeMix.add(tbox);
                }
            }
            if (level == k) break;
        }

    }

    public int shapeMix(int x, int y, int z) {
//        x = x % (ChunkFinder.GetTestColumns().dimensions.getX() * Chunk.XLENGTH);
//        x = Math.abs(x);
//        z = z % (ChunkFinder.GetTestColumns().dimensions.getZ() * Chunk.ZLENGTH);
//        z = Math.abs(z);
        if (y < 4) return BlockType.GRASS.ordinal();
        Coord3 co = new Coord3(x,y,z);
        for (int i = 0 ; i < shapeMix.size(); ++i) {
            if (shapeMix.get(i).isOnBorder(co)) {
                return BlockType.SAND.ordinal();
            }
        }
        return BlockType.AIR.ordinal();
    }
    private List<Cone> getCones() {
        if (cones == null) {
            cones  = new ArrayList<>(4);

            ToothyCone toothyOne = new ToothyCone(new Box(new Coord3(1, 8, 15), new Coord3(12,18,12)), Axis.Y);
            toothyOne.taperDirection = Direction.YPOS;
            cones.add(toothyOne);

            Cone toothyTwo = new Cone(new Box(new Coord3(1, 18, 15), new Coord3(12,18,12)));
//            ToothyCone toothyTwo = new ToothyCone(new Box(new Coord3(1, 18, 15), new Coord3(12,18,12)), Axis.Z);
            toothyTwo.taperDirection = Direction.YPOS;
            cones.add(toothyTwo);

            cones.add(new ToothyCone(new Box(new Coord3(15, 8, 15), new Coord3(18,18,18))));

//            cones.add(new ToothyCone(new Box(new Coord3(31, 8, 15), new Coord3(18,18,18))));
//            cones.add(new ToothyCone(new Box(new Coord3(-1, 8, 31), new Coord3(18,18,18))));
//            TODO: study the cases presented here.
        }
        return cones;
    }


    public int conescape(int x, int y, int z) {
        if (y < 4) return BlockType.GRASS.ordinal();
        Coord3 co = new Coord3(x,y,z);
        for (int i = 0 ; i < getCones().size(); ++i) {
            Cone cone = cones.get(i);
            if (cone.isOnBorder(co)) {
                return BlockType.SAND.ordinal();
            }
        }
        return BlockType.AIR.ordinal();
    }
    private Cone getTheCone() {
        if (theCone == null) {
            theCone = new ToothyCone(new Box(new Coord3(15, 8, 15), new Coord3(18,18,18)));
        }
        return theCone;
    }
    private List<BorderBox> getEnclosures() {
        if (enclosures == null) {
            enclosures = new ArrayList<>(ENCLOSURE_COUNT);
            int xzdim = 8;
            int startxz = 2;
            int incrxz = (int) (xzdim * .75);
            int yDim = 10;
            for (int i = 0; i < ENCLOSURE_COUNT; ++i) {
                BorderBox b = new BorderBox(new Box(new Coord3(startxz, 2, startxz), new Coord3(xzdim, yDim, xzdim)));
                b.openFaces = new boolean[] { false, false, false, false, true, false};
                startxz += incrxz;
                yDim = Math.min(50, yDim + 6);
                enclosures.add(b);
            }
        }
        return enclosures;
    }

    public int columns(int x, int y, int z) {
        if (y < 4) return BlockType.GRASS.ordinal();
        Coord3 co = new Coord3(x,y,z);
        for (int i = 0 ; i < getColumns().size(); ++i) {
            BorderBox bb = getColumns().get(i);
            if (bb.box.contains(co)) {
                return BlockType.SAND.ordinal();
            }
        }
        return BlockType.AIR.ordinal();
    }


    private int enclosuresBorder(int x, int y, int z) {
        if (y < 4) return BlockType.GRASS.ordinal();
        Coord3 co = new Coord3(x,y,z);
        for (int i = 0 ; i < getEnclosures().size(); ++i) {
            BorderBox bb = getEnclosures().get(i);
            if (bb.box.contains(co)) {
                if (!bb.isOnBorder(co))
                    return BlockType.AIR.ordinal();
                if (co.y == bb.box.extent().y - 1)
                    return BlockType.GRASS.ordinal();
                if (i < getEnclosures().size() - 1 && getEnclosures().get(i + 1).box.contains(co))
                    return BlockType.AIR.ordinal();
                if (i > 0 && getEnclosures().get(i - 1).box.contains(co))
                    return BlockType.AIR.ordinal();
                return (i & 1) == 1 ? BlockType.SAND.ordinal() : BlockType.STONE.ordinal();
            }
        }
        return BlockType.AIR.ordinal();
    }
    public BorderBox getFakeCaveBorderBox() {
        if (fakeCaveBorderBox == null) {
            fakeCaveBorderBox = new BorderBox(new Box(new Coord3(20, 0, 20), new Coord3(6,8,6)));
            fakeCaveBorderBox.openFaces[Direction.XNEG] = true;
        }
        return fakeCaveBorderBox;
    }
    public BorderBox getFakeTallCaveBorderBox() {
        if (fakeTallCaveBorderBox == null) {
            fakeTallCaveBorderBox = new BorderBox(new Box(new Coord3(18, 6, 18), new Coord3(9,12,9)));
            fakeTallCaveBorderBox.openFaces[Direction.XNEG] = false;
            fakeTallCaveBorderBox.openFaces[Direction.ZNEG] = false;
            fakeTallCaveBorderBox.openFaces[Direction.ZPOS] = false;
        }
        return fakeTallCaveBorderBox;
    }
    public BorderBox getEnclosure() {
        if (enclosure == null) {
            enclosure = new BorderBox(new Box(new Coord3(2, 2, 7), new Coord3(6, 12, 22)));
            enclosure.openFaces = new boolean[] {true, true, false, false, true, false};
        }
        return enclosure;
    }
    private int flat(int y) {
        if (y < 4) return BlockType.GRASS.ordinal();
        return BlockType.AIR.ordinal();
    }
    public int fakeTallCaveWithBoxAndAdjacentEnclosure(int x, int y, int z) {
        if (y < 4) return BlockType.GRASS.ordinal();
        BlockType pyrBlock = pyramidBlock(x,y,z, new Coord3(2, 0, 2));
        if (pyrBlock != null) {
            return pyrBlock.ordinal();
        }
        if (getFakeTallCaveBorderBox().isOnBorder(new Coord3(x,y,z))) {
            if ((x == 2 && y < 21 && y > 16)) {
                return BlockType.AIR.ordinal();
            }
            return BlockType.LANTERN.ordinal();
        }
        if (getEnclosure().isOnBorder(new Coord3(x,y,z))) {
            return BlockType.SAND.ordinal();
        }
        return BlockType.AIR.ordinal();
    }
    private BlockType pyramidBlock(int x, int y, int z, Coord3 offset) {
        Coord3 global = new Coord3(x, y - 4, z).minus(offset);
        Coord3 local =global; // Chunk.ToChunkLocalCoord(global);
        return pyramid.getBlocks().get(local);
    }
    public int coneCave(int x, int y, int z) {
        if (y < 4) return BlockType.GRASS.ordinal();
        if (getTheCone().isOnBorder(new Coord3(x,y,z))) {

            return BlockType.DIRT.ordinal();
        }
        return BlockType.AIR.ordinal();
    }
    private int fakeCaveWithBox(int x, int y, int z) {
        if (y < 4) return BlockType.GRASS.ordinal();
        if (y == 36 && x > 1 && z > 1) return BlockType.STONE.ordinal();

        if (getFakeCaveBorderBox().isOnBorder(new Coord3(x,y,z))) {
            return BlockType.LANTERN.ordinal();
        }
        if (getFakeTallCaveBorderBox().isOnBorder(new Coord3(x,y,z))) {
            return BlockType.STONE.ordinal();
        }
        return BlockType.AIR.ordinal();
    }
    private boolean fakeCave(int xin, int yin, int zin) {
        Coord3 dims = new Coord3(12, 8, 32);
        Box box = new Box (new Coord3(10, 40, 0), dims);
        Coord3 co = new Coord3(xin, yin, zin);
        return box.contains(co);
    }

    private int testNoise(int x, int y, int z) {
//        if  ( (x & 15 + x & 7) + (z & 15 + z & 7) + y < 54) return 3;
//        int sumxz = (x & 15) + (z & 15);
//        if ( y < 17 || (y != 20 && y != 19 && ((sumxz + 8) & 31) < 16 && y < 22)) return 3;
        int across = z;
        if ((y == 14) && ((across & 3) == 1)) {
            if ((z & 1) == 1) return 8;
            else return 5;
        }
        if (y == 10 && (across & 15) == 5) return 7;
        if (y == 12 && (across & 3) == 2) return 4;
//        if (y < 8) return 3;
        return 1;
    }
}
