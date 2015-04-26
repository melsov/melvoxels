package voxel.landscape.collection.chunkarray;

import voxel.landscape.Chunk;
import voxel.landscape.util.Asserter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkUByte2D extends ChunkPrimitiveType2D
{
    private byte[] chunk = new byte [Chunk.CHUNKDIMS.x*Chunk.CHUNKDIMS.z];
    public final AtomicBoolean writeDirty = new AtomicBoolean(true);

    @Override
    public void Set(int val, int x, int z) {
        chunk[z * Chunk.CHUNKDIMS.x + x] = (byte)val;
        writeDirty.set(true);
    }
    @Override
    public int Get(int x, int z) {
        return chunk[z * Chunk.CHUNKDIMS.x + x] & 255; //bitwise conversion to positive
    }

    public void write(Path path) {
        if (!writeDirty.get()) return;
        try {
            Files.write(path, chunk);
            writeDirty.set(false);
        } catch (IOException e) {
            e.printStackTrace();
            Asserter.assertFalseAndDie("failed to write ChunkUByte: " + path.toString());
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
    }

	
}
