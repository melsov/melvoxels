package voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkseeds;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.fileutil.FileUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by didyouloseyourdog on 10/14/14.
 * A collection of ChunkFloodFillSeedBlob3Ds.
 * Handles adding blobs and combining contiguous blobs.
 */
public class ChunkFloodFillSeedSet implements Serializable {
    private Coord3 chunkCoord;
    public Coord3 getChunkCoord() { return chunkCoord; }
    private ArrayList<ChunkFloodFillSeedBlob3D> seeds = new ArrayList<>(7);
    public final AtomicBoolean writeDirty = new AtomicBoolean(true);

    public ChunkFloodFillSeedSet(Coord3 _chunkCoord) {
        chunkCoord = _chunkCoord;
    }

    public void addCoord(Coord3 global){
        Coord3 local = Chunk.ToChunkLocalCoord(global);
        ChunkFloodFillSeedBlob3D adjacentFloodFillSeed = null;
        for (int i = 0; i < seeds.size(); ++i) {
            ChunkFloodFillSeedBlob3D floodFillSeed = seeds.get(i);
            // Is this coord adjacent to an existing FloodFillSeedBlob?
            if (adjacentFloodFillSeed == null && floodFillSeed.addCoord(local)) {
                adjacentFloodFillSeed = floodFillSeed;
            // else if we did find an adjacent blob already,
            // is the current one also adjacent? if so, combine.
            } else if (floodFillSeed.isCoordAdjacent(global)) {
                adjacentFloodFillSeed.addMembersOfSet(floodFillSeed);
                seeds.remove(i--);
            }
        }
        if (adjacentFloodFillSeed == null) {
            seeds.add(new ChunkFloodFillSeedBlob3D(local));
        }
        writeDirty.set(true);
    }

    public Coord3 removeNext() {
        if (seeds.size() == 0) return null;
        writeDirty.set(true);
        return Chunk.ToWorldPosition(chunkCoord, seeds.remove(0).getSeed());
    }
    public int size() {
        return seeds.size();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ChunkSlice: Chunk coord: ");
        sb.append(chunkCoord.toString());
        sb.append(" block set size: ");
        sb.append(seeds.size());
        return sb.toString();
    }

    /*
     * Read/Write
     */
    public void readFromFile(Coord3 position) {
        Object seedsO = FileUtil.DeserializeChunkObject(position, FileUtil.ChunkFloodFillSeedSetExtension);
        if (seedsO != null) {
            seeds = (ArrayList<ChunkFloodFillSeedBlob3D>) seedsO;
            writeDirty.set(false);
        }
    }

    public void writeToFile(Coord3 position) {
        try {
            FileUtil.SerializeChunkObject(seeds, position, FileUtil.ChunkFloodFillSeedSetExtension);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        writeDirty.set(false);
    }


}
