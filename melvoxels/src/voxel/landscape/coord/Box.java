package voxel.landscape.coord;

import voxel.landscape.Chunk;

import java.util.Iterator;

/**
 * Created by didyouloseyourdog on 10/5/14.
 */
public class Box implements Iterable<Coord3> {
    public Coord3 start;
    public Coord3 dimensions;
    public Coord3 extent() {
        return start.add(dimensions);
    }
    public Box(Coord3 start_, Coord3 dimensions_) {
        start = start_; dimensions = dimensions_;
    }
    public static Box BoxWithStartAndExtent(Coord3 _start, Coord3 extent) {
        return new Box (_start, extent.minus(_start));
    }
    public boolean contains(Coord3 co) {
        return co.greaterThanOrEqual(start) && dimensions.greaterThan(co.minus(start));
    }
    public boolean isOnBorder(Coord3 co) {
        return contains(co) && (start.x == co.x || start.y == co.y || start.z == co.z ||
                extent().x - 1 == co.x || extent().y - 1 == co.y || extent().z - 1  == co.z);
    }
    public int outsideOfInDirection(Coord3 co) {
        Coord3 dif = co.minus(start);
        if (dif.x < 0) {
            return Direction.XNEG;
        } else if (dif.y < 0) {
            return Direction.YNEG;
        } else if (dif.z < 0) {
            return Direction.ZNEG;
        }
        Coord3 outerDif = extent().minus(co);
        if (outerDif.x <= 0) {
            return Direction.XPOS;
        } else if (outerDif.y <= 0) {
            return Direction.YPOS;
        } else if (outerDif.z <= 0) {
            return Direction.ZPOS;
        }
        return -1;
    }
    public int borderLocation(int borderSide) {
        if (Direction.IsNegDir(borderSide)) {
            return start.componentForDirection(borderSide);
        }
        return extent().componentForDirection(borderSide) - 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Box ) return equals((Box) other);
        return false;
    }
    public boolean equals(Box other) {
        return start.equal(other.start) && dimensions.equal(other.dimensions);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Box: start: ");
        sb.append(start.toString());
        sb.append(" dimensions: ");
        sb.append(dimensions.toString());
        return sb.toString();
    }

    public static Box WorldCoordBoxForChunk(Chunk chunk) {
        return Box.WorldCoordBoxForChunkCoord(chunk.position.clone());
    }
    public static Box WorldCoordBoxForChunkCoord(Coord3 chunkStart) {
        return Box.WorldCoordBoxForChunkRegion(chunkStart, chunkStart.add(new Coord3(1)));
    }
    public static Box WorldCoordBoxForChunkRegion(Coord3 chunkStart, Coord3 chunkEnd) {
        Coord3 start = Chunk.ToWorldPosition(chunkStart);
        return new Box(start, Chunk.ToWorldPosition(chunkEnd).minus(start));
    }
    @Override
    public Box clone() {
        return new Box(start.clone(), dimensions.clone());
    }

    @Override
    public Iterator<Coord3> iterator() {
        return new BoxIterator(this);
    }
}
