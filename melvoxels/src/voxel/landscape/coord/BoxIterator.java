package voxel.landscape.coord;

import java.util.Iterator;

/**
 * Created by didyouloseyourdog on 10/13/14.
 */
public class BoxIterator implements Iterator<Coord3> {

    public Box box;
    private Coord3 index; // = new Coord3(0);

    public int getSize() { return box.dimensions.x * box.dimensions.y * box.dimensions.z; }

    public BoxIterator(Box _box) {
        box = _box;
        index = box.start.clone();
    }

    @Override
    public boolean hasNext() {
        return index.y < box.extent().y;
    }

    private void incrementMarker() {
        index.x++;
        if (index.x == box.extent().x) {
            index.x = box.start.x;
            index.z++;
            if (index.z == box.extent().z) {
                index.z = box.start.z;
                index.y++;
            }
        }
    }
    @Override
    public Coord3 next() {
        if (!hasNext()) return null;
        Coord3 result = index.clone();
        incrementMarker();
        return result;
    }

    @Override
    public void remove() {
        incrementMarker();
//        Asserter.assertFalseAndDie("one doesn't remove from a Box Iterator. Just not done.");
    }
}
