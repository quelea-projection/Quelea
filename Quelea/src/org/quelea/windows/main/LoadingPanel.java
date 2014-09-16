/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import org.quelea.windows.splash.Pips;

/**
 *
 * @author Greg
 */
public class LoadingPanel extends AbstractPanel {

    private Font loadingFont = Font.loadFont("file:icons/Ubuntu-RI.ttf", 60);
    private LinearGradient loadingGrad = new LinearGradient(0, 1, 0, 0, true, CycleMethod.REPEAT, new Stop(0, Color.web("#666666")), new Stop(1, Color.web("#000000")));
    private Pips loadingDots;

    /**
     * Start the loading animation
     */
    public void startLoading() {
        loadingDots = new Pips(loadingFont, loadingGrad);
        setCenter(loadingDots);
    }

    /**
     * Stop the loading animation and remove
     */
    public void stopLoading() {
        loadingDots = null;
        setCenter(null);
    }

    /**
     * NOT USED!
     *
     * @return -1
     */
    @Override
    public int getCurrentIndex() {
        return -1;
    }

    /**
     * NOT USED!
     *
     * @param canvas does nothing
     * @return does nothing
     */
    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        return null;
    }

}
