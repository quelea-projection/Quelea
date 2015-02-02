/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.multimedia;

import java.io.File;

/**
 *Class that loads native objective-c code in order to nicely play video on MacOS
 * @author grgarno
 */
public class AVPlayerJava {
    
    static {
        File library = new File("./lib/libAVPlayerJava.jnilib");
        System.load(library.getAbsolutePath());
        //boolean blub = isInit();
    }

  
    /**
     * Initializes native code, then determines whether the av player has been sucessfully initilized.
     *
     * @return True if initialized, false otherwise.
     */
    public static native boolean isInit();

    /**
     * Sets whether the video should loop
     *
     * @param repeat True if the video should repeat, false otherwise
     */
    public static native void setRepeat(boolean repeat);

    /**
     * Load a video into the video player
     *
     * @param path The path to the video to be played.
     */
    public static native void loadVid(String path);

    /**
     * Set options for loading a player
     * @param options The options// currently are ignored
     */
    public static native void setOptions(String options);
    
    /**
     * Set stretch for the video 
     * @param stretch true if the video should fill the frame, false to hold the aspect ratio.
     */
    public static native void setStretch(boolean stretch);
    
    /**
     * Set the fade speed of the player
     * @param FadeSpeed The fade speed in seconds.
     */
    public static native void setFadeSpeed(double FadeSpeed);
    /**
     * Play the current loaded video
     */
    public static native void play();


    /**
     * Get the last location of a played video
     *
     * @return The string as a path of the last played video.
     */
    public static native String getLastLocation();

    /**
     * Pause the currently playing video.
     */
    public static native void pauseVideo();

    /**
     * Stop the currently playing video.
     */
    public static native void stop();

    /**
     * Gets whether the current player is muted.
     *
     * @return True if the player is muted, false otherwise
     */
    public static native boolean isMute();

    /**
     * Sets the mute for the currently playing video.
     *
     * @param mute True if the player should be muted, false otherwise.
     */
    public static native void setMute(boolean mute);

    /**
     * Gets the progress percent of the currently playing video.
     *
     * @return a double representing the percent completed of the video of the
     * currently playing video.
     */
    public static native double getProgressPercent();

    /**
     * Set the progress percent of the current video.
     *
     * @param percent a double between 0 and 1 representing the where the video
     * should be set for playback.
     */
    public static native void setProgressPercent(double percent);

    /**
     * Gets whether the current player is playing
     *
     * @return True if the player is playing, false otherwise
     */
    public static native boolean isPlaying();

    /**
     * Gets whether the current player is paused
     *
     * @return True if the player is paused, false otherwise.
     */
    public static native boolean isPaused();

    /**
     * Gets whether the current player is finished playing the video
     *
     * @return True if the video is finished, false otherwise.
     */
    public static native boolean isFinished();

    /**
     * Set the visiblity of the player window.
     *
     * @param visible True if the player should show, false otherwise.
     */
    public static native void setVisible(boolean visible);

    /**
     * Set the volume of the currently playing video.
     *
     * @param volume The volume of the video, between 0 and 1.
     */
    public static native void setVolume(double volume);

    /**
     * Get the volume of currently playing video.
     *
     * @return The volume of the video as a percentage between 0 and 1.
     */
    public static native double getVolume();

    /**
     * Set location of the video window. This is expecting the coordinate system
     * that is present in java (origin is top-left of main screen)
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public static native void setLocation(int x, int y);

    /**
     * Set the size of the video window.      *
     * @param width The width of the video
     * @param height The height of the video
     */
    public static native void setSize(int width, int height);

    /**
     * Set the Hue of the currently playing video
     *
     * @param hue The hue of the video
     */
    public static native void setHue(double hue);

    /**
     * Gets the hue of the currently playing video
     *
     * @return The hue of the video
     */
    public static native double getHue();
}
