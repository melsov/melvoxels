package voxel.landscape.collection;

import java.util.concurrent.atomic.AtomicInteger;

public class ColumnChunk {
//	public boolean built = false;
//    public volatile int buildStatus = 0;
    public volatile AtomicInteger buildStatus = new AtomicInteger(0);
}

