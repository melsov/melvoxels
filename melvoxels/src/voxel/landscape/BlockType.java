package voxel.landscape;

import voxel.landscape.map.light.SunLightComputer;
import voxel.landscape.map.water.WaterFlowComputer;

import java.awt.*;
import java.util.*;

public enum BlockType {
	NON_EXISTENT (0, 0d, new Color(0.8392157f,0f, 0.81960785f)),
	AIR (1, 0d, new Color(0.22745098f, 0.9019608f, 1f)),
    DIRT(2, 4d, new Color(0.3137255f, 0.09411765f, 0.0627451f)),
	GRASS(3, 0d, new Color(0f, 1f, 0f)),
    SAND(4, 3d, new Color(.9f, .9f, .6f)),
	STONE(5, 5d, new Color(.4f, .4f, .4f)),
    CAVESTONE(6, 1d, new Color(.3f, .5f, .3f)),
    LANTERN(7, 0d, new Color(.9f, 1f, .3f)),
    BEDROCK(8, 0d, new Color(.2f,.2f,.2f)),
    WATER(9, .3d, new Color(0.0f, 0.54901963f, 1.0f)),
    WATER_RUNOFF (10, 0d, new Color(0.0f, 0.54901963f, 1.0f)),
    FLOODFILLED_AIR(11, 0d, new Color(0.22745098f, 0.9019608f, 1f)),
    PLACEHOLDER_AIR (12, 0d, new Color(0.22745098f, 0.9019608f, 1f)); // DECOMMISSIONED!

    private static final long serialVersionUID = 444L;

	BlockType(int i, double _prevalence, Color _color) {
		integer = i;
        prevalence = _prevalence;
        color = _color;
	}
	private static final Map lookup = new HashMap();
    private static EnumSet<BlockType> existentBlockTypes;
    private static EnumSet<BlockType> generatedSolidTypes;
    public static final BlockType[] SolidTypes = new BlockType[] { DIRT, GRASS, SAND, STONE, CAVESTONE, LANTERN, BEDROCK };
    private static double TotalPrevalence;
    private int integer;
    private double prevalence;
    private Color color;
    public static Map colorLookup = new HashMap<Color, BlockType>();

	// Populate the lookup table on loading time
	static {
		for (BlockType bt : EnumSet.allOf(BlockType.class)) {
            lookup.put(bt.integer, bt);
        }
        TotalPrevalence = totalPrevalence(getExistentBlockTypes());
        for (BlockType bt : EnumSet.allOf(BlockType.class)) {
            colorLookup.put(bt.color, bt);
        }
	}
    public static EnumSet<BlockType> getExistentBlockTypes() {
        if (existentBlockTypes == null) {
            existentBlockTypes = EnumSet.allOf(BlockType.class);
            existentBlockTypes.remove(NON_EXISTENT);
            existentBlockTypes.remove(PLACEHOLDER_AIR);
        }
        return existentBlockTypes;
    }
    public static EnumSet<BlockType> getGeneratedSolidTypes() {
        if (generatedSolidTypes == null) {
            generatedSolidTypes = EnumSet.of(DIRT, GRASS, SAND, STONE, CAVESTONE, BEDROCK, WATER);
        }
        return generatedSolidTypes;
    }
    public static Map<BlockType, Double> prevalenceTableForGeneratedSolidTypes() {
        return prevalenceTable(getGeneratedSolidTypes(), totalPrevalence(getGeneratedSolidTypes()));
    }
    private static double totalPrevalence(Set<BlockType> blockTypeSet) {
        double result = 0d;
        Iterator<BlockType> iterator = blockTypeSet.iterator();
        while(iterator.hasNext()) {
            result += iterator.next().prevalence;
        }
        return result;
    }
    public static Map<BlockType, Double> prevalenceTable(Set<BlockType> blockTypeSet) {
        return prevalenceTable(blockTypeSet, totalPrevalence(blockTypeSet));
    }
    private static Map<BlockType, Double> prevalenceTable(Set<BlockType> blockTypeSet, double totalPrevalence) {
        Iterator<BlockType> iterator = blockTypeSet.iterator();
        Map<BlockType, Double> result = new HashMap<BlockType, Double>(blockTypeSet.size());
        while(iterator.hasNext()) {
            BlockType bt = iterator.next();
            result.put(bt, bt.prevalence / totalPrevalence);
        }
        return result;
    }

	public static BlockType get(int i) {
		return (BlockType) lookup.get(i);
	}

    public static Color terrainDemoColor(BlockType blockType) { return terrainDemoColor(blockType.ordinal()); }


    public static float LiquidFlowTimeStepSeconds(int i) {
        switch(BlockType.get(i)) {
            case WATER:
            case WATER_RUNOFF:
                return 2f;
            default:
                return Float.MAX_VALUE;
        }
    }

	public boolean equals(int i) {
		return this.ordinal() == i;
	}
	
	public static boolean IsTranslucent(int i) {
		return i == AIR.ordinal() || i == NON_EXISTENT.ordinal() || i == PLACEHOLDER_AIR.ordinal() || i == FLOODFILLED_AIR.ordinal() || IsWaterType(i);
	}
    public static boolean IsWaterSurface(int i) {
        return !IsWaterType(i);
    }
    public static boolean IsNonExistentOrPlaceHolderAir(int i) { return i == NON_EXISTENT.ordinal() || i == PLACEHOLDER_AIR.ordinal(); }
    public static boolean IsFloodFilledAir(int i) { return i == FLOODFILLED_AIR.ordinal(); }

	public static boolean IsAirOrNonExistent(int i) {
		return i == AIR.ordinal() || i == NON_EXISTENT.ordinal() || i == FLOODFILLED_AIR.ordinal() ||  i == PLACEHOLDER_AIR.ordinal();
	}
    public static boolean IsRenderedType(int i) {
        return !IsAirOrNonExistent(i);
    }
    public static boolean AcceptsWater(int i) {
        return i == AIR.ordinal() || i == FLOODFILLED_AIR.ordinal() || i == PLACEHOLDER_AIR.ordinal() || IsWaterType(i); // || i == NON_EXISTENT.ordinal();
    }

	public static boolean IsSolid(int i) {
		return !IsAirOrNonExistent(i) && !IsWaterType(i);
	}

    public static boolean IsBreakAble(int i) {
        return !IsAirOrNonExistent(i) && i != WATER_RUNOFF.ordinal();
    }
    public static boolean AcceptsPlaceBlock(int i) {
        return IsSolid(i);
    }

    public static boolean IsPlaceable(int i) {
        return IsSolid(i) || BlockType.WATER.ordinal() == i;
    }
	
	public static boolean IsEmpty(int i) {
		return NON_EXISTENT.ordinal() == i;
	}
	
	public float getFloat() { return (float) this.ordinal(); }
	
	public static int LightLevelForType(int type) {
        if (type == LANTERN.ordinal()) return SunLightComputer.MAX_LIGHT;
        return 0;
	}
    public static int WaterLevelForType(int type) {
        if (type == WATER.ordinal()) return WaterFlowComputer.MAX_WATER_LEVEL;
        return 0; //NOTE: WATER_RUNOFF should have zero level (by type at least) right? (Can't be placed)
    }

    public static boolean IsWaterType(int i) { return i >= WATER.ordinal(); }

    public static int NextPlaceableBlockFrom(int block) {
        BlockType[] blockTypes = BlockType.values();
        int result;
        BlockType blockType;
        do {
            blockType = blockTypes[(++block) % blockTypes.length];
            result = blockType.ordinal();
        } while (!IsPlaceable(result));
        return result;
    }

    public static boolean IsAir(int block) {
        return BlockType.AIR.ordinal() == block; // || BlockType.FLOODFILLED_AIR.ordinal() == block;
    }

    public Color color() { return color; }
    public static Color terrainDemoColor(int i) {
        if (!lookup.containsKey(i)) return null;
        return BlockType.get(i).color;
    }
}