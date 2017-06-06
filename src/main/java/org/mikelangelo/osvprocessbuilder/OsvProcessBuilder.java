package org.mikelangelo.osvprocessbuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Cinkelj, Gasper Vrhovsek
 */

public class OsvProcessBuilder  /* ProcessBuilder */ {
    private File m_directory;
    private Map<String, String> m_environment;
    private List<String> m_command;

    public OsvProcessBuilder(List<String> command)
            throws NullPointerException {
        m_command = command;
        m_environment = new HashMap<String, String>();
    }

    public OsvProcessBuilder(String... command) {
        this(Arrays.asList(command));
    }

    // JNI
    private native int execve(String path, String[] argv, String[] envp, long[] thread_id, int notification_fd);

    public static native long waittid(long thread_id, int[] status, int options);

    static {
        System.loadLibrary("OsvProcessBuilder");
    }

    //    // main
    public static void main(String[] args) {
        OsvProcessBuilder pb = new OsvProcessBuilder(Arrays.asList(new String[]{"aa", "bb"}));
        long[] thread_id = new long[]{0};
        pb.execve(
                "/java.so",
                //new String[] {"/java.so", "-Djava.library.path=/stormy-java", "-cp", "/stormy-java", "-Dsupervisor=osv", "OsvProcessBuilder", "5"},
                new String[]{"/java.so", "-version"},
                new String[]{"TT=11", "RT=22"},
                thread_id,
                -1);
    }

    public OsvProcess start()
            throws IOException {
        //
        String path = m_command.get(0);

        String[] argv = new String[m_command.size()];
        for (int ii = 0; ii < m_command.size(); ii++)
            argv[ii] = m_command.get(ii);

        String[] envp = new String[m_environment.size()];
        int ii = 0;
        for (String key : m_environment.keySet()) {
            //Worker worker = entry.getValue();
            envp[ii] = key + "=" + m_environment.get(key);
            ii++;
        }

        long[] thread_id = new long[]{0};
//        this.execve(path, argv, envp, thread_id, -1);

        // Remove element on 3, because java.so does not seem to understand it
        String[] argvCopy = new String[argv.length];

        int k = 0, j = 0;
        for (String arg : argv) {
            System.out.println("Original argv[" + k + "] = " + arg);
            k++;
            if (arg.equals("-Xmx1024M")) {
                continue;
            }
            argvCopy[j++] = arg;
        }

        // TODO temporary fix, this should be solved differently
        if (argvCopy[0].contains("/bin/java")) {
            argvCopy[0] = "/java.so"; // replace standard java path
        }

        // Trim new array
        String[] argNew = new String[j];
        int i = 0;
        for (String arg : argvCopy) {
            if (arg != null) {
                System.out.println("argv[" + i + "] = " + arg);
                argNew[i++] = arg;
            }
        }

        this.execve("/java.so", argNew, envp, thread_id, -1);

        return new OsvProcess(thread_id[0]);
    }

    public File directory() {
        return m_directory;
    }

    public OsvProcessBuilder directory(File directory) {
        m_directory = directory;
        return this;
    }

    public Map<String, String> environment() {
        return m_environment;
    }

    public List<String> command() {
        return m_command;
    }

    public OsvProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        System.out.println("OsvProcessBuilder.redirectErrorStream = " + redirectErrorStream);
        return this;
    }

    public boolean redirectErrorStream() {
        return false;
    }
}
