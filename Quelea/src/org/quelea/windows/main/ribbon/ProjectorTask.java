package org.quelea.windows.main.ribbon;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 *
 * @author Michael
 */
public class ProjectorTask extends RibbonTask {
    
    public ProjectorTask() {
        super("Projector", getControlBand());
    }

    private static JRibbonBand getControlBand() {
        JRibbonBand controlBand = new JRibbonBand("Controls", RibbonUtils.getRibbonIcon("icons/projector.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(controlBand);
        JCommandButton onButton = new JCommandButton("On", RibbonUtils.getRibbonIcon("icons/poweron.png", 100, 100));
        controlBand.addCommandButton(onButton, RibbonElementPriority.TOP);
        onButton.setEnabled(false);
        JCommandButton offButton = new JCommandButton("Off", RibbonUtils.getRibbonIcon("icons/exit.png", 100, 100));
        controlBand.addCommandButton(offButton, RibbonElementPriority.TOP);
        offButton.setEnabled(false);
        JCommandButton inputButton = new JCommandButton("Switch Input", RibbonUtils.getRibbonIcon("icons/projectorinput.png", 100, 100));
        controlBand.addCommandButton(inputButton, RibbonElementPriority.TOP);
        inputButton.setEnabled(false);
        return controlBand;
    }
}
