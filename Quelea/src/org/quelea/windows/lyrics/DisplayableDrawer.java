package org.quelea.windows.lyrics;

public abstract class DisplayableDrawer {

    final DisplayCanvas canvas;

    public DisplayableDrawer(DisplayCanvas canvas) {
        this.canvas = canvas;
    }
    
    public abstract void draw();
}
