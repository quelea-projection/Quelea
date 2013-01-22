package org.quelea.windows.main;

import java.util.logging.Logger;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.utils.LoggerUtils;

public abstract class DisplayableDrawer {
    protected static final Logger LOGGER = LoggerUtils.getLogger();
    protected DisplayCanvas canvas;
    protected DisplayCanvas currentCanvas;
    public DisplayableDrawer() {
        this.canvas = null;
        this.currentCanvas = null;
    }
    
    public void setCanvas(DisplayCanvas canvas){
        this.currentCanvas = this.canvas;
        this.canvas = canvas;
    }
    
    public abstract void draw(Displayable displayable);
    
    public abstract void clear();
    
    public abstract void requestFocus();
   }
