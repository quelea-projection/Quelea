package org.quelea.video;

import com.sun.jna.Native;
import java.awt.Canvas;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        Canvas canvas = new Canvas();
        canvas.setSize(800, 600);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
        long drawable = Native.getComponentID(canvas);
        startSecondJVM(4444, drawable);
    }

    public static void startSecondJVM(int port, long memory) throws Exception {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, OutOfProcessPlayer.class.getName(), Integer.toString(port), Long.toString(memory));
        processBuilder.start();
        write();
    }

    public static void write() throws IOException {
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            echoSocket = new Socket("localhost", 4444);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    echoSocket.getInputStream()));
        }
        catch (UnknownHostException e) {
            System.err.println("Don't know about host: taranis.");
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to: taranis.");
            System.exit(1);
        }

        out.println("open F:\\Videos\\Inception\\Inception.mkv");
        System.out.println("echo: " + in.readLine());
        out.println("play");
        try {
            Thread.sleep(5000);
        }
        catch (Exception ex) {
        }
        out.println("close");

        out.close();
        in.close();
        echoSocket.close();
    }
}
