/*
 * This file is released as a part of Quelea, free projection software for churches.
 *
 * Quelea Copyright (C) 2012 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//
//  AVPlayerJava.h
//  AVPlayerJava
//
//  Created by Greg Arno on 1/23/15.
//  Code Copyright (c) 2015 Greg Arno.
//

#import <Foundation/Foundation.h>
#import "AVFoundation/AVFoundation.h"
#import <Cocoa/Cocoa.h>
#include <JavaVM/jni.h>

@interface AVPlayerJava : NSObject






@property (strong, nonatomic) NSString * lastPlayedFile;
@property  (nonatomic) BOOL done;
@property (nonatomic)BOOL isDone;
@property  (nonatomic)double saveHue;
@property  (nonatomic)BOOL shouldRepeat;

@property  (nonatomic)double currentTime;
@property  (nonatomic)double duration;
@property  (nonatomic)float playerVolume;
@property  (nonatomic)int locX;
@property (nonatomic)int locY;
@property (strong, nonatomic) AVPlayer *player;

@property (strong, nonatomic) AVPlayerLayer *playerLayer;

@property (strong, nonatomic) CALayer *mainLayer;
@property (strong, nonatomic) NSWindow *mainWindow;
@property (strong, nonatomic) NSView *playerView;
@property (strong, nonatomic) CIFilter *hueAdjust;

@property  (nonatomic)BOOL firstLoad;
@property (strong, nonatomic) NSString *objOptions;

@property (nonatomic)BOOL stretchVideo;
@property (nonatomic)BOOL didStop;

/**
 Determines whether the av player has been sucessfully initilizes
 
 @return True if initialized, false otherwise.
 */
-(BOOL) isInit;

/**
 Fade up the current video window. (Does not affect playback
 */
-(void)fadeUp;

/**
 Fade down and stop the current video window.
 */
-(void)fadeDown;


/** Sets whether the video should loop
 
 @param repeat True if the video should repeat, false otherwise
 */
-(void)setRepeat:(BOOL) repeat;



/**
 Load a video into the video player
 
 @param path The path to the video to be played.
 @param options Options to set when loading the video player. Currently no options are implemented
 @param stretch Whether the video should be played stretched to the frame (true) or whether the video should maintain its aspect ratio (fale.
 */
-(void)     loadVid:(NSString *)path
        withOptions:(NSString *)options
     stretchToFrame:(BOOL)stretch;



/**
 Play the current loaded video
 */
-(void) play;



/**
 Play a video that is passed with a url. This will first call load, then will call play.
 
 @param path The path to the video to be played.
 @param options Options to set when loading the video player. Currently no options are implemented
 @param stretch Whether the video should be played stretched to the frame (true) or whether the video should maintain its aspect ratio (fale.
 */
-(void) play:(NSString *)path withOptions:(NSString *)options stretchToFrame:(BOOL)stretch;



/**
 Get the last location of a played video
 
 @return The string as a path of the last played video.
 */
-(NSString *) getLastLocation;



/**
 Pause the currently playing video.
 */
-(void) pauseVideo;



/**
 Stop the currently playing video.
 */
-(void) stop;



/**
 Gets whether the current player is muted.
 
 @return True if the player is muted, false otherwise
 */
-(BOOL) isMute;



/**
 Sets the mute for the currently playing video.
 
 @param mute True if the player should be muted, false otherwise.
 */
-(void) setMute: (BOOL)mute;



/**
 Gets the progress percent of the currently playing video.
 
 @return a double representing the percent completed of the video of the currently playing video.
 */
-(double) getProgressPercent;



/**
 Set the progress percent of the current video.
 
 @param percent a double between 0 and 1 representing the where the video should be set for playback.
 */
-(void) setProgressPercent: (double)percent;



/**
 Gets whether the current player is playing
 
 @return True if the player is playing, false otherwise
 */
-(BOOL) isPlaying;



/**
 Gets whether the current player is paused
 
 @return True if the player is paused, false otherwise.
 */
-(BOOL) isPaused;



/**
 Gets whether the current player is finished playing the video
 
 @return True if the video is finished, false otherwise.
 */
-(BOOL) isFinished;



/**
 Set the visiblity of the player window.
 
 @param visibilty True if the window should be shown, false otherwise.
 */
-(void) setVisible: (BOOL)visibility;



/**
 Set the volume of the currently playing video.
 
 @param volume The volume of the video, between 0 and 1.
 */
-(void) setVolume: (double)volume;



/**
 Get the volume of currently playing video.
 
 @return The volume of the video as a percentage between 0 and 1.
 */
-(double) getVolume;



/**
 Set location of the video window. This is expecting the coordinate system that is present in java (origin is top-left of main screen)
 
 @param x The x coordinate
 @param y The y coordinate
 */
-(void) setLocation: (int)x forY: (int)y;



/**
 Set the size of the video window.
 
 @param width The width of the video
 @param height The height of the video
 */
-(void) setSize: (int)width forHeight: (int)height;



/**
 Set the Hue of the currently playing video
 
 @param hue The hue of the video
 */
-(void) setHue: (double)hue;



/**
 Gets the hue of the currently playing video
 
 @return The hue of the video
 */
-(double) getHue;

@end
