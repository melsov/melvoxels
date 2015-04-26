package voxel.landscape.chunkbuild.blockfacefind;

import voxel.landscape.coord.Direction;

import java.io.Serializable;

/**
 * Created by didyouloseyourdog on 10/2/14.
 */
public class BlockFaceRecord implements Serializable {
    private int storage;
    public int hashCode() { return storage; }

    public void setFace(int direction, boolean exists) {
        if (exists) {
            storage |= Direction.BitMasks[direction];
        } else {
            storage &= Direction.NegativeBitMasks[direction];
        }
    }
    public boolean hasFaces() {
        return storage != 0;
    }
    public boolean getFace(int direction) {
        return 0 < (storage & Direction.BitMasks[direction]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BlockFaceRecord: \n");
        for (int dir : Direction.Directions) {
            sb.append(Direction.Names[dir]);
            sb.append(" : ");
            sb.append(getFace(dir));
            sb.append("\n");
        }
        return sb.toString();
    }
}
