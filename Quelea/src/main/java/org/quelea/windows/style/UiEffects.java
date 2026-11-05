/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.style;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Duration;

/**
 * Small, unobtrusive animations that make the operator interface feel
 * smoother. They only affect the operator windows, never the projection.
 *
 * @author Quelea contributors
 */
public final class UiEffects {

    private static final Duration TAB_FADE = Duration.millis(160);

    private UiEffects() {
        throw new AssertionError();
    }

    /**
     * Fade (and nudge up slightly) the content of a tab when it is selected.
     *
     * @param tabPane the tab pane to animate.
     */
    public static void installTabFade(TabPane tabPane) {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (oldTab != null && newTab != null) {
                fadeIn(newTab);
            }
        });
    }

    private static void fadeIn(Tab tab) {
        Node content = tab.getContent();
        if (content == null) {
            return;
        }
        FadeTransition fade = new FadeTransition(TAB_FADE, content);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(TAB_FADE, content);
        slide.setFromY(4);
        slide.setToY(0);
        ParallelTransition transition = new ParallelTransition(fade, slide);
        transition.setInterpolator(Interpolator.EASE_OUT);
        transition.play();
    }
}
