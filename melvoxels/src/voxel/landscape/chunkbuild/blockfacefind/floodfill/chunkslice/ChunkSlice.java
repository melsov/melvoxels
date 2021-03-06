package voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkslice;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.util.Asserter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by didyouloseyourdog on 10/9/14.
 * Manages a collection of ChunkSliceBlockSets
 * for a 16x16 slice of blocks,
 * representing the face of a chunk
 */

public class ChunkSlice
{
    private Coord3 chunkCoord;
    public Coord3 getChunkCoord() { return chunkCoord; }
    private final int direction;
    private final int planePositionGlobal;
    public final Coord3 global;
    public int getDirection() { return direction; }
    private List<ChunkSliceBlockSet> blockSets = new ArrayList<ChunkSliceBlockSet>(3);

    public ChunkSlice(int _direction, Coord3 global) {
        chunkCoord = Chunk.ToChunkPosition(global); direction = _direction;
        planePositionGlobal = global.componentForDirection(direction);
        this.global = global;
        
    }
    
    public void addCoord(Coord3 global){
    	Asserter.assertTrue(chunkCoord.equal(Chunk.ToChunkPosition(global)), "wrong chunk: slice chunk: " + chunkCoord.toString() + " global: " 
			    + global.toString() + " global's chunk: " + Chunk.ToChunkPosition(global));
    	Asserter.assertTrue(planePositionGlobal == global.componentForDirection(direction), 
    			"wrong global position: global plane is at " + planePositionGlobal + " add coord: " 
		    	+ global.toString() + "\nfor direction: " + Direction.ToString(direction) );
        ChunkSliceBlockSet addedToSliceBlockSet = null;
        for (int i = 0; i < blockSets.size(); ++i) {
            ChunkSliceBlockSet chunkSliceBlockSet = blockSets.get(i);
            if (addedToSliceBlockSet == null) {
                if (chunkSliceBlockSet.addCoord(global)) {
                    addedToSliceBlockSet = chunkSliceBlockSet;
                }
            } else {
                if (chunkSliceBlockSet.isCoordAdjacent(global)) {
                    addedToSliceBlockSet.addMembersOfSet(chunkSliceBlockSet);
                    blockSets.remove(i--);
                }
            }
        }
        if (addedToSliceBlockSet == null) {
            blockSets.add(new ChunkSliceBlockSet(global, Direction.AxisForDirection(direction)));
        }
    }

    public Coord3 removeNext() {
        if (blockSets.size() == 0) return null;
        return blockSets.remove(0).getSeedGlobal();
    }
    public int size() {
        return blockSets.size();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ChunkSlice: Chunk coord: ");
        sb.append(chunkCoord.toString());
        sb.append(" block set size: ");
        sb.append(blockSets.size());
        sb.append(" direction: ");
        sb.append(direction);
        return sb.toString();
    }
}
