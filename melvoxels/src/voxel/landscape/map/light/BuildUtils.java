package voxel.landscape.map.light;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.water.WaterFlowComputer;

public class BuildUtils {

	public static float[] GetSmoothVertexLight(TerrainMap map, Coord3 pos, Vector3f vertex, int face, boolean wantWater) {
		int dx = (int)Math.signum( vertex.x );
		int dy = (int)Math.signum( vertex.y );
		int dz = (int)Math.signum( vertex.z );
		
		Coord3 a, b, c, d;
		if(face == Direction.XNEG || face == Direction.XPOS) { // X
			a = pos.add(new Coord3(dx, 0,  0));
			b = pos.add(new Coord3(dx, dy, 0));
			c = pos.add(new Coord3(dx, 0,  dz));
			d = pos.add(new Coord3(dx, dy, dz));
		} else 
		if(face == Direction.YNEG || face == Direction.YPOS) { // Y
			a = pos.add(new Coord3(0,  dy, 0));
			b = pos.add(new Coord3(dx, dy, 0));
			c = pos.add(new Coord3(0,  dy, dz));
			d = pos.add(new Coord3(dx, dy, dz));
		} else { // Z
			a = pos.add(new Coord3(0,  0,  dz));
			b = pos.add(new Coord3(dx, 0,  dz));
			c = pos.add(new Coord3(0,  dy, dz));
			d = pos.add(new Coord3(dx, dy, dz));
		}
        ColorRGBA res;
        //If, for example, 'UP' or 'LEFT' neighbor blocks are translucent, then UP-AND-LEFT neighbor block matters
        if(map.blockAtWorldCoordIsTranslucent(b) || map.blockAtWorldCoordIsTranslucent(c)) {
            ColorRGBA c1 = GetBlockLight(map, a);
            ColorRGBA c2 = GetBlockLight(map, b);
            ColorRGBA c3 = GetBlockLight(map, c);
            ColorRGBA c4 = GetBlockLight(map, d);
            res = c1.add(c2).add(c3).add(c4).mult(.25f);
        } else {
            ColorRGBA c1 = GetBlockLight(map, a);
            ColorRGBA c2 = GetBlockLight(map, b);
            ColorRGBA c3 = GetBlockLight(map, c);
            res = c1.add(c2).add(c3).mult(.33f);
        }
        if (wantWater) {
            if (BlockType.WATER.ordinal() == map.lookupBlock(pos)) {
                res.g = 0f;
            } else {
                b = pos.add(new Coord3(0, 1, 0));
                float wb = GetWaterLevel(map, b);
                if (wb > 0) {
                    res.g = 0f;
                } else {
                    // re-use a,c,d to save allocations
                    a = pos.add(new Coord3(dx, 0, 0));
                    c = pos.add(new Coord3(0, 0, dz));
                    d = pos.add(new Coord3(dx, 0, dz));
                    float wa = GetWaterLevel(map, a);
                    float wc = GetWaterLevel(map, c);
                    float wd = GetWaterLevel(map, d);
                    float wp = GetWaterLevel(map, pos);
                    res.g = 1f - Math.max(Math.max(wd,wp), Math.max(wa, wc));
                }
            }
        }
        return res.toArray(null);

	}
    public static ColorRGBA GetBlockLight(TerrainMap map, Coord3 pos) {
		Coord3 chunkPos = Chunk.ToChunkPosition(pos);
		Coord3 localPos = Chunk.ToChunkLocalCoord(pos);
		float light = (float) map.GetLightmap().GetLight( chunkPos, localPos ) / (float) SunLightComputer.MAX_LIGHT;
		float sun = (float) map.GetSunLightmap().GetLight( chunkPos, localPos, pos.y ) / (float) SunLightComputer.MAX_LIGHT;
        return new ColorRGBA(0,0,light,sun);
	}
    public static float GetWaterLevel(TerrainMap map, Coord3 pos) {
        Coord3 chunkPos = Chunk.ToChunkPosition(pos);
        Coord3 localPos = Chunk.ToChunkLocalCoord(pos);
        return (float) (map.getLiquidLevelMap().GetWaterLevel(chunkPos, localPos) / (float) WaterFlowComputer.MAX_WATER_LEVEL);
    }
}
