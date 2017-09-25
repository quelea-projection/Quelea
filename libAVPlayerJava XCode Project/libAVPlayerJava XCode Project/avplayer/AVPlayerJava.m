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

#import "AVPlayerJava.h"
#include <JavaVM/jni.h>




static AVPlayerJava *instance;
static AVPlayerJava *instance2;
static NSWindow *background;
static AVPlayerJava *currentInstance;
static double FADE_SPEED = 6.0;
static BOOL is1 = false;

@implementation AVPlayerJava

/**
 Called when the java VM is inited. This method is used to initialize all static elements of this library.
 */
+(void)start{
    NSRect frame = NSMakeRect(0, 0, 200, 200);
    background  = [[NSWindow alloc] initWithContentRect:frame
                                              styleMask:NSBorderlessWindowMask
                                                backing:NSBackingStoreBuffered
                                                  defer:NO] ;
    
    [background setBackgroundColor:[NSColor blackColor]];
    instance = [[AVPlayerJava alloc] init];
    instance2 = [[AVPlayerJava alloc] init];
    
    
    currentInstance = instance;
}
/**
 Initialize the player.
 @return Self after initialization.
 */
-(id)init{
    
    
    _done = false;
    _isDone = false;
    _saveHue = 0;
    _shouldRepeat = false;
    _didStop = false;
    
    _locX = 0;
    _locY =  0;
    
    
    _firstLoad = TRUE;
    _objOptions = @"";
    
    _stretchVideo = false;
    
    
    // Create the AVPlayer, add rate and status observers
    _player = [[AVPlayer alloc] init];
    
    
    NSRect frame = NSMakeRect(0, 0, 200, 200);
    _mainWindow  = [[NSWindow alloc] initWithContentRect:frame
                                               styleMask:NSBorderlessWindowMask
                                                 backing:NSBackingStoreBuffered
                                                   defer:NO] ;
    [_mainWindow setOpaque:NO];
    [_mainWindow setAlphaValue:0];
    [_mainWindow setBackgroundColor:[NSColor blackColor]];
    _playerView = [[NSView alloc] initWithFrame:frame];
    
    
    
    [_mainWindow.contentView addSubview:_playerView];
    
    [self addTightConstraintsWithItem:_playerView toParentView:_mainWindow.contentView];
    
    
    
    _isDone = true;
    
    
    
    
    return self;
}
/**
 Add constraints for item in view. Util method
 @param child The child view
 @param parent The Super view
 */
-(void)addTightConstraintsWithItem:(id)child toParentView:(id)parent{
    [NSLayoutConstraint constraintWithItem:child
                                 attribute:NSLayoutAttributeLeading
                                 relatedBy:NSLayoutRelationEqual
                                    toItem:parent
                                 attribute:NSLayoutAttributeLeading
                                multiplier:1.0f
                                  constant:0.0f];
    [NSLayoutConstraint constraintWithItem:child
                                 attribute:NSLayoutAttributeTop
                                 relatedBy:NSLayoutRelationEqual
                                    toItem:parent
                                 attribute:NSLayoutAttributeTop
                                multiplier:1.0f
                                  constant:0.0f];
    
    [NSLayoutConstraint constraintWithItem:child
                                 attribute:NSLayoutAttributeWidth
                                 relatedBy:NSLayoutRelationEqual
                                    toItem:parent
                                 attribute:NSLayoutAttributeWidth
                                multiplier:1.0f
                                  constant:0.0f];
    [NSLayoutConstraint constraintWithItem:child
                                 attribute:NSLayoutAttributeHeight
                                 relatedBy:NSLayoutRelationEqual
                                    toItem:parent
                                 attribute:NSLayoutAttributeHeight
                                multiplier:1.0f
                                  constant:0.0f];
}
/**
 Add constraints to layer. Util method
 @param child the layer which should have constraints added
 */
-(void)addLayerConstraints:(CALayer *)child{
    [child addConstraint:[CAConstraint constraintWithAttribute:kCAConstraintWidth relativeTo:@"superlayer" attribute:kCAConstraintWidth]];
    [child addConstraint:[CAConstraint constraintWithAttribute:kCAConstraintHeight relativeTo:@"superlayer" attribute:kCAConstraintHeight]];
    
}
/**
 Method that is called when the player reaches the end of the video
 @param notification the notification instance.
 */
- (void)playerItemDidReachEnd:(NSNotification *)notification
{
    if(_shouldRepeat){
        
        [_player seekToTime:kCMTimeZero];
        [_player play];
        
    }else{
        _done = true;
    }
}

/**
 Determines whether the av player has been sucessfully initilizes
 
 @return True if initialized, false otherwise.
 */
-(BOOL) isInit{
    if(!_isDone){
        [self init];
    }
    
    return _isDone;
}



/** Sets whether the video should loop
 
 @param repeat True if the video should repeat, false otherwise
 */
-(void)setRepeat:(BOOL) repeat{
    _shouldRepeat = repeat;
}



/**
 Load a video into the video player
 
 @param path The path to the video to be played.
 @param options Options to set when loading the video player. Currently no options are implemented
 @param stretch Whether the video should be played stretched to the frame (true) or whether the video should maintain its aspect ratio (fale.
 */
-(void)     loadVid:(NSString *)path
        withOptions:(NSString *)options
     stretchToFrame:(BOOL)stretch{
    _didStop = false;
    _lastPlayedFile = path;
    _objOptions = options;
    _stretchVideo = stretch;
    // Create an asset with our URL, asychronously load its tracks and whether it's playable or protected.
    // When that loading is complete, configure a player to play the asset.
    AVURLAsset *asset = [AVAsset assetWithURL:[NSURL fileURLWithPath:path]];
    NSArray *assetKeysToLoadAndTest = @[@"playable", @"hasProtectedContent", @"tracks"];
    
    [asset loadValuesAsynchronouslyForKeys:assetKeysToLoadAndTest completionHandler:^(void) {
        
        // The asset invokes its completion handler on an arbitrary queue when loading is complete.
        // Because we want to access our AVPlayer in our ensuing set-up, we must dispatch our handler to the main queue.
        dispatch_async(dispatch_get_main_queue(), ^(void) {
            
            [self setUpPlaybackOfAsset:asset withKeys:assetKeysToLoadAndTest];
            
        });
        
    }];
    
    
}
/**
 Set up the playback of an asset. Used in the load method
 */
- (void)setUpPlaybackOfAsset:(AVAsset *)asset withKeys:(NSArray *)keys
{
    _playerLayer = [AVPlayerLayer playerLayerWithPlayer:_player];
    _playerLayer.frame = _playerView.layer.bounds;
    _playerLayer.autoresizingMask = kCALayerWidthSizable | kCALayerHeightSizable;
    _playerLayer.hidden = NO;
    if(_stretchVideo){
        _playerLayer.videoGravity = AVLayerVideoGravityResize;
    }else{
        _playerLayer.videoGravity = AVLayerVideoGravityResizeAspect;
    }
    _playerView.layer.sublayers = nil;
    [_playerView.layer addSublayer:_playerLayer];
    AVPlayerItem *playerItem = [AVPlayerItem playerItemWithAsset:asset];
    [_player replaceCurrentItemWithPlayerItem:playerItem];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(playerItemDidReachEnd:)
                                                 name:AVPlayerItemDidPlayToEndTimeNotification
                                               object:playerItem ];
    
    _hueAdjust = [CIFilter filterWithName:@"CIHueAdjust"];
    [_hueAdjust setDefaults];
    
    [_hueAdjust setValue:[NSNumber numberWithDouble:_saveHue] forKey:@"inputAngle"];
    [_playerView setCompositingFilter:_hueAdjust];
    
    
    
}




/**
 Play the current loaded video
 */
-(void) play{
    if(_didStop){
        [self loadVid:_lastPlayedFile withOptions:_objOptions stretchToFrame:_stretchVideo];
    }
    [_player play];
    [self fadeUp];
    
}
/**
 Fade up the current video window. (Does not affect playback
 */
-(void) fadeUp{
    [_mainWindow setOpaque:NO];
    NSThread* evtThread = [ [NSThread alloc] initWithTarget:self
                                                   selector:@selector(fadeUpThread)
                                                     object:nil ];
    [evtThread setQualityOfService:NSQualityOfServiceUserInteractive];
    [ evtThread start ];
}
/**
 Util method used by fade up.
 */
-(void) fadeUpThread{
    dispatch_async(dispatch_get_main_queue(), ^(void) {
        _mainWindow.alphaValue = 0;
        
        
        
    });
    for (double i = (_mainWindow.alphaValue * 100); i <= 100; i++) {
        dispatch_async(dispatch_get_main_queue(), ^(void) {
            
            _mainWindow.alphaValue = ((float) (i / 100.0));
            [self setVolume:(float) (i / 100.0)];
            
            
        });
        
        
        [NSThread sleepForTimeInterval:(FADE_SPEED / 100)];
        
    }
    
}
/**
 Fade down and stop the current video window.
 */
-(void) fadeDown{
    [_mainWindow setOpaque:NO];
    NSThread* evtThread = [ [NSThread alloc] initWithTarget:self
                                                   selector:@selector(fadeDownThread)
                                                     object:nil ];
    [evtThread setQualityOfService:NSQualityOfServiceUserInteractive];
    [ evtThread start ];
}
/**
 Util thread used by the fade down method
 */
-(void) fadeDownThread{
    dispatch_async(dispatch_get_main_queue(), ^(void) {
        _mainWindow.alphaValue = 0;
        
        
        
    });
    for (double i =((1- _mainWindow.alphaValue) * 100); i <= 100; i++) {
        dispatch_async(dispatch_get_main_queue(), ^(void) {
            
            _mainWindow.alphaValue = 1 -((float) (i / 100.0));
            [self setVolume: 1 -(float) (i / 100.0)];
            
        });
        
        
        [NSThread sleepForTimeInterval:(FADE_SPEED / 100)];
        
    }
    dispatch_async(dispatch_get_main_queue(), ^(void) {
        [self stop];
        
        
    });
    
}




/**
 Play a video that is passed with a url. This will first call load, then will call play.
 
 @param path The path to the video to be played.
 @param options Options to set when loading the video player. Currently no options are implemented
 @param stretch Whether the video should be played stretched to the frame (true) or whether the video should maintain its aspect ratio (fale.
 */
-(void) play:(NSString *)path withOptions:(NSString *)options stretchToFrame:(BOOL)stretch{
    [self loadVid:path withOptions:options stretchToFrame:stretch];
    [self play];
}



/**
 Get the last location of a played video
 
 @return The string as a path of the last played video.
 */
-(NSString *) getLastLocation{
    return _lastPlayedFile;
}



/**
 Pause the currently playing video.
 */
-(void) pauseVideo{
    
    [_player pause];
    
}



/**
 Stop the currently playing video.
 */
-(void) stop{
    [_player pause];
    [ _player replaceCurrentItemWithPlayerItem:nil];
    _didStop = true;
    
}



/**
 Gets whether the current player is muted.
 
 @return True if the player is muted, false otherwise
 */
-(BOOL) isMute{
    return [_player isMuted];
}



/**
 Sets the mute for the currently playing video.
 
 @param mute True if the player should be muted, false otherwise.
 */
-(void) setMute: (BOOL)mute{
    
    [_player setMuted:mute];
    
}



/**
 Gets the progress percent of the currently playing video.
 
 @return a double representing the percent completed of the video of the currently playing video.
 */
-(double) getProgressPercent{
    double percent;
    
    percent= (double)CMTimeGetSeconds([_player.currentItem currentTime])
    /(double)CMTimeGetSeconds([_player.currentItem duration]);
    
    return percent;
    
}


/**
 Gets the current Time
 
 @return a long representing the time in ms
 */
-(long) getCurrentTime{
    
    
    return 1000 * (double)CMTimeGetSeconds([_player.currentItem currentTime]);
}

/**
 Gets the current duration
 
 @return a long representing the current duration in ms
 */
-(long) getCurrentDuration{
    
    
    return 1000 * (double)CMTimeGetSeconds([_player.currentItem duration]);
    
    
    
}


/**
 Set the progress percent of the current video.
 
 @param percent a double between 0 and 1 representing the where the video should be set for playback.
 */
-(void) setProgressPercent: (double)percent{
    
    [_player seekToTime: CMTimeMultiplyByFloat64([_player.currentItem duration], percent)];
    
}



/**
 Gets whether the current player is playing
 
 @return True if the player is playing, false otherwise
 */
-(BOOL) isPlaying{
    return ([_player rate] >= 1.0);
    
}



/**
 Gets whether the current player is paused
 
 @return True if the player is paused, false otherwise.
 */
-(BOOL) isPaused{
    return ([_player rate] <= 0.0);
}



/**
 Gets whether the current player is finished playing the video
 
 @return True if the video is finished, false otherwise.
 */
-(BOOL) isFinished{
    return _done;
}



/**
 Set the visiblity of the player window.
 
 @param visibilty True if the window should be shown, false otherwise.
 */
-(void) setVisible: (BOOL)visibility{
    if(visibility){
        [_mainWindow orderFront:NSApp];
        [_mainWindow orderBack:NSApp];
        if(_mainLayer == NULL){//only the first time
            _mainLayer = [CALayer layer];
            [_mainLayer setFrame:CGRectMake(0, 0, 100, 100)];
            
            [_playerView setLayer:_mainLayer];
            [_playerView setWantsLayer:YES];
            [_mainLayer display];
        };
    }else{
        [_mainWindow orderOut:NSApp];
        
    }
}



/**
 Set the volume of the currently playing video.
 
 @param volume The volume of the video, between 0 and 1.
 */
-(void) setVolume: (double)volume{
    _player.volume = volume;
    
    [_player setVolume:volume];
    
}



/**
 Get the volume of currently playing video.
 
 @return The volume of the video as a percentage between 0 and 1.
 */
-(double) getVolume{
    return [_player volume];
}



/**
 Set location of the video window. This is expecting the coordinate system that is present in java (origin is top-left of main screen)
 
 @param x The x coordinate
 @param y The y coordinate
 */
-(void) setLocation: (int)x forY: (int)y{
    _locX = x;
    _locY = y;
    
    NSPoint pos;
    pos.x = x;
    pos.y = [self getMacYCoord:y xCoordinate:x heightOfWindow:_mainWindow.frame.size.height];
    if(!(_mainWindow.frame.origin.x == x && _mainWindow.frame.origin.y == y)){
        [_mainWindow setFrameOrigin:pos];
        //NSLog(@"setlocation for window");
        
    }
    if(!(background.frame.origin.x == x && background.frame.origin.y == y)){
        [background setFrameOrigin:pos];
        
        
    }
    
    
}

-(int)getMacYCoord:(int) properYCoord xCoordinate:(int) coorspondingXCoord heightOfWindow:(int)windowHeight{
    int runningTotal = 0;
    for(NSScreen *s in NSScreen.screens){
        runningTotal = runningTotal + s.frame.size.width;
        
        if(coorspondingXCoord < runningTotal){
            
            
            return (s.frame.origin.y + s.frame.size.height) - properYCoord - windowHeight;
            
            
            
        }
    }
    return properYCoord;
}


/**
 Set the size of the video window.
 
 @param width The width of the video
 @param height The height of the video
 */
-(void) setSize: (int)width forHeight: (int)height{
    CGSize size;
    size.height = height;
    size.width = width;
    NSRect rect = [_mainWindow frame];
    rect.size = size;
    if(!(_playerView.frame.size.height == height && _playerView.frame.size.width == width)){
        NSRect viewRect = [_playerView frame];
        viewRect.size = size;
        [_playerView setFrame:viewRect];
        [_mainLayer setFrame:CGRectMake(0, 0, viewRect.size.width, viewRect.size.height)];
        //NSLog(@"setsize for layer");
    }
    if(!(_mainWindow.frame.size.height == height && _mainWindow.frame.size.width == width)){
        [_mainWindow setFrame:rect display:true];
        [background setFrame:rect display:true];
        //NSLog(@"setsize for window");
        [self setLocation:_locX forY:_locY];
    }
    
    
    
}



/**
 Set the Hue of the currently playing video
 
 @param hue The hue of the video
 */
-(void) setHue: (double)hue{
    
    _saveHue = hue;
    
    /*
     double percentDone = [self getProgressPercent];
     
     [self loadVid:_lastPlayedFile withOptions:_objOptions stretchToFrame:_stretchVideo];
     [self setProgressPercent:percentDone];
     [self play];
     */
    
    
    
    
}




/**
 Gets the hue of the currently playing video
 
 @return The hue of the video
 */
-(double) getHue{
    return _saveHue;
}


@end



#ifdef __cplusplus
extern "C" {
#endif
    
#ifndef VEC_LEN
#define VEC_LEN(v) (sizeof(v)/sizeof(v[0]))
#endif/*VEC_LEN*/
    
    static JavaVM *javaVM;
    
    /*
     static BOOL isInit();
     static void setRepeat(BOOL);
     static void load(char[], char[], BOOL);
     static void play();
     static char* getLastLocation();
     static void pauseVideo();
     static void stop();
     static BOOL isMute();
     static void setMute(BOOL);
     static double getProgressPercent();
     static void setProgressPercent(double);
     static BOOL isPlaying();
     static BOOL isPaused();
     static BOOL isFinished();
     static void setVisible(BOOL);
     static void setVolume(double);
     static double getVolume();
     static void setLocation(int, int);
     static void setSize(int, int);
     static void setHue(double);
     static double getHue();
     */
    
    static BOOL shouldStretch = false;
    static NSString *passedOptions;
    static NSString *lastPlayedVideo;
    static BOOL isInit(JNIEnv *env, jobject obj) {
        return[instance isInit] && [instance2 isInit];
    }
    
    static void setRepeat(JNIEnv *env, jobject obj, BOOL  input){
        [currentInstance setRepeat:input];
    }
    
    static void setOptions(JNIEnv *env, jobject obj,jstring options){
        if(options == NULL){
            return;
        }
        const jchar *chars = (*env)->GetStringChars(env, options, NULL);
        passedOptions = [NSString stringWithCharacters:(UniChar *)chars  length:(*env)->GetStringLength(env, options)];
        (*env)->ReleaseStringChars(env, options, chars);
        
    }
    
    static void setStretch(JNIEnv *env, jobject obj,BOOL stretch){
        shouldStretch = stretch;
    }
    
    static void loadVid(JNIEnv *env, jobject obj,jstring url){
        
        if(url == NULL){
            return;
        }
        const jchar *chars = (*env)->GetStringChars(env, url, NULL);
        NSString *inputURL = [NSString stringWithCharacters:(UniChar *)chars  length:(*env)->GetStringLength(env, url)];
        (*env)->ReleaseStringChars(env, url, chars);
        
        if(inputURL == nil){
            return;
        }
        
        if([inputURL  isEqual: @""]){
            return;
        }
        lastPlayedVideo = inputURL;
        if(is1){
            
            [instance loadVid:inputURL withOptions:passedOptions stretchToFrame:shouldStretch];
            currentInstance = instance;
            //NSLog(@"loading 1");
        }else{
            [instance2 loadVid:inputURL withOptions:passedOptions stretchToFrame:shouldStretch];
            currentInstance = instance2;
            //NSLog(@"loading 2");
        }
        
    }
    
    static void play(JNIEnv *env, jobject obj){
        //NSLog(@"play");
        [currentInstance play];
        is1 = !is1;
        //[currentInstance play];
    }
    
    static jstring getLastLocation(JNIEnv *env, jobject obj){
        //NSLog(@"getLastLocation");
        NSString *nativeStr = [currentInstance getLastLocation];
        if (nativeStr == NULL)
        {
            return NULL;
        }
        // Note that length returns the number of UTF-16 characters,
        // which is not necessarily the number of printed/composed characters
        jsize buflength = [nativeStr length];
        unichar buffer[buflength];
        [nativeStr getCharacters:buffer];
        jstring javaStr = (*env)->NewString(env, (jchar *)buffer, buflength);
        return javaStr;
    }
    
    static void pauseVideo(JNIEnv *env, jobject obj){
        //NSLog(@"pause video");
        [currentInstance pauseVideo];
        
        
    }
    
    static void stop(JNIEnv *env, jobject obj){
        //NSLog(@"stop-- currently does nothing");
        [currentInstance fadeDown];
    }
    
    static BOOL isMute(JNIEnv *env, jobject obj){
        //NSLog(@"isMute");
        return [currentInstance isMute];
    }
    
    static void setMute(JNIEnv *env, jobject obj,BOOL muted){
        // NSLog(@"setMute");
        [instance setMute:muted];
        [instance2 setMute:muted];
    }
    
    static double getProgressPercent(JNIEnv *env, jobject obj){
        
        return [currentInstance getProgressPercent];
    }
    
    static void setProgressPercent(JNIEnv *env, jobject obj,double percent){
        [currentInstance setProgressPercent:percent];
    }
    
    static BOOL isPlaying(JNIEnv *env, jobject obj){
        return [currentInstance isPlaying];
    }
    
    static BOOL isPaused(JNIEnv *env, jobject obj){
        return [currentInstance isPaused];
    }
    
    static BOOL isFinished(JNIEnv *env, jobject obj){
        return [currentInstance isFinished];
    }
    
    static void setVisible(JNIEnv *env, jobject obj,BOOL visible){
        // NSLog(@"set visible");
        [instance setVisible:visible];
        [instance2 setVisible:visible];
        if(visible){
            [background orderFront:NSApp];
            [background orderBack:NSApp];
            
        }else{
            [background orderOut:NSApp];
            
        }
    }
    
    static void setVolume(JNIEnv *env, jobject obj,double volume){
        [currentInstance setVolume:volume];
    }
    
    static double getVolume(JNIEnv *env, jobject obj){
        return [currentInstance getVolume];
    }
    
    static void setLocation(JNIEnv *env, jobject obj, int x, int y){
        // NSLog(@"set Location");
        [instance setLocation:x forY:y];
        [instance2 setLocation:x forY:y];
        
        
    }
    
    static void setSize(JNIEnv *env, jobject obj,int width, int height){
        //   NSLog(@"set size");
        [instance setSize:width forHeight:height];
        [instance2 setSize:width forHeight:height];
    }
    
    static void setHue(JNIEnv *env, jobject obj,double hue){
        // NSLog(@"set hue--currently does nothing");
        [instance setHue:hue];
        [instance2 setHue:hue];
        
        
        
    }
    static void setFadeSpeed(JNIEnv *env, jobject obj, double fadeSpeed){
        //   NSLog(@"fade speed set: %f", fadeSpeed);
        FADE_SPEED = fadeSpeed;
        
    }
    
    static double getHue(JNIEnv *env, jobject obj){
        //NSLog(@"get Hue");
        return [currentInstance getHue];
        
    }
    
    static long getDuration(JNIEnv *env, jobject obj){
        return [currentInstance getCurrentDuration];
    }
    
    static long getTime(JNIEnv *env, jobject obj){
        return [currentInstance getCurrentTime];
    }
    
    
    static JNINativeMethod Main_methods[] =
    {
        { "isInit", "()Z", (BOOL*)isInit },
        { "setRepeat", "(Z)V", (void*)setRepeat },
        { "setOptions", "(Ljava/lang/String;)V", (void*)setOptions},
        { "setStretch", "(Z)V", (void*)setStretch },
        { "loadVid", "(Ljava/lang/String;)V", (void*)loadVid },
        { "play", "()V", (void*)play },
        { "getLastLocation", "()Ljava/lang/String;", (jstring)getLastLocation },
        { "pauseVideo", "()V", (void*)pauseVideo },
        { "stop", "()V", (void*)stop },
        { "isMute", "()Z", (BOOL*)isMute},
        { "setMute", "(Z)V", (void*)setMute },
        { "getProgressPercent", "()D", (double*)getProgressPercent },
        { "setProgressPercent", "(D)V", (void*)setProgressPercent },
        { "isPlaying", "()Z", (BOOL*)isPlaying },
        { "isPaused", "()Z", (BOOL*)isPaused },
        { "isFinished", "()Z", (BOOL*)isFinished },
        { "setVisible", "(Z)V", (void*)setVisible },
        { "setVolume", "(D)V", (void*)setVolume },
        { "getVolume", "()D", (double*)getVolume },
        { "setLocation", "(II)V",(void*)setLocation },
        { "setSize", "(II)V", (void*)setSize },
        { "setHue", "(D)V", (void*)setHue },
        { "setFadeSpeed", "(D)V", (void*)setFadeSpeed },
        { "getHue", "()D", (double*)getHue},
        { "getDuration", "()J", (long*)getDuration},
        { "getTime", "()J", (long*)getTime},
    };
    
    
    static struct {
        const char      *class_name;
        JNINativeMethod *methods;
        int             num_methods;
    } native_methods[] = {
        { "org/quelea/windows/multimedia/AVPlayerJava", Main_methods, VEC_LEN(Main_methods) },
    };
    
    JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
        JNIEnv *env = 0;
        jclass cls  = 0;
        jint   rs   = 0;
        [AVPlayerJava start];
        if ((*jvm)->GetEnv(jvm, (void**)&env, JNI_VERSION_1_6)) {
            return JNI_ERR;
        }
        
        javaVM = jvm;
        
        for (unsigned int i = 0; i < VEC_LEN(native_methods); i++) {
            cls = (*env)->FindClass(env, native_methods[i].class_name);
            if (cls == NULL) {
                return JNI_ERR;
            }
            rs = (*env)->RegisterNatives(env, cls, native_methods[i].methods, native_methods[i].num_methods);
            assert(rs == JNI_OK);
        }
        
        return JNI_VERSION_1_6;
    }
    
    
    
    
    
#ifdef __cplusplus
}
#endif
