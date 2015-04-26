package playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by didyouloseyourdog on 8/9/14.
 */
public class PrintItAsBinary {
    public static void main(String[] args) {
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter Integer and I'll print it as a binary string:");
            try {
                String s = null;
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s.equals("quit")) return;
                int i = Integer.parseInt(s);
                System.out.println(bitString(i));
            } catch (NumberFormatException nfe) {
                System.err.println("Invalid Format!");
            }
        }

    }
    private static String bitString(int x) {
        char[] chars = new char[32];
        for(int i=31; i >= 0; --i) {
            chars[i] = (x & 1) == 1 ? '1' : '0';
            x = x >> 1;
        }
        return new String(chars);
    }
}
