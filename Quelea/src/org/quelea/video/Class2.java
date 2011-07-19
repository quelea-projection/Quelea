package org.quelea.video;

import com.sun.jna.Pointer;
import java.awt.Canvas;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import uk.co.caprica.vlcj.binding.LibVlcFactory;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_player_t;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.linux.LinuxEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.mac.MacEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.WindowsEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Michael
 */
public class Class2 {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    public Class2(final int port, final long canvasId) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {
            System.out.println("Could not listen on port: 4444");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            clientSocket = serverSocket.accept();
        }
        catch (IOException e) {
            System.out.println("Accept failed: 4444");
            System.exit(-1);
        }

        EmbeddedMediaPlayer mediaPlayer;

        if (RuntimeUtil.isNix()) {
            mediaPlayer = new LinuxEmbeddedMediaPlayer(LibVlcFactory.factory().synchronise().log().create().libvlc_new(1, new String[]{"--no-video-title"}), null) {

                @Override
                protected void nativeSetVideoSurface(libvlc_media_player_t mediaPlayerInstance, Canvas videoSurface) {
                    libvlc.libvlc_media_player_set_xwindow(mediaPlayerInstance, (int) canvasId);
                }
            };
        }
        else if (RuntimeUtil.isWindows()) {
            mediaPlayer = new WindowsEmbeddedMediaPlayer(LibVlcFactory.factory().synchronise().log().create().libvlc_new(1, new String[]{"--no-video-title"}), null) {

                @Override
                protected void nativeSetVideoSurface(libvlc_media_player_t mediaPlayerInstance, Canvas videoSurface) {
                    Pointer ptr = Pointer.createConstant(canvasId);
                    libvlc.libvlc_media_player_set_hwnd(mediaPlayerInstance, ptr);
                }
            };
        }
        else if (RuntimeUtil.isMac()) {
            mediaPlayer = new MacEmbeddedMediaPlayer(LibVlcFactory.factory().synchronise().log().create().libvlc_new(2, new String[]{"--no-video-title", "--vout=macosx"}), null) {

                @Override
                protected void nativeSetVideoSurface(libvlc_media_player_t mediaPlayerInstance, Canvas videoSurface) {
                    Pointer ptr = Pointer.createConstant(canvasId);
                    libvlc.libvlc_media_player_set_nsobject(mediaPlayerInstance, ptr);
                }
            };
        }
        else {
            mediaPlayer = null;
        }

        mediaPlayer.setVideoSurface(new Canvas());

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            if (inputLine.startsWith("open ")) {
                inputLine = inputLine.substring("open ".length());
                mediaPlayer.prepareMedia(inputLine);
                out.println("ok");
            }
            else if (inputLine.equalsIgnoreCase("play")) {
                mediaPlayer.play();
                out.println("ok");
            }
            else if (inputLine.equalsIgnoreCase("pause")) {
                mediaPlayer.pause();
                out.println("ok");
            }
            else if (inputLine.equalsIgnoreCase("close")) {
                System.exit(0);
            }
            else {
                out.println("unknown command");
            }
        }
    }

    public static void main(String[] args) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new File("C:\\Users\\Michael\\Desktop\\log.txt"));
            System.setErr(ps);
            System.err.println(args[0]);
            new Class2(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            ps.close();
        }
    }
}
