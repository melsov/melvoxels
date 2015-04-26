package voxel.landscape.coord;

import java.util.Iterator;

/**
 * Created by didyouloseyourdog on 4/23/15.
 */
public class BoxBorder2 implements Iterable<Coord2> {

    public final Box2 box;
    public final int strokeWidth;
    private final boolean isOuterBorder;

    public BoxBorder2(Box2 box, int strokeWidth, boolean isOuterBorder) {
        this.strokeWidth = strokeWidth;
        this.isOuterBorder = isOuterBorder;
        if (isOuterBorder) {
            this.box = new Box2(box.start.minus(new Coord2(strokeWidth)), box.dimensions.add(new Coord2(strokeWidth * 2)));
        } else {
            this.box = box.clone();
        }
    }

    @Override
    public Iterator<Coord2> iterator() {
        return new BoxBorder2Iterator();
    }
    class BoxBorder2Iterator implements Iterator<Coord2> {
        private Coord2 index;
        public BoxBorder2Iterator() {
            index = new Coord2(0);
        }
        private void increment() {
            if (index.y < strokeWidth || index.y >= box.dimensions.y - strokeWidth ||
                    index.x < strokeWidth - 1 || index.x >= box.dimensions.x - strokeWidth) {
                index.x++;
            } else {
                index.x = box.dimensions.x - strokeWidth;
            }
            if (index.x == box.dimensions.x) {
                index.x = 0;
                index.y++;
            }
        }
        @Override
        public boolean hasNext() {
            return index.y < box.dimensions.y;
        }

        @Override
        public Coord2 next() {
            Coord2 result = index.clone();
            increment();
            return box.start.add(result);
        }

        @Override
        public void remove() {
            increment();
        }
    }
}
