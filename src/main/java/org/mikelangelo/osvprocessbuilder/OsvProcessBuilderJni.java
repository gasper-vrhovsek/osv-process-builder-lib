package org.mikelangelo.osvprocessbuilder;

/**
 * @author Gasper Vrhovsek
 */
public class OsvProcessBuilderJni {

    static {
        System.loadLibrary("OsvProcessBuilder");
    }

    // JNI
    public static native int execve(String path, String[] argv, String[] envp, long[] thread_id, int notification_fd);

    public static native long waittid(long thread_id, int[] status, int options);

}
