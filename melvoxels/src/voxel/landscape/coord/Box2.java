package voxel.landscape.coord;

import voxel.landscape.util.Asserter;

import java.util.Iterator;

/**
 * Created by didyouloseyourdog on 12/29/14.
 */
public class Box2 implements Iterable<Coord2> {
    public Coord2 start;
    public Coord2 dimensions;

    public Box2(Coord2 _start, Coord2 _dims) {
        Asserter.assertTrue(_dims.positiveComponents(), "positive box dimensions please.");
        start = _start;
        dimensions = _dims;
    }
    public Coord2 extent() {
        return start.add(dimensions);
    }
    public boolean containsXZ(Coord3 co) {
        return contains(new Coord2(co.x, co.z));
    }
    public boolean contains(Coord2 coord2) {
        return coord2.greaterThanOrEqual(start) && extent().greaterThan(coord2);
    }
    public void centerOver(Coord2 coord2) {
        start = coord2.minus(dimensions.divideBy(2));
    }
    @Override
    public Box2 clone() {
        return new Box2(start.clone(), dimensions.clone());
    }
    @Override
    public Iterator<Coord2> iterator() {
        return new Box2Iterator();
    }

    class Box2Iterator implements Iterator<Coord2> {
        private Coord2 index;
        public Box2Iterator() {
            index = start.clone();
        }

        private void incrementMarker() {
            index.x++;
            if (index.x == extent().x) {
                index.x = start.x;
                index.y++;
            }
        }

        @Override
        public boolean hasNext() {
            return index.y < extent().y;
        }

        @Override
        public Coord2 next() {
            Coord2 result = index.clone();
            incrementMarker();
            return result;
        }

        @Override
        public void remove() {
            incrementMarker();
        }
    }

}
