package voxel.landscape.debug.debugutil;

import com.jme3.util.BufferUtils;

import java.text.NumberFormat;

/**
 * Created by didyouloseyourdog on 7/27/14.
 */
public class MemoryStats
{


    public static long GetAllocatedMemory() {
        return Runtime.getRuntime().totalMemory();
    }
    public static long GetMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }
    public static long GetFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    private static final long ONE_MB = 1048576L;

    public static String MemoryInfo() {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("MEMORY(MB) free: " + format.format(freeMemory / ONE_MB) + " ");
        BufferUtils.printCurrentDirectMemory(sb);
//        sb.append("alloc'd: " + format.format(allocatedMemory / ONE_MB) + " ");
//        sb.append("max: " + format.format(maxMemory / ONE_MB) + " ");
//        sb.append("total free: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / ONE_MB) + " ");

        return sb.toString();
    }

}
