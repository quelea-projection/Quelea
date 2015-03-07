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
package org.quelea.windows.lyrics;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

/**
 * A tab specifically designed for a translation for a song - holds the
 * translated lyrics.
 *
 * @author Michael
 */
public class TranslateTab extends Tab {

    private final String name;
    private final LyricsTextArea lyricsArea;

    /**
     * Create a new translate tab.
     * @param name the name of the tab (usually the language.)
     * @param lyrics the translated lyrics (can be blank.)
     */
    public TranslateTab(String name, String lyrics) {
        super(name);
        setOnCloseRequest(new EventHandler<Event>() {

            @Override
            public void handle(Event tabEvent) {
                String nameReplace = name;
                if(QueleaProperties.get().getLanguageFile().getName().equalsIgnoreCase("sv.lang")) { //Language names should be in lower case for Swedish
                    nameReplace = nameReplace.toLowerCase();
                }
                Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.translation.title"), LabelGrabber.INSTANCE.getLabel("delete.translation.text").replace("$1", nameReplace))
                        .addYesButton((event1) -> {}).addNoButton((ActionEvent buttonEvent) -> {
                            tabEvent.consume();
                }).build().showAndWait();
            }
        });
        this.name = name;
        setClosable(true);
        lyricsArea = new LyricsTextArea();
        lyricsArea.replaceText(lyrics);
        setContent(lyricsArea);
    }

    /**
     * Get the translation's name.
     * @return the translation's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the translation's lyrics.
     * @return the translation's lyrics.
     */
    public String getLyrics() {
        return lyricsArea.getText();
    }
    
    /**
     * Set the lyrics.
     * @param lyrics the lyrics to set on the lyrics area.
     */
    public void setLyrics(String lyrics) {
        lyricsArea.replaceText(lyrics);
    }
}
