package voxel.landscape.chunkbuild;

import com.jme3.bounding.BoundingBox;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.AbstractControl;

import voxel.landscape.Chunk;
import voxel.landscape.MeshSet;
import voxel.landscape.VoxelLandscape;
import voxel.landscape.chunkbuild.meshbuildasync.ChunkMeshBuildingSet;
import voxel.landscape.player.B;
import voxel.landscape.util.Asserter;

/*
 * Build (or rebuild) a mesh for the chunk
 * and set and reset our geometry's ('spatial's') mesh to the (re)built mesh.
 */
public class ChunkBrain extends AbstractControl implements Cloneable, Savable, ThreadCompleteListener 
{
	private Chunk chunk;
	private boolean dirty, lightDirty, liquidDirty;

    public boolean isHiding() {
        if (getSpatial() == null) return false;
        return getSpatial().getParent() == null;
    }
    
    public boolean hasMeshData() {
    	Geometry g = getGeometryUnsafe();
    	if (g != null) {
	    	Mesh mesh = g.getMesh();
	    	if (mesh != null) {
		    	VertexBuffer vb = mesh.getBuffer(Type.Position);
		    	if (vb != null) {
			    	if (vb.getNumComponents() > 0) return true;
		    	}
	    	}
    	}
    	g = getWaterGeometryUnsafe();
    	if (g != null) {
	    	Mesh mesh = g.getMesh();
	    	if (mesh != null) {
		    	VertexBuffer vb = mesh.getBuffer(Type.Position);
		    	if (vb != null) {
			    	if (vb.getNumComponents() > 0) return true;
		    	}
	    	}
    	}
    	return false;
    }

    public ChunkBrain(Chunk _chunk) {
		chunk = _chunk;
	}

	@Override
	protected void controlUpdate(float timePerFrame) {
		if(dirty) {
			buildMesh(false, false);
			dirty = lightDirty = liquidDirty = false;
		}
		if(lightDirty) {
			buildMeshLight();
			lightDirty = false;
		}
        if (liquidDirty) {
            buildMeshLiquid();
            liquidDirty = false;
        }
	}

	@Override
	protected void controlRender(RenderManager renderManager, ViewPort viewPort) {
		//do nothing here. must override however.
	}

    public void setMeshEmpty() {
        getGeometry().setMesh(makeEmptyMesh());
    }

    private Mesh makeEmptyMesh() {
        Mesh mesh = new Mesh();
        mesh.setDynamic();
        mesh.setMode(Mesh.Mode.Triangles);
        return mesh;
    }

	private Mesh getMesh() {
		return getGeometry().getMesh();
	}
	public Geometry getGeometry() {
        Node node = getChunkBrainRootNode();
        if (node == null) return null;
        Geometry geometry = (Geometry) node.getChild("chunk_geom");
        if (geometry == null) {
            geometry = new Geometry("chunk_geom", makeEmptyMesh());
            node.attachChild(geometry);
        }
        return geometry;
    }
	private Geometry getGeometryUnsafe() {
		if (getSpatial() == null) return null;
		return (Geometry) ((Node) getSpatial()).getChild("chunk_geom");
	}
	private Geometry getWaterGeometryUnsafe() {
		if (getSpatial() == null) return null;
		return (Geometry) ((Node) getSpatial()).getChild("water_geom");
	}
    public synchronized Geometry getWaterGeometry() {
        Node node = getChunkBrainRootNode();
        if (node == null) return null;
        Geometry waterGeom = (Geometry) node.getChild("water_geom");
        if (waterGeom == null) {
            waterGeom = new Geometry("water_geom", makeEmptyMesh());
            waterGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
            node.attachChild(waterGeom);
        }
        return waterGeom;
    }
    private Mesh getWaterMesh() {
        return getWaterGeometry().getMesh();
    }
    public Node getRootSpatial() {
        return getChunkBrainRootNode();
    }

    private synchronized Node getChunkBrainRootNode() {
        Node node = (Node) getSpatial();
        if (node == null) {
            node = new Node();
            node.move(Chunk.ToWorldPosition(chunk.position).toVector3());
            node.addControl(this);
        }
        return node;
    }

    public void wakeUp() { getChunkBrainRootNode(); }

    public void attachToTerrainNode(Node terrainNode) {
        terrainNode.attachChild(getRootSpatial());
    }
    public void attachTerrainMaterial(Material terrainMaterial) {
        getGeometry().setMaterial(terrainMaterial);
    }
    public void attachWaterMaterial(Material waterMaterial) {
        getWaterGeometry().setMaterial(waterMaterial);
    }


	private void buildMeshLight() { buildMesh(true, false); }

    private void buildMeshLiquid() { buildMesh(false, true); }


	private void buildMesh(boolean onlyLight, boolean onlyLiquid) {
        if (VoxelLandscape.DO_USE_TEST_GEOMETRY) return;

        if (VoxelLandscape.SHOULD_BUILD_CHUNK_MESH_ASYNC) {
            enqueueChunkBuildMeshSets(onlyLight, onlyLiquid);
            return;
        }
        /* ************ */

		MeshSet mset = new MeshSet( false, onlyLight);
        MeshSet waterMSet = new MeshSet( true, onlyLight);

        ChunkBuilder.buildMesh(chunk, mset, waterMSet, onlyLight, onlyLiquid);

        if (!onlyLiquid) {
            applyMeshSet(mset);
        }

        applyMeshSet(waterMSet);
	}
    public void applyMeshBuildingSet(ChunkMeshBuildingSet chunkMeshBuildingSet) {
        if (!chunkMeshBuildingSet.isOnlyLiquid) {
            applyMeshSet(chunkMeshBuildingSet.meshSet);
        }
        applyMeshSet(chunkMeshBuildingSet.liquidMeshSet);
    }

    private void applyMeshSet(MeshSet meshSet) {
        Mesh mesh = meshSet.isLiquidMaterial ? getWaterMesh() : getMesh();
        ChunkBuilder.ApplyMeshSet(meshSet, mesh, meshSet.isOnlyLightUpdate);
        Geometry geom = meshSet.isLiquidMaterial ? getWaterGeometry() : getGeometry();
        if (geom == null) {
            B.bugln("null geom when trying to apply mesh");
            return;
        }
        geom.setModelBound(new BoundingBox(Vector3f.ZERO.clone(), new Vector3f(Chunk.XLENGTH,Chunk.YLENGTH,Chunk.ZLENGTH)));
        geom.updateModelBound();
    }
	public class AsyncBuildMesh extends ResponsiveRunnable {
		private boolean onlyLight;
		private MeshSet mset = new MeshSet(false, false);
        private MeshSet waterMSet = new MeshSet(true, false);
		public AsyncBuildMesh(boolean _onlyLight) {
			onlyLight = _onlyLight;
		}
		public MeshSet getMeshSet() { return mset; }
		public boolean getOnlyLight() { return onlyLight; }
		@Override
		public void doRun() {
			ChunkBuilder.buildMesh(chunk, mset, waterMSet, onlyLight, false);
		}
	}
	@Override
	public void notifyThreadComplete(ResponsiveRunnable responsizeRunnable) {
		if (responsizeRunnable.getClass() == AsyncBuildMesh.class) {
		}
	}

	public void SetLightDirty() {
		lightDirty=true;
	}
	
	public void SetDirty() {
		dirty=true;
	}

    public void SetLiquidDirty() { liquidDirty = true; }

    private void enqueueChunkBuildMeshSets(boolean onlyLight, boolean onlyLiquid) {
        ChunkMeshBuildingSet chunkMeshBuildingSet = new ChunkMeshBuildingSet();
        chunkMeshBuildingSet.isOnlyLiquid = onlyLiquid;
        chunkMeshBuildingSet.isOnlyLight = onlyLight;
        chunkMeshBuildingSet.chunkPosition = chunk.position;
        chunk.getTerrainMap().getApp().getWorldGenerator().enqueueChunkMeshSets(chunkMeshBuildingSet);
    }

    /*
     * Clean up
     */
    public void clearMeshBuffers() {
        ChunkBuilder.ClearBuffers(getMesh());
    }

    public void detachNodeFromParent() {
        Node g = (Node) getSpatial();
        if (g != null) {
            g.removeFromParent();
            //TEST
            Asserter.assertTrue(getSpatial() != null, "we want to still have a non-null spatial despite detaching.");
            Asserter.assertTrue(getSpatial().getParent() == null, "parent not null?");
        }
    }

    public void setSpatialNull() {
        setSpatial(null);
    }

}
