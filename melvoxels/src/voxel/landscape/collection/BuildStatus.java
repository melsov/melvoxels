package voxel.landscape.collection;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum BuildStatus
{
    HAS_NOT_BEEN_TOUCHED (0),
    BUILDING_DATA (1),
    BUILT_SURFACE_DATA(2),
    BUILT_DATA(3);

    private int integer;

    BuildStatus(int i) { integer = i; }

    private static final Map lookup = new HashMap();
    // Populate the lookup table on loading time
    static {
        for (BuildStatus status : EnumSet.allOf(BuildStatus.class))
            lookup.put(status.integer, status);
    }

    public boolean equals(int i) {
        return this.ordinal() == i;
    }

}
