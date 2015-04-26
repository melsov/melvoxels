package voxel.landscape.collection;

import voxel.landscape.collection.coordmap.managepages.ConcurrentHashMapCoord2D;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;

import java.util.Set;

public class ColumnMap {
    private ConcurrentHashMapCoord2D<ColumnChunk> columns = new ConcurrentHashMapCoord2D<ColumnChunk>(ColumnChunk.class);
    public int columnCount() { return columns.size(); }

    public Set<Coord2> getCoordXZSet() { return (Set<Coord2>) columns.keySet(); }

    /*
     * SET
     */
//    public void SetWroteData(int x, int z) {
//        GetColumnChunk(x, z).buildStatus.set (BuildStatus.WROTE_DATA.ordinal());
//    }
//    public void SetWritingData(int x, int z) {
//        GetColumnChunk(x, z).buildStatus.set (BuildStatus.WRITING_DATA.ordinal());
//    }

	public void SetBuilt(int x, int z) {
		GetColumnChunk(x, z).buildStatus.set (BuildStatus.BUILT_DATA.ordinal());
	}

    public void SetBuildingData(int x, int z) {
        GetColumnChunk(x, z).buildStatus.set(BuildStatus.BUILDING_DATA.ordinal());
    }
	public void SetBuiltSurface(int x, int z) {
		GetColumnChunk(x, z).buildStatus.set(BuildStatus.BUILT_SURFACE_DATA.ordinal());
	}

    public synchronized boolean SetIsBuildingOrReturnFalseIfStartedAlready(int x, int z) {
        if (HasNotBeenStarted(x,z)) {
            SetBuildingData(x,z);
            return true;
        }
        return false;
    }
    public synchronized void SetIsBuiltIfLessThanBuilt(int x, int z) {
        if (NotYetBuilt(x,z)) {
            SetBuilt(x,z);
        }
    }

//    public synchronized void SetIsBuiltIfWroteData(int x, int z) {
//        if (WroteData(x, z)) {
//            SetBuilt(x, z);
//        }
//    }

    /*
     * INFO
     */
//    public boolean WroteData(int x, int z) {
//        return GetColumnChunk(x, z).buildStatus.get() == BuildStatus.WROTE_DATA.ordinal();
//    }
//    public boolean IsWritingData(int x, int z) {
//        return GetColumnChunk(x, z).buildStatus.get() == BuildStatus.WRITING_DATA.ordinal();
//    }
	public boolean IsBuilt(int x, int z) {
		return GetColumnChunk(x, z).buildStatus.get() >= BuildStatus.BUILT_DATA.ordinal();
	}
    public boolean HasNotBeenStarted(int x, int z) {
        return GetColumnChunk(x, z).buildStatus.get() == BuildStatus.HAS_NOT_BEEN_TOUCHED.ordinal();
    }
    public boolean IsBuiltOrIsBuilding(int x, int z) {
        return GetColumnChunk(x, z).buildStatus.get() > BuildStatus.HAS_NOT_BEEN_TOUCHED.ordinal();
    }
	public boolean HasBuiltSurface(int x, int z) {
		return GetColumnChunk(x, z).buildStatus.get() >= BuildStatus.BUILT_SURFACE_DATA.ordinal();
	}
    public boolean NotYetBuilt(int x, int z) {
        return GetColumnChunk(x, z).buildStatus.get() < BuildStatus.BUILT_DATA.ordinal();
    }

    /*
     * CLEAN-UP
     */
    public void Destroy(int x, int z) { columns.Remove(x,z); }
	
	public Coord3 GetClosestEmptyColumn(int cx, int cz, int rad) {
		Coord3 center = new Coord3(cx, 0, cz);
		Coord3 closest = null;
		for(int z=cz-rad; z<=cz+rad; z++) {
			for(int x=cx-rad; x<=cx+rad; x++) {
				Coord3 current = new Coord3(x, 0, z);
				int dis = (int) center.minus(current).distanceSquared();
				if(dis > rad*rad) continue;
//				if( IsBuilt(x, z) ) continue;
                if( IsBuiltOrIsBuilding(x, z) ) continue;
				if(closest == null) {
					closest = current;
				} else {
					int oldDis = (int) center.minus(closest).distanceSquared(); 
					if(dis < oldDis) closest = current;
				}
			}
		}
		return closest;
	}

	private ColumnChunk GetColumnChunk(int x, int z) {
		return columns.GetInstance(x, z);
	}


	
}
