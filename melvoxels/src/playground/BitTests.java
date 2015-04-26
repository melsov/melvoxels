package playground;

import junit.framework.TestCase;
import voxel.landscape.player.B;

/**
 * Created by didyouloseyourdog on 8/3/14.
 */
public class BitTests extends TestCase {
    
    public void testBits()
    {
        int x = Integer.MIN_VALUE + 2;
        String bits = Integer.toBinaryString(x);
        B.bugln(bits);
        B.bugln("x pre shift: " + x);
        x = x << 1;
        B.bugln("x post shift: " + x);

        int maxdim = Integer.MAX_VALUE & 1023;
        String mxstr = Integer.toBinaryString(maxdim);
        B.bugln(mxstr);

    }
}
