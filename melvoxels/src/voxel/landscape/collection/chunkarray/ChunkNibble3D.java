package voxel.landscape.collection.chunkarray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;
import voxel.landscape.util.Asserter;

/**
 * Created by didyouloseyourdog on 7/28/14.
 */
public class ChunkNibble3D extends ChunkPrimitiveType3D {

    /*
    Look up:
    for 16x16x16: -->16x16x8 in a nibble (since one dim is compressed)
    say y is most significant dimension, x least (and its the compressed one)
    look up means: x + (z * 8) + (y * 8 * 16)
     */

    private byte[] chunk = new byte[(Chunk.XLENGTH * Chunk.YLENGTH * Chunk.ZLENGTH) / 2];
	private AtomicBoolean writeDirty = new AtomicBoolean(true);
    // TODO: read/write methods

    @Override
    public void Set(int val, Coord3 pos) {
        Set(val, pos.x, pos.y, pos.z);
    }
    @Override
    public void Set(int val, int x, int y, int z) {
        int lookup = y << 7 | z << 3 | ((x >> 1) & 7 );
        if ((x & 1) == 0) {
            chunk[lookup] = (byte)(chunk[lookup] & 0xf0 | val & 0xf);
        } else {
            chunk[lookup] = (byte)((val & 0xf) << 4 | chunk[lookup] & 0xf);
        }
    }
    @Override
    public int Get(Coord3 pos) {
        return Get(pos.x, pos.y, pos.z);
    }
    @Override
    public int Get(int x, int y, int z) {
        int lookup = y << 7 | z << 3 | ((x >> 1) & 7 );
        if ((x & 1) == 0) {
            return chunk[lookup] & 0xf;
        } else {
            return chunk[lookup] >> 4 & 0xf;
        }
    }
    
    /*
     * Read/Write
     */
    public void write(Path path) {
        if (!writeDirty.get()) return;
        try {
            Files.write(path, chunk);
            writeDirty.set(false);
        } catch (IOException e) {
            Asserter.assertFalseAndDie("failed to write ChunkNibble3D: " + path.toString());
        }
    }

    public void read(Path path) {
        if (!writeDirty.get()) return;
        if (!Files.exists(path)) return;
        try {
            chunk = Files.readAllBytes(path);
            writeDirty.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (chunk.length < (Chunk.XLENGTH * Chunk.YLENGTH * Chunk.ZLENGTH) / 2){
        	chunk =  new byte [(Chunk.XLENGTH * Chunk.YLENGTH * Chunk.ZLENGTH) / 2];
        }
    }

}
