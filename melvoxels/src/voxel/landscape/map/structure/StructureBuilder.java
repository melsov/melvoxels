package voxel.landscape.map.structure;

import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.VoxelLandscape;
import voxel.landscape.WorldGenerator;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.debug.DebugGeometry;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.structure.structures.AbstractStructure;
import voxel.landscape.noise.TerrainDataProvider;

import java.util.HashSet;
import java.util.Set;

import com.jme3.math.ColorRGBA;

import static voxel.landscape.player.B.bug;
/**
 * Created by didyouloseyourdog on 3/28/15.
 */
public class StructureBuilder {

    private SurfaceStructureGenerator surfaceStructureGenerator = new SurfaceStructureGenerator();

    /*
     * Iterate over the x,z surface of a chunk column
     * check for and add structures to the chunk and possibly
     * adjacent chunks.
     */
    public void addStructures(Coord2 chunkColumn, TerrainMap map, TerrainDataProvider dataProvider, HashSet<Coord3> touchedChunkCoords) {
        // TODO: contemplate how to really deal with detecting already-built-from-file chunks
        if (WorldGenerator.TEST_DONT_BUILD || !VoxelLandscape.BUILD_STRUCTURES) return;

        if (map.columnChunksBuiltFromFile(chunkColumn.getX(), chunkColumn.getZ())) {
            return;
        }
        // TODO: CONSIDER: are we deleting light and water map data when we delete chunks?
        int x1 = chunkColumn.getX() * Chunk.CHUNKDIMS.x;
        int z1 = chunkColumn.getZ() * Chunk.CHUNKDIMS.z;

        int x2 = x1+Chunk.CHUNKDIMS.x;
        int z2 = z1+Chunk.CHUNKDIMS.z;
        int surfaceY = 0;
        Set<Chunk> gotStructureChunks = new HashSet<>(4);
        for(int z=z1; z<z2; z++) {
            for(int x=x1; x<x2; x++) {
                surfaceY = map.getSurfaceHeight(x, z);
                Coord3 global = new Coord3(x, surfaceY, z);
                Chunk originChunk = map.lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(global));
                if (originChunk != null) {
                    gotStructureChunks.add(originChunk);
                    if (originChunk.hasAddedStructures.get()) {
                        touchedChunkCoords.add(originChunk.position);
                        continue;
                    }
                }
                AbstractStructure structure = surfaceStructureGenerator.structureAt(global);
                if (structure == null) continue;
                Coord3 shiftPlot = structure.viablePlot(global, map);
                if (shiftPlot == null) continue; //structure refuses to be placed
                global = global.add(shiftPlot);
                for (Coord3 structureLocal : structure.getBlocks().keySet()) {
                    BlockType blockType = structure.getBlocks().get(structureLocal);
                    Coord3 structureGlobal = global.add(structureLocal);
                    Chunk chunk = map.lookupOrCreateChunkAtPosition(Chunk.ToChunkPosition(structureGlobal));
                    if (chunk == null) {
                    	bug("*\n");
                    	continue;
                    }
                    map.setBlockUpdateSurface(blockType.ordinal(), structureGlobal);
                    chunk.chunkBlockFaceMap.addExposedFacesUpdateNeighbors(structureGlobal, map);
                    touchedChunkCoords.add(Chunk.ToChunkPosition(structureGlobal));
                }
            }
        }
        for (Chunk chunk : gotStructureChunks) {
            if (chunk != null) {
                chunk.hasAddedStructures.set(true);
            }
        }

    }
}
