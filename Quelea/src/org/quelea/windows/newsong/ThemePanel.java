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
package org.quelea.windows.newsong;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import org.quelea.data.ThemeDTO;
import static org.quelea.data.ThemeDTO.BIBLE_DEFAULT_BACKGROUND;
import static org.quelea.data.ThemeDTO.BIBLE_DEFAULT_FONT;
import static org.quelea.data.ThemeDTO.BIBLE_DEFAULT_FONT_COLOR;
import static org.quelea.data.ThemeDTO.BIBLE_DEFAULT_SHADOW;
import org.quelea.services.languages.LabelGrabber;

/**
 * The panel where the user chooses what visual theme a song should have.
 * <p/>
 * @author Michael
 */
public class ThemePanel extends TabPane {

    
    private String saveHash = "";
    private final Button confirmButton;
    public static final String[] SAMPLE_LYRICS = {"Amazing Grace how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private final Tab songTab = new Tab(LabelGrabber.INSTANCE.getLabel("theme.song.tab.text"));
    private final Tab bibleTab = new Tab(LabelGrabber.INSTANCE.getLabel("theme.bible.tab.text"));
    private final ThemeSettingsPane songPane;
    private final ThemeSettingsPane biblePane;
    private final boolean showBible;

    /**
     * Create and initialise the theme panel
     */
    public ThemePanel() {
        this(null, null, true);
    }

    /**
     * Create and initialise the theme panel.
     * <p>
     * @param wordsArea the text area to use for words. If null, sample lyrics
     * will be used.
     */
    public ThemePanel(TextArea wordsArea, Button confirmButton, boolean showBible) {
        this.showBible = showBible;
        this.getTabs().add(songTab);
        this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        songPane = new ThemeSettingsPane(wordsArea, confirmButton, false, this);
        biblePane = new ThemeSettingsPane(wordsArea, confirmButton, true, this);
        songTab.setContent(songPane);
        bibleTab.setContent(biblePane);
        if (showBible) {
            this.getTabs().add(bibleTab);
        }

        this.confirmButton = confirmButton;
    }

    /**
     * Determine if the save hash has changed since resetSaveHash() was last
     * called.
     * <p>
     * @return true if the hash has changed, false otherwise.
     */
    public boolean hashChanged() {
        return !getSaveHash().equals(saveHash);
    }

    /**
     * Reset the save hash to the current state of the panel.
     */
    public void resetSaveHash() {
        saveHash = getSaveHash();
    }

    /**
     * Get the current save hash.
     *
     * @return the current save hash.
     */
    private String getSaveHash() {
        return Integer.toString(getTheme().hashCode());
    }

    /**
     * Set the current theme to represent in this panel.
     * <p/>
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {

        songPane.setTheme(theme, false);
        biblePane.setTheme(theme, true);
    }

    /**
     * Get the theme currently represented by the state of this panel.
     * <p/>
     * @return the current theme.
     */
    public ThemeDTO getTheme() {
        ThemeToolbar songToolbar = songPane.getToolbar();
        ThemeToolbar bibleToolbar = biblePane.getToolbar();

        if(showBible) {
            return new ThemeDTO(songToolbar.getThemeFont(), songToolbar.getThemeFontColor(), songToolbar.getTranslateFont(), songToolbar.getTranslateFontColor(),
                songToolbar.getThemeBackground(), songToolbar.getThemeShadow(), songToolbar.getThemeFont().isBold(), songToolbar.getThemeFont().isItalic(),
                songToolbar.getTranslateFont().isBold(), songToolbar.getTranslateFont().isItalic(), songPane.getTextPosition(), songToolbar.getTextAlignment(),
                BIBLE_DEFAULT_FONT, BIBLE_DEFAULT_FONT_COLOR, BIBLE_DEFAULT_BACKGROUND, BIBLE_DEFAULT_SHADOW, BIBLE_DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("bold"), BIBLE_DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("italic"), -1, -1);
        }
        else { 
            return new ThemeDTO(songToolbar.getThemeFont(), songToolbar.getThemeFontColor(), songToolbar.getTranslateFont(), songToolbar.getTranslateFontColor(),
                songToolbar.getThemeBackground(), songToolbar.getThemeShadow(), songToolbar.getThemeFont().isBold(), songToolbar.getThemeFont().isItalic(),
                songToolbar.getTranslateFont().isBold(), songToolbar.getTranslateFont().isItalic(), songPane.getTextPosition(), songToolbar.getTextAlignment(),
                bibleToolbar.getThemeFont(), bibleToolbar.getThemeFontColor(), bibleToolbar.getThemeBackground(), bibleToolbar.getThemeShadow(),
                bibleToolbar.getThemeFont().isBold(), bibleToolbar.getThemeFont().isItalic(), biblePane.getTextPosition(), bibleToolbar.getTextAlignment());
        }
    }
}
