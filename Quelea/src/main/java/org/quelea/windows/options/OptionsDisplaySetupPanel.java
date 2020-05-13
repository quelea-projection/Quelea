/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.preferencesfx.model.Category;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.GraphicsDeviceWatcher;
import org.quelea.windows.main.QueleaApp;

import java.util.HashMap;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * <p/>
 *
 * @author Arvid
 */
public class OptionsDisplaySetupPanel {
    private HashMap<Field, ObservableValue> bindings;
    private DisplayGroup controlScreen;
    private DisplayGroup projectorScreen;
    private DisplayGroup stageScreen;

    /**
     * Create a new display setup panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsDisplaySetupPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        controlScreen = new DisplayGroup(LabelGrabber.INSTANCE.getLabel("control.screen.label"), false, bindings);
        projectorScreen = new DisplayGroup(LabelGrabber.INSTANCE.getLabel("projector.screen.label"), true, bindings);
        stageScreen = new DisplayGroup(LabelGrabber.INSTANCE.getLabel("stage.screen.label"), true, bindings);

        GraphicsDeviceWatcher.INSTANCE.addGraphicsDeviceListener(devices -> {
            QueleaApp.get().getMainWindow().getPreferencesDialog().updatePos();
        });
    }

    Category getDisplaySetupTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("display.options.heading"), new ImageView(new Image("file:icons/setting-ic-monitor.png")), //, new ImageView(new Image("file:icons/monitorsettingsicon.png")),
                controlScreen.getGroup(),
                projectorScreen.getGroup(),
                stageScreen.getGroup()
        );
    }


    boolean isDisplayChange() {
        return controlScreen.isDisplayChange() ||
                projectorScreen.isDisplayChange() ||
                stageScreen.isDisplayChange();
    }

    public void setDisplayChange(boolean displayChange) {
        controlScreen.setDisplayChange(displayChange);
        projectorScreen.setDisplayChange(displayChange);
        stageScreen.setDisplayChange(displayChange);
    }
}
