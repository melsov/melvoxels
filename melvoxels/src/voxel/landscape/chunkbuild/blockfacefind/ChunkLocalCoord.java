package voxel.landscape.chunkbuild.blockfacefind;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.player.B;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by didyouloseyourdog on 10/2/14.
 * Stands in for chunk local coord3s in HashMaps
 * to save memory.
 */

public class ChunkLocalCoord implements Serializable
{
    private static final long serialVersionUID = 555; // ObjectStreamClass.lookup(ChunkLocalCoord.class).getSerialVersionUID();

    private int storage;

    private static final int localCoordBitMask = Chunk.XLENGTH - 1;

    public ChunkLocalCoord(Coord3 localCoord) {
        storage = ((localCoord.z & localCoordBitMask ) << (Chunk.SIZE_X_BITS * 2)) |
                ((localCoord.y & localCoordBitMask) << Chunk.SIZE_X_BITS) |
                (localCoord.x & localCoordBitMask);
    }
    public Coord3 toCoord3() {
        return new Coord3(
                storage & localCoordBitMask,
                (storage >>> Chunk.SIZE_X_BITS) & localCoordBitMask,
                (storage >>> Chunk.SIZE_X_BITS * 2) & localCoordBitMask
        );
    }
    @Override
    public boolean equals(Object other) {
        return this.storage == other.hashCode();
    }
    @Override
    public int hashCode() { return storage; }

    public static Coord3 IntToLocal(int i) {
        return new Coord3(
                i & localCoordBitMask,
                (i >>> Chunk.SIZE_X_BITS) & localCoordBitMask,
                (i >>> Chunk.SIZE_X_BITS * 2) & localCoordBitMask
        );
    }

    private void readObject(ObjectInputStream inStr) throws IOException, ClassNotFoundException {
        try {
            inStr.defaultReadObject();
        } catch (IOException e) {
            B.bugln("ioE");
        }
//        password = decode((String) inStr.readObject());
    }

    private void writeObject(ObjectOutputStream outStr) throws IOException {
        outStr.defaultWriteObject();
//        outStr.writeObject(encode(password));
    }

    // TODO: fix InvalidClassExcpetion try: implementing serializable methods
    // http://www.cs.uic.edu/~troy/fall04/cs441/drake/serialization.html
}

