/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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



//this file is used to create a standard way of accessing VLC windows. This allows users to determine if they want to use the VLC Direct Media Player or the VLC Embedded Media Player
package org.quelea.windows.multimedia;

import org.quelea.services.utils.QueleaProperties;


public abstract class VLCWindow {
   
    public static final VLCWindow INSTANCE = getInstance();
    
    public static final VLCWindow getInstance(){
       QueleaProperties props = QueleaProperties.get();
       if(props.getVLCAdvanced()){
           return VLCWindowAdvancedEmbed.INSTANCE;
       }else{
           return VLCWindowEmbed.INSTANCE;
       }
    }
    
    public abstract boolean isInit();
    public abstract void setRepeat(final boolean repeat);
    public abstract void load(final String path);
    public abstract  void play();
    public abstract void play(final String vid);
    public abstract String getLastLocation();
    public abstract void pause();
    public abstract void stop();
    public abstract boolean isMute();
    public abstract void setMute(final boolean mute);
    public abstract double getProgressPercent();
    public abstract void setProgressPercent(final double percent);
    public abstract boolean isPlaying();
    public abstract boolean isPaused();
    public abstract void setOnFinished(final Runnable onFinished);
    public abstract void show();
    public abstract void hide();
    public abstract void setHideButton(final boolean hide);
    public abstract void setLocation(final int x, final int y);
    public abstract void setSize(final int width, final int height);
    public abstract void refreshPosition();
    public abstract void fadeHue(final double hue);
    public abstract void setHue(final double hue);
    public abstract double getHue();
    
    
    
}
