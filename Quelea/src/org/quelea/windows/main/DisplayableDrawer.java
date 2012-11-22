package org.quelea.windows.main;

public abstract class DisplayableDrawer {

    final protected DisplayCanvas canvas;

    public DisplayableDrawer(DisplayCanvas canvas) {
        this.canvas = canvas;
    }
    
    public abstract void draw();
}
