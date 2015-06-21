package voxel.landscape.fileutil;

import voxel.landscape.VoxelLandscape;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.player.B;
import voxel.landscape.util.Asserter;

import java.io.*;
import java.util.Random;
import java.util.logging.Logger;
import static voxel.landscape.player.B.*;
/**
 * Created by didyouloseyourdog on 4/5/15.
 */
public class FileUtil {

    public static final String SaveDirectory = "save";
    public static final String ChunkExtension = ".chunk";
    public static final String RaysExtension = ".rays";
    public static final String SunLightExtension = ".sunlight";
    public static final String LightExtension = ".light";

    public static final String LocalBlockMapExtension = ".lbm";
    public static final String ConcurrentHashMapC3DExtension = ".ch3m";
    public static final String ChunkFloodFillSeedSetExtension = ".cfs";
    public static final String ChunkBlockFaceMapExtension = ".cbfm";
    public static final String HasAddedStructuresExtension = ".has";
    private static final String SerializationExtension = ".ser";

    public static String WorldDirectory() {
        return String.format("%s/%s", SaveDirectory, VoxelLandscape.WorldSettings.seed);
    }

    public static String ColumnDirectory(int x, int z) {
        return String.format("%s/x%d-z%d", WorldDirectory(), x, z);
    }

    public static String ChunkFile(Coord3 coord3, boolean serialized) {
        return ChunkFile(coord3, "", serialized);
    }
    public static String ChunkFile(Coord3 coord3, String objectExtension, boolean serialized) {
        return ChunkFileName(coord3, String.format("%s%s%s", ChunkExtension, objectExtension, (serialized ? SerializationExtension : "")));
    }

    public static String SunlightFile(Coord3 coord3) {
        return ChunkFileName(coord3, SunLightExtension);
    }

    public static String LightFile(Coord3 coord3) {
        return ChunkFileName(coord3, LightExtension);
    }

    public static String RaysFile(Coord2 coord2) {
        String columnDirectory = createDirectories(ColumnDirectory(coord2.getX(), coord2.getZ()));
        return String.format("%s/%d-%d%s", columnDirectory, coord2.getX(), coord2.getZ(), RaysExtension);
    }

    private static String ChunkFileName(Coord3 coord3, String extension) {
        String columnDirectory = createDirectories(ColumnDirectory(coord3.x, coord3.z));
        return String.format("%s/%d-%d-%d%s", columnDirectory, coord3.x, coord3.y, coord3.z, extension);
    }

    private static String createDirectories(String fileName) {
        new File(fileName).mkdirs();
        return fileName;
    }

    public static Object DeserializeChunkObject(Coord3 position, String objectExtension) {
        Object result = null;
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = serializedChunkFile(position, objectExtension);
            if (fileIn == null) {
                return null;
            }
            in = new ObjectInputStream(fileIn);
            result = in.readObject();

        } catch (FileNotFoundException e) {
            B.bugln(e.getMessage());
            B.bug("chunk pos: ");
            B.bug(position);
            B.bugln("extension: " + objectExtension);
            B.StackTrace();
        } catch (ClassNotFoundException e) {
            B.bugln(e.getMessage());
            B.bug("chunk pos: ");
            B.bug(position);
            B.bugln("extension: " + objectExtension);
            B.StackTrace();
        } catch (IOException e) {
            B.bugln(e.getMessage());
            B.bug("chunk pos: ");
            B.bug(position);
            if (result == null) B.bug("RESULT IS NULL ");
            B.bugln("extension: " + objectExtension);
            B.StackTrace();
        }
        try {
            if (in != null) in.close();
            if (fileIn != null) fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void SerializeChunkObject(Serializable serializable, Coord3 position, String objectExtension) throws IOException {
//        try {
        FileOutputStream fileOut;
        fileOut = serializeChunkFile(position, objectExtension);
        ObjectOutputStream out;
        out = new ObjectOutputStream(fileOut);
        Asserter.assertTrue(serializable != null,"hmmm.. serializable was null");
        out.writeObject(serializable);
        out.close();
        fileOut.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static int testWasteTime(long pause) {
        float result = 0;
        Random random = new Random();
        while(pause > (int) result) {
            result += .5f + random.nextFloat();
        }
        return (int) result;
    }

    public static Object DeserializeChunkObjectTestPause(Coord3 position, String objectExtension, long pause) {
        Object result = null;
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = serializedChunkFile(position, objectExtension);
            if (fileIn == null) {
                return null;
            }
            testWasteTime(pause);
            in = new ObjectInputStream(fileIn);
            result = in.readObject();

        } catch (FileNotFoundException e) {
            B.bug("exception pos: ");
            B.bug(position);
            B.bugln("extension: " + objectExtension);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            B.bug("exception pos: ");
            B.bug(position);
            B.bugln("extension: " + objectExtension);
            e.printStackTrace();
        } catch (IOException e) {
            B.bug("IO exception pos: ");
            B.bug(position);
            B.bugln("extension: " + objectExtension);
            e.printStackTrace();
        } catch (Exception e) {
            B.bug("general exception");
            e.printStackTrace();
        }
        try {
            if (in != null) in.close();
            if (fileIn != null) fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void SerializeChunkObjectTestPause(Serializable serializable, Coord3 position, String objectExtension, long testPause) throws IOException {
        FileOutputStream fileOut;
        fileOut = serializeChunkFile(position, objectExtension);
        ObjectOutputStream out;
        out = new ObjectOutputStream(fileOut);
        testWasteTime(testPause);
        out.writeObject(serializable);
        out.close();
        fileOut.close();
    }

    private static FileInputStream serializedChunkFile(Coord3 position, String objectExtension) throws FileNotFoundException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(ChunkFile(position, objectExtension, true));
        } catch (Exception e) {
        }
        return inputStream;
    }
    private static FileOutputStream serializeChunkFile(Coord3 position, String objectExtension) throws FileNotFoundException {
        return new FileOutputStream(ChunkFile(position, objectExtension, true));
    }
    
    public static boolean DeleteCurrentWorldDirectory() {
    	boolean result = deleteDirectory(new File(WorldDirectory()) );
    	bug("deleting directory: " + WorldDirectory() + (result? " SUCESS" : "FAILURE") + "\n");
    	return result;
    }
    
    private static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

}
