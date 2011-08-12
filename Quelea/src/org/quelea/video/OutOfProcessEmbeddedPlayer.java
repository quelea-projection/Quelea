package org.quelea.video;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import java.awt.Canvas;
import java.io.File;
import java.io.PrintStream;
import org.quelea.utils.QueleaProperties;
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
public class OutOfProcessEmbeddedPlayer extends OutOfProcessPlayer {

    private EmbeddedMediaPlayer mediaPlayer;

    public OutOfProcessEmbeddedPlayer(final long canvasId) throws Exception {

        //Lifted pretty much out of the VLCJ code
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
            System.exit(1);
        }
        mediaPlayer.setVideoSurface(new Canvas()); //Required with a dummy canvas to active the above nativeSetVideoSurface method

        if (!TEST_MODE) {
            read(mediaPlayer);
        }
    }
    private static final boolean TEST_MODE = false;

    public static void main(String[] args) {
        if (TEST_MODE) {
            args = new String[]{"0"};
        }
        File nativeDir = new File("lib/native");
        NativeLibrary.addSearchPath("libvlc", nativeDir.getAbsolutePath());
        NativeLibrary.addSearchPath("vlc", nativeDir.getAbsolutePath());
        try (PrintStream stream = new PrintStream(new File(QueleaProperties.getQueleaUserHome(), "ooplog.txt"))) {
            System.setErr(stream); //Important, MUST redirect err stream
            OutOfProcessEmbeddedPlayer player = new OutOfProcessEmbeddedPlayer(Integer.parseInt(args[0]));
//            player.mediaPlayer.prepareMedia("dvdsimple://E:");
//            player.mediaPlayer.play();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
