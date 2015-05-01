package voxel.landscape.debug;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

import voxel.landscape.Chunk;
import voxel.landscape.chunkbuild.MaterialLibrarian;
import voxel.landscape.coord.Coord3;
import voxel.landscape.map.TerrainMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by didyouloseyourdog on 10/18/14.
 */
public class DebugGeometry
{
    public static Node debugNode = new Node("debug-node");
    public static Node addChunkNode = new Node("add-node");
    public static Node removeChunkNode = new Node("remove-node");
    private static ConcurrentHashMap<Coord3, Geometry> addChunks = new ConcurrentHashMap<>(1024);
    private static ConcurrentHashMap<Coord3, Geometry> removeChunks = new ConcurrentHashMap<>(1024);

    public static MaterialLibrarian materialLibrarian;
    private static BlockingQueue<Geometry> geometries = new LinkedBlockingQueue<>(1024);
    private static Map<Coord3, Integer> addedCoords = new HashMap<>(1024);
    private final static Geometry blockCursor = new Geometry("cursor", new Sphere(18,16,.2f));

    private DebugGeometry() {
        blockCursor.setMaterial(materialLibrarian.wireFrameMaterialWithColor(ColorRGBA.Magenta));
        try {
            geometries.put(blockCursor);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    private static int TimesAddedToCoord(Coord3 co) {
        Integer times = addedCoords.get(co);
        if (times == null) {
            times = new Integer(0);
        }
        times = new Integer(times.intValue() + 1);
        addedCoords.put(co, times);
        return times.intValue();
    }
    public static void AddDebugChunk(Coord3 position, ColorRGBA color) {
        AddDebugBlock(Chunk.ToWorldPosition(position), color, Chunk.XLENGTH, true);
    }
    public static void AddDebugChunkSolid(Coord3 position, ColorRGBA color) {
        AddDebugBlock(Chunk.ToWorldPosition(position), color, Chunk.XLENGTH, true);
    }
    public static void AddDebugChunkSolidSkinny(Coord3 position, ColorRGBA color) {
        AddDebugBlockSkinny(Chunk.ToWorldPosition(position), color, Chunk.XLENGTH, true);
    }
    public static void AddDebugBlock(Coord3 position, ColorRGBA color) {
        AddDebugBlock(position, color, 1f, false);
    }
    public static void AddDebugBlock(Coord3 position, ColorRGBA color, float size, boolean solid) {
        Box s = new Box(new Vector3f(0,0,0), new Vector3f(size, size, size));
        Geometry g = new Geometry(position.toString());
        g.setMesh(s);
        AddDebugGeometry(g, position, color, solid);
    }
    public static void AddDebugBlockSkinny(Coord3 position, ColorRGBA color, float size, boolean solid) {
        Box s = new Box(new Vector3f(2,2,2), new Vector3f(3, 12, 4));
        Geometry g = new Geometry();
        g.setMesh(s);
        AddDebugGeometry(g, position, color, solid);
    }
    public static void AddDebugSphere(Coord3 position, ColorRGBA color, float radius) {
        Sphere s = new Sphere(24, 8, radius);
        Geometry g = new Geometry();
        g.setMesh(s);
        AddDebugGeometry(g, position, color);
    }
    private static final Vector3f half = new Vector3f(.5f, .5f, .5f);
    public static void AddDebugGeometry(Geometry g, Coord3 position, ColorRGBA color) {
        AddDebugGeometry(g, position, color, false);
    }
    public static void AddDebugGeometrySolidMaterial(Geometry g, Coord3 position, ColorRGBA color) {
        AddDebugGeometry(g, position, color, true);
    }
    public static void AddDebugGeometry(Geometry g, Coord3 position, ColorRGBA color, boolean solidMat) {
        int times = TimesAddedToCoord(position) - 1;
        Vector3f localTrans = position.toVector3().subtract(half).add(Vector3f.UNIT_XYZ.mult(times/100f));
        AddDebugGeometry(g, localTrans, color, solidMat);
    }
    public static void AddDebugGeometry(Geometry g, Vector3f localTrans, ColorRGBA color, boolean solidMat) {
        localTrans = localTrans.clone(); //.add(.5f, .5f, .5f);
        g.setLocalTranslation(localTrans);
        if (solidMat)
            g.setMaterial(materialLibrarian.solidMaterialWithColor(color));
        else
            g.setMaterial(materialLibrarian.wireFrameMaterialWithColor(color));
        try {
            geometries.put(g);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * debug chunk management
     */
    public static void AddAddChunk(Coord3 position) {
    	if (!addChunks.containsKey(position) && addChunkNode.getChild("box" + Chunk.ToWorldPosition(position).toString()) == null) {
	        Geometry g = makeChunkBox(position, ColorRGBA.Orange, false);
	        addChunks.put(position, g);
    	}
    }
    public static void AddRemoveChunk(Coord3 position) {
    	if (!removeChunks.containsKey(position) && removeChunkNode.getChild("box" + Chunk.ToWorldPosition(position).toString()) == null) {
	        Geometry g = makeChunkBox(position, ColorRGBA.Blue, false);
	        removeChunks.put(position, g);
    	}
    }
    public static void AddChunk(Coord3 position, ColorRGBA color) {
    	if (!addChunks.containsKey(position) && addChunkNode.getChild("box" + Chunk.ToWorldPosition(position).toString()) == null) {
    		Geometry g = makeChunkBox(position, color, true);
	        addChunks.put(position, g);
    	}
    }
    private static Geometry makeChunkBox(Coord3 position, ColorRGBA color, boolean solid) {
        Coord3 pos = position.clone();
        pos.y = TerrainMap.MIN_CHUNK_COORD.y;
        int hedge = 3;
        pos = Chunk.ToWorldPosition(pos);
        return makeBox(pos, color, Chunk.CHUNKDIMS.minus(new Coord3(hedge * 2)).toVector3(), solid);
    }
    
    private static Geometry makePoleMarker(Coord3 position, ColorRGBA color, boolean solid) {
        Coord3 pos = position.clone();
        pos.y = TerrainMap.MIN_CHUNK_COORD.y;
        int hedge = 5;
        pos = Chunk.ToWorldPosition(pos).add(hedge, 0, hedge);
        return makeBox(pos, color, new Vector3f(Chunk.XLENGTH - hedge * 2, TerrainMap.GetWorldHeightInBlocks(), Chunk.XLENGTH - hedge * 2), solid);
    }

    private static Geometry makeBox(Coord3 position, ColorRGBA color, Vector3f size, boolean solid) {
        Box s = new Box(new Vector3f(0,0,0), size);
        Geometry g = new Geometry("box" + position.toString());
        g.setMesh(s);
        if (solid)
            g.setMaterial(materialLibrarian.solidMaterialWithColor(color));
        else
            g.setMaterial(materialLibrarian.wireFrameMaterialWithColor(color));
        g.setLocalTranslation(position.toVector3());
        return g;
    }

    /*
     * update
     */
    public static void Update(float tpf) {
        Geometry g = null;
        g = geometries.poll();
        if (g != null) {
            debugNode.attachChild(g);
        }
        UpdateChunkDebug();
    }
    private static void UpdateChunkDebug() {
    	Geometry g = null;
    	if (addChunks.size() > 0)
	        g = addChunks.remove(addChunks.keys().nextElement());
        if (g != null) {
            if (addChunkNode.getChild(g.getName()) == null) {
                addChunkNode.attachChild(g);
            }
            if (removeChunkNode.getChild(g.getName()) != null) {
                removeChunkNode.detachChildNamed(g.getName());
            }
        }

        Geometry r = null;
        if (removeChunks.size() > 0)
		    r = removeChunks.remove(removeChunks.keys().nextElement());
        if (r != null) {
            if (removeChunkNode.getChild(r.getName()) == null) {
                removeChunkNode.attachChild(r);
            }
            if (addChunkNode.getChild(r.getName()) != null) {
                addChunkNode.detachChildNamed(r.getName());
            }
        }
    }
    // block pointer cursor
    public static void AddVector3fCursor(Vector3f global) {
        AddDebugGeometry(blockCursor.clone(), global, ColorRGBA.randomColor(), false);
    }
}
