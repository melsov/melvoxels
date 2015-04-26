package voxel.landscape.chunkbuild.blockfacefind.floodfill.chunkslice;

import voxel.landscape.chunkbuild.bounds.XZBounds;
import voxel.landscape.coord.Coord3;
import voxel.landscape.player.B;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by didyouloseyourdog on 10/9/14.
 */
public class ChunkSliceBag {
    private List<ChunkSlice> slices = new ArrayList<ChunkSlice>(128);
    public final XZBounds xzBounds;
//    private Box bounds;
//    public Box getBounds() { return bounds; }
//    public void setBounds(Box _bounds) {
//        //CONSIDER: for each out of bounds slice remove?? maybe not nec. we can sort out while iterating...
//        bounds = _bounds;
//    }
    private ChunkSliceBag() {
        this(null);
    }
    public ChunkSliceBag(XZBounds _xzBounds) {
        xzBounds = _xzBounds;
    }
    public static ChunkSliceBag ChunkSliceBagWithBounds(XZBounds _xzBounds) {
        return new ChunkSliceBag(_xzBounds);
    }
//    public static ChunkSliceBag ChunkSliceBagWithBounds(Box _bounds) {
//        ChunkSliceBag chunkSliceBag = new ChunkSliceBag();
//        chunkSliceBag.bounds = _bounds;
//        return chunkSliceBag;
//    }
    public static ChunkSliceBag UnboundedChunkSliceBag() {
        return new ChunkSliceBag();
    }
    public boolean add(ChunkSlice chunkSlice) {
        if (contains(chunkSlice.getChunkCoord())) {
            slices.add(chunkSlice);
            return true;
        }
        return false;
    }
    private boolean contains(Coord3 co) {
        if (xzBounds == null) return true;
        return xzBounds.contains(co);
//        return bounds.contains(co);
    }
    public ChunkSlice removeNext() {
        if (slices.size() == 0) return null;
        return slices.remove(0);
    }
    public List<ChunkSlice> getSlices() { return slices; }
    public ChunkSlice remove(int index) {
        return slices.remove(index);
    }
    public int size() { return slices.size(); }

    public void debugListSlices() {
        StringBuilder sb = new StringBuilder(120);
        sb.append("Chunk Slice Bag:: \n");
        for(ChunkSlice chunkSlice : slices) {
            sb.append(chunkSlice.getChunkCoord().toString());
            sb.append("\n");
        }
        B.bugln(sb.toString());
    }


}
