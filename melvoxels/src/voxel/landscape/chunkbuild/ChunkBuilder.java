package voxel.landscape.chunkbuild;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.MeshSet;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.util.Asserter;

import static voxel.landscape.Chunk.*;

public class ChunkBuilder 
{
	public static void buildMesh(Chunk chunk, MeshSet mset, MeshSet waterMSet, boolean lightOnly, boolean liquidOnly)
	{
        Asserter.assertFalseAndDie("not calling build mesh in chunk builder");
		int xin = 0, yin = 0, zin = 0;
		Coord3 posi;
		int triIndex = 0, waterTriIndex = 0;
		int i = 0, j = 0, k = 0;
		
		TerrainMap map = chunk.getTerrainMap();
		Coord3 worldPosBlocks = chunk.originInBlockCoords();

		for(i = 0; i < Chunk.XLENGTH; ++i) {
			for(j = 0; j < Chunk.ZLENGTH; ++j) {
				for (k = 0; k < Chunk.YLENGTH; ++k) {
					xin = i + worldPosBlocks.x; yin = k  + worldPosBlocks.y; zin = j  + worldPosBlocks.z;
					posi = new Coord3(i,k,j);
//					byte btype = (byte) map.lookupOrCreateBlock(xin, yin, zin); // <--WANT
                    byte btype = (byte) map.lookupBlock(xin, yin, zin); //TEST
                    if (BlockType.NON_EXISTENT.equals(btype)) continue;

//					if (BlockType.AIR.equals(btype)) continue;
                    if (BlockType.IsAirOrNonExistent(btype)) continue;

                    Coord3 worldcoord = new Coord3(xin, yin, zin);
                    if (BlockType.IsWaterType(btype)) {
                        for (int dir = 0; dir <= Direction.ZPOS; ++dir) {
                            if (IsWaterSurfaceFace(map, worldcoord, dir)) {
                                if (!lightOnly) BlockMeshUtil.AddFaceMeshData(posi, waterMSet, btype, dir, waterTriIndex, map);
                                BlockMeshUtil.AddFaceMeshLightData(worldcoord, waterMSet, dir, map);
                                waterTriIndex += 4;
                            }
                        }
                        if (liquidOnly) continue;
                    } else {
                        for (int dir = 0; dir <= Direction.ZPOS; ++dir) {
                            if (IsFaceVisible(map, worldcoord, dir)) {
                                if (!lightOnly) BlockMeshUtil.AddFaceMeshData(posi, mset, btype, dir, triIndex, map);
                                BlockMeshUtil.AddFaceMeshLightData(worldcoord, mset, dir, map);
                                triIndex += 4;
                            }
                        }
                    }
				}
			}
		}
	}


//for(i = 0; i < Chunk.XLENGTH; ++i) {
//    for(j = 0; j < Chunk.ZLENGTH; ++j) {
//        for (k = 0; k < Chunk.YLENGTH; ++k) {
//            // ....set-up code ..... //
//                for (int dir = 0; dir <= Direction.ZPOS; ++dir) {
//                    if (IsFaceVisible(map, worldCoord, dir)) {
//                        BlockMeshUtil.AddFaceMeshData(posi, mset, btype, dir, triIndex, map);
//
//                        /* ****ADD LIGHT DATA AT worldCoord TO THE MESHSET (mset)**** */
//                        BlockMeshUtil.AddFaceMeshLightData(worldCoord, mset, dir, map);
//
//                        triIndex += 4;
//                    }
//                }
//            }
//        }
//    }
//}

//    /*
//     * Chunk Block Face Map Build
//     */
//    public static void buildMeshFromFaceMap(Chunk chunk, MeshSet mset, MeshSet waterMSet, boolean lightOnly, boolean liquidOnly) {
//        TerrainMap map = chunk.getTerrainMap();
//        Coord3 worldCoord = chunk.originInBlockCoords();
//        int triIndex = 0, waterTriIndex = 0;
//        Iterator<Map.Entry<ChunkBlockFaceCoord, BlockFaceRecord>> iterator = chunk.chunkBlockFaceMap.iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<ChunkBlockFaceCoord, BlockFaceRecord> entry = iterator.next();
//            ChunkBlockFaceCoord blockFaceCoord = entry.getKey();
//            BlockFaceRecord faceRecord = entry.getValue();
//            Coord3 blockWorldCoord = worldCoord.add(blockFaceCoord.toCoord3());
//            byte blockType = (byte) map.lookupOrCreateBlock(blockWorldCoord);
//
//            if (BlockType.AIR.equals(blockType)) continue;
//
//            if (BlockType.IsWaterType(blockType)) {
//                for (int dir : Direction.Directions) {
//                    if (IsWaterSurfaceFace(map, worldCoord, dir)) {
//                        if (!lightOnly) BlockMeshUtil.AddFaceMeshData(blockFaceCoord.toCoord3(), waterMSet, blockType, dir, waterTriIndex, map);
//                        BlockMeshUtil.AddFaceMeshLightData(blockWorldCoord, waterMSet, dir, map);
//                        waterTriIndex += 4;
//                    }
//                }
//            } else {
//                for (int dir = 0; dir <= Direction.ZPOS; ++dir) {
//                    if (!lightOnly) BlockMeshUtil.AddFaceMeshData(blockFaceCoord.toCoord3(), mset, blockType, dir, triIndex, map);
//                    BlockMeshUtil.AddFaceMeshLightData(blockWorldCoord, mset, dir, map);
//                    triIndex += 4;
//                }
//            }
//        }
//
//    }
	
	public static void ApplyMeshSet(MeshSet mset, Mesh bigMesh, boolean lightOnly)
    {
        if (bigMesh == null) {
            Asserter.assertFalseAndDie("trying to apply a mesh set to a null mesh");
            return;
        }
		if (!lightOnly) 
		{
			bigMesh.clearBuffer(Type.Position);
			bigMesh.clearBuffer(Type.TexCoord);
//			bigMesh.clearBuffer(Type.TexCoord2);
			bigMesh.clearBuffer(Type.Index);
			bigMesh.clearBuffer(Type.Normal);
			
			bigMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(mset.vertices.toArray(new Vector3f[0])));
			bigMesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(mset.uvs.toArray(new Vector2f[0])));
            /* Pre purge tex map offset */
//			bigMesh.setBuffer(Type.TexCoord2, 2, BufferUtils.createFloatBuffer(mset.texMapOffsets.toArray(new Vector2f[0])));
	
			// google guava library helps with turning Lists into primitive arrays
			// "Ints" and "Floats" are guava classes. 
			bigMesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(Ints.toArray(mset.indices)));
			bigMesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(mset.normals.toArray(new Vector3f[0])));
		}
		bigMesh.clearBuffer(Type.Color);
		bigMesh.setBuffer(Type.Color, 4, Floats.toArray(mset.colors));

//		bigMesh.setDynamic();
//		bigMesh.setMode(Mesh.Mode.Triangles);
        BoundingBox bbox = new BoundingBox(new Vector3f(0,0,0), new Vector3f(XLENGTH, YLENGTH, ZLENGTH));
        bigMesh.setBound(bbox);
		bigMesh.updateBound();

	}


    public static void ClearBuffers(Mesh bigMesh) {
        if (bigMesh == null) return;
        bigMesh.clearBuffer(Type.Position);
        bigMesh.clearBuffer(Type.TexCoord);
        bigMesh.clearBuffer(Type.TexCoord2);
        bigMesh.clearBuffer(Type.Index);
        bigMesh.clearBuffer(Type.Normal);
        bigMesh.clearBuffer(Type.Color);
    }

    private static boolean IsWaterSurfaceFace(TerrainMap terrainMap, Coord3 woco, int direction) {
        byte btype = (byte) terrainMap.lookupOrCreateBlock(woco.add(Direction.DirectionCoordForDirection(direction)));
        return BlockType.IsWaterSurface(btype);
    }

	private static boolean IsFaceVisible(TerrainMap terrainMap, Coord3 woco, int direction) {
		byte btype = (byte) terrainMap.lookupOrCreateBlock(woco.add(Direction.DirectionCoordForDirection(direction))); 
		return BlockType.IsTranslucent(btype);
	}
}
