package org.quelea.windows.main;

import org.quelea.data.displayable.Displayable;

public abstract class DisplayableDrawer {

    protected DisplayCanvas canvas;

    public DisplayableDrawer() {
        this.canvas = null;
    }
    
    public void setCanvas(DisplayCanvas canvas){
        this.canvas = canvas;
    }
    
    public abstract void draw(Displayable displayable);
    
    public abstract void clear();
    
    public abstract void requestFocus();
   }
