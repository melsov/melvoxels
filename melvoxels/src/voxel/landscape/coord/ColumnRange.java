package voxel.landscape.coord;

import voxel.landscape.map.TerrainMap;
import voxel.landscape.util.Asserter;

import java.util.Iterator;

/**
 * Created by didyouloseyourdog on 4/11/15.
 */
public class ColumnRange implements Iterable<Coord3> {

    private Coord3 column;
    private ICoordXZ iCoordXZ;
    public ColumnRange(ICoordXZ iCoordXZ) {
        this.iCoordXZ = iCoordXZ;
    }
    private Coord3 getColumn() {
        if (column == null) {
            column = new Coord3(iCoordXZ.getX(), TerrainMap.MIN_CHUNK_COORD.y, iCoordXZ.getZ());
        }
        return column;
    }
    @Override
    public Iterator<Coord3> iterator() {
        Iterator<Coord3> it = new Iterator<Coord3>() {
            @Override
            public boolean hasNext() {
                return getColumn().y < TerrainMap.MAX_CHUNK_COORD.y;
            }

            @Override
            public Coord3 next() {
                Coord3 result = getColumn().clone();
                getColumn().y++;
                return result;
            }

            @Override
            public void remove() {
                getColumn().y++;
            }
        };
        return it;
    }
}
