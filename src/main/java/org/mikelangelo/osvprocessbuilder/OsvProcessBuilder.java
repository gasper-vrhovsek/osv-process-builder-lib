package org.mikelangelo.osvprocessbuilder;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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

    // main
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

        System.out.println("Entering start() method of OsvProcessBuilder.");

        HttpClient httpClient = HttpClientBuilder.create().build();

//        String executorIp = "http://172.16.122.14";

        URI executorEnvUri = null;
        try {
            executorEnvUri = new URI("http", null, "172.16.122.14", 8000, "/env", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String path = m_command.get(0);

        String[] argv = new String[m_command.size()];
        for (int ii = 0; ii < m_command.size(); ii++)
            argv[ii] = m_command.get(ii);

        String[] envp = new String[m_environment.size()];
        int ii = 0;
        for (String key : m_environment.keySet()) {
            //Worker worker = entry.getValue();
            envp[ii] = key + "=" + m_environment.get(key);
            // setup env on executor node
            setEnvironmentVariable(httpClient, executorEnvUri, key, m_environment.get(key));

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

        // Sending a request to executor node
        URI executorAppUri = null;
        try {
            executorAppUri = new URI("http", null, "172.16.122.14", 8000, "/app", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpPut put = new HttpPut(executorAppUri);
        List<NameValuePair> urlParams = new ArrayList<>();
        urlParams.add(new BasicNameValuePair("command", String.join(" ", argNew)));

        put.setEntity(new UrlEncodedFormEntity(urlParams));

        HttpResponse httpResponse = httpClient.execute(put);

        System.out.println(httpResponse.getStatusLine() + httpResponse.toString());
        // end of test


//        this.execve("/java.so", argNew, envp, thread_id, -1);

//        return new OsvProcess(thread_id[0]);
        return new OsvProcess(Integer.MAX_VALUE);
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

    private void setEnvironmentVariable(HttpClient httpClient, URI executorUri, String var, String val) throws IOException {
        HttpPost envPost = new HttpPost(executorUri);
        List<NameValuePair> envUrlParams = new ArrayList<>();
        envUrlParams.add(new BasicNameValuePair("var", var));
        envUrlParams.add(new BasicNameValuePair("val", val));

        envPost.setEntity(new UrlEncodedFormEntity(envUrlParams));
        HttpResponse response = httpClient.execute(envPost);

        System.out.println(response.toString());
    }

    private int execve(String path, String[] argv, String[] envp, long[] thread_id, int notification_fd) {
        return OsvProcessBuilderJni.execve(path, argv, envp, thread_id, notification_fd);
    }

    public static long waittid(long thread_id, int[] status, int options) {
        return OsvProcessBuilderJni.waittid(thread_id, status, options);
    }
}
