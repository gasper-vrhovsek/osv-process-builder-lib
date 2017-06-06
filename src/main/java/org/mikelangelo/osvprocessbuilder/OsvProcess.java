package org.mikelangelo.osvprocessbuilder;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Justin Cinkelj, Gasper Vrhovsek
 */
public class OsvProcess extends Process {
    long m_thread_id;

    public OsvProcess(long thread_id) {
        m_thread_id = thread_id;
    }

    public void destroy() {
    }

    public int exitValue() {
        return 0;
    }

    public int waitFor()
            throws InterruptedException {
        System.out.println("OsvProcess.waitFor tid=" + m_thread_id);
        int[] status = new int[]{-123};
        OsvProcessBuilder.waittid(m_thread_id, status, 0);
        System.out.println("OsvProcess.waitFor tid=" + m_thread_id + " status=" + status[0]);
        return 0;
    }

    public InputStream getInputStream() {
        return System.in;
        //return null;
    }

    public OutputStream getOutputStream() {
        return System.out;
        //return null;
    }

    public InputStream getErrorStream() {
        return System.in; // ???
        //return null;
    }
}
