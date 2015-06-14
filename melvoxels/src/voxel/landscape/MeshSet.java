package voxel.landscape;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import java.util.ArrayList;

public class MeshSet {
    private static int INITIAL_CAPACITY = Chunk.XLENGTH * Chunk.YLENGTH * Chunk.ZLENGTH * 4 * 6 / 6;
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>(INITIAL_CAPACITY);
    public ArrayList<Integer> indices = new ArrayList<Integer>((int) (INITIAL_CAPACITY * 1.5));
    public ArrayList<Vector2f> uvs = new ArrayList<Vector2f>(INITIAL_CAPACITY);
    public ArrayList<Vector2f> texMapOffsets = new ArrayList<Vector2f>(INITIAL_CAPACITY);
    public ArrayList<Float> colors = new ArrayList<Float>(INITIAL_CAPACITY);
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public boolean isLiquidMaterial, isOnlyLightUpdate;

    public MeshSet() {
        this(false, false);
    }
    public MeshSet( boolean _isLiquidMaterial, boolean _isOnlyLightUpdate) {
        isLiquidMaterial = _isLiquidMaterial;
        isOnlyLightUpdate = _isOnlyLightUpdate;
    }
    
    public boolean validData() {
    	if (vertices.size() == 0) return false;
    	return true;
    }
    
}
//public class MeshSet
//{
//	public Vector<Vector3f> vertices = new Vector<Vector3f>();
//	public Vector<Integer> indices = new Vector<Integer>();
//	public Vector<Vector2f> uvs = new Vector<Vector2f>();
//	public Vector<Vector2f> texMapOffsets = new Vector<Vector2f>();
//	public Vector<Float> colors = new Vector<Float>();
//	public Vector<Vector3f> normals = new Vector<Vector3f>();
//    public boolean isLiquidMaterial, isOnlyLightUpdate;
////    public Coord3 chunkPosition;
//
//    public MeshSet() {
//        this(false, false);
//    }
//    public MeshSet( boolean _isLiquidMaterial, boolean _isOnlyLightUpdate) {
////        chunkPosition = _chunkPosition;
//        isLiquidMaterial = _isLiquidMaterial;
//        isOnlyLightUpdate = _isOnlyLightUpdate;
//    }
//}
