package voxel.landscape.player;



import com.jme3.math.Vector3f;

import voxel.landscape.coord.Coord3;

public class B {

	public static void bugln(String s) {
		System.out.println(s);
	}
	public static void bug(String s) {
		System.out.print(s);
	}
	public static void bug(int i) {
		System.out.println(i);
	}
    public static void bug(long l) {
        System.out.println(l);
    }
    public static void bug(long...is) {
        for (long i : is) {
            bug(i);
        }
    }
    public static void bug(int...is) {
        for (int i : is) {
            bug(i);
        }
    }
	public static void bug(double d) {
		System.out.println(d);
	}
	public static void bug(Vector3f v) {
		System.out.println(v.toString());
	}
	public static void bug(Coord3 c) {
		System.out.println(c.toString());
	}
    public static void bug(StringBuilder sb) {
        System.out.println(sb.toString());
    }
    public static void stackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder(stackTraceElements.length * 20);
        for(StackTraceElement stackTraceElement : stackTraceElements) {
            sb.append(stackTraceElement.getClassName());
            sb.append(" : ");
            sb.append(stackTraceElement.getMethodName());
            sb.append(" : ");
            sb.append(stackTraceElement.getLineNumber());
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
    public static void ThisMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder(stackTraceElements.length * 20);
        StackTraceElement stackTraceElement = stackTraceElements[2];

        sb.append(stackTraceElement.getClassName());
        sb.append(" : ");
        sb.append(stackTraceElement.getMethodName());
        sb.append(" : ");
        sb.append(stackTraceElement.getLineNumber());
        sb.append("\n");

        System.out.println(sb.toString());
    }
    public static void StackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder(stackTraceElements.length * 20);
        for(int i=2; i < stackTraceElements.length; ++i) {
            StackTraceElement stackTraceElement = stackTraceElements[i];
            sb.append("(");
            sb.append(stackTraceElement.getClassName());
            sb.append(".java:");
            sb.append(stackTraceElement.getLineNumber());
            sb.append(")");
            sb.append(" : ");
            sb.append(stackTraceElement.getMethodName());
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
	
}
