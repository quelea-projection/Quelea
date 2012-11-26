package org.quelea.windows.main;

public abstract class DisplayableDrawer {

    protected DisplayCanvas canvas;

    public DisplayableDrawer() {
        this.canvas = null;
    }
    
    public void setCanvas(DisplayCanvas canvas){
        this.canvas = canvas;
    }
    
    public abstract void draw();
    
    public abstract void clear();
    
    public abstract void requestFocus();
   }
