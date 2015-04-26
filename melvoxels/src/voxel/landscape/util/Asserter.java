package voxel.landscape.util;

import voxel.landscape.Chunk;
import voxel.landscape.coord.Coord3;

public class Asserter {
	public static void assertTrue(boolean condition) {
		assertTrue(condition, "");
	}
	public static void assertFalseAndDie(String s) {
		assertTrue(false, s);
	}
	public static void assertTrue(boolean condition, String s) {
		if (!condition) {
			System.out.println(s);
			try {
				throw new Exception(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
            System.exit(1);
		}
	}
    public static void assertChunkNotNull(Chunk chunk, Coord3 coord3) {
        if (chunk == null) {
            System.out.println("null chunk at: " + coord3.toString());
            assertFalseAndDie("exiting");
        }

    }
}
