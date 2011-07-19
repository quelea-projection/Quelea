package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import org.pushingpixels.substance.internal.ui.SubstanceSliderUI;
import org.quelea.utils.Utils;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.linux.LinuxEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.mac.MacEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.WindowsEmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Michael
 */
public class VideoControlPanel extends JPanel {

    private JButton play;
    private JButton pause;
    private JButton stop;
    private JButton mute;
    private JSlider positionSlider;
    private Canvas videoArea;
    private List<EmbeddedMediaPlayer> mediaPlayers;
    private List<LyricCanvas> registeredCanvases;
    private ScheduledExecutorService executorService;
    private boolean pauseCheck;

    public VideoControlPanel() {
        play = new JButton(Utils.getImageIcon("icons/play.png"));
        play.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                playVideo();
            }
        });
        pause = new JButton(Utils.getImageIcon("icons/pause.png"));
        pause.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pauseVideo();
            }
        });
        stop = new JButton(Utils.getImageIcon("icons/stop.png"));
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopVideo();
                positionSlider.setValue(0);
            }
        });
        mute = new JButton(Utils.getImageIcon("icons/mute.png"));
        mute.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setMute(!getMute());
            }
        });
        positionSlider = new JSlider(0, 1000);
        positionSlider.setValue(0);
        positionSlider.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                for (EmbeddedMediaPlayer mediaPlayer : mediaPlayers) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        pauseCheck = false;
                    }
                    else {
                        pauseCheck = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (EmbeddedMediaPlayer mediaPlayer : mediaPlayers) {
                    mediaPlayer.setTime((long) ((positionSlider.getValue() / (double) 1000) * mediaPlayer.getLength()));
                    if (!pauseCheck) {
                        mediaPlayer.play();
                    }
                }
            }
        });
        try {
            positionSlider.setUI(new SubstanceSliderUI(positionSlider) {

                @Override
                protected void scrollDueToClickInTrack(int direction) {
                    // this is the default behaviour, let's comment that out
                    //scrollByBlock(direction);

                    int value = positionSlider.getValue();

                    if (positionSlider.getOrientation() == JSlider.HORIZONTAL) {
                        value = this.valueForXPosition(positionSlider.getMousePosition().x);
                    }
                    else if (positionSlider.getOrientation() == JSlider.VERTICAL) {
                        value = this.valueForYPosition(positionSlider.getMousePosition().y);
                    }
                    positionSlider.setValue(value);
                }
            });
        }
        catch (Exception ex) {
            //UI issue, cannot do a lot and don't want to break program...
        }
        videoArea = new Canvas();
        videoArea.setBackground(Color.BLACK);
        videoArea.setMinimumSize(new Dimension(20, 20));
        MediaPlayerFactory factory = new MediaPlayerFactory(new String[]{"--no-video-title"});
        EmbeddedMediaPlayer mediaPlayer = factory.newMediaPlayer(null);
        factory.release();
        mediaPlayer.setVideoSurface(videoArea);
        mediaPlayers = new ArrayList<EmbeddedMediaPlayer>();
        mediaPlayers.add(mediaPlayer);
        registeredCanvases = new ArrayList<LyricCanvas>();

        setLayout(new BorderLayout());
        add(videoArea, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
        sliderPanel.add(positionSlider);
        controlPanel.add(sliderPanel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(play);
        buttonPanel.add(pause);
        buttonPanel.add(stop);
        buttonPanel.add(mute);
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.NORTH);
    }

    public void registerCanvas(LyricCanvas canvas) {
        registeredCanvases.add(canvas);
        mediaPlayers.get(0).setVideoSurface(canvas);
//        MediaPlayerFactory factory = new MediaPlayerFactory(new String[] {"--no-video-title"});
//        EmbeddedMediaPlayer mediaPlayer = factory.newMediaPlayer(null);
//        mediaPlayer.setVideoSurface(videoArea);
//        mediaPlayers.add(mediaPlayer);
    }

    public List<LyricCanvas> getRegisteredCanvases() {
        return registeredCanvases;
    }

    public void loadVideo(String videoPath) {
        for (EmbeddedMediaPlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.prepareMedia(videoPath);
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void playVideo() {
        for(int i=0 ; i<mediaPlayers.size() ; i++) {
            final EmbeddedMediaPlayer mediaPlayer = mediaPlayers.get(i);
            mediaPlayer.play();
            executorService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                int timeVal = (int) ((mediaPlayer.getTime() / (double) mediaPlayer.getLength()) * 1000);
                                positionSlider.setValue(timeVal);
                            }
                        });
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    public void pauseVideo() {
        for (EmbeddedMediaPlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.pause();
        }
    }

    public void stopVideo() {
        for (EmbeddedMediaPlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.stop();
        }
    }

    public void setMute(boolean muteState) {
        mediaPlayers.get(0).mute(muteState);
        if (getMute()) {
            mute.setIcon(Utils.getImageIcon("icons/unmute.png"));
        }
        else {
            mute.setIcon(Utils.getImageIcon("icons/mute.png"));
        }
    }

    public boolean getMute() {
        return mediaPlayers.get(0).isMute();
    }

    public void close() {
        for (EmbeddedMediaPlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.release();
        }
        executorService.shutdownNow();
    }

    @Override
    protected void finalize() throws Throwable {
        stopVideo();
        super.finalize();
        close();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        VideoControlPanel panel = new VideoControlPanel();
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        panel.loadVideo("F:\\Videos\\Inception\\Inception.mkv");
        panel.playVideo();
    }
}
