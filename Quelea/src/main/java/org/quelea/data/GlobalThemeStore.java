/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.data;

import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.QueleaProperties;

/**
 *
 * @author Michael
 */
public class GlobalThemeStore {

    // These are the defaults used by the song and Bible constructors. They
    // differ in layout and some stored style flags from DEFAULT_THEME.
    private static final ThemeDTO DEFAULT_SONG_ITEM_THEME = new ThemeDTO(ThemeDTO.DEFAULT_FONT,
            ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
            ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, -1, 0);
    private static final ThemeDTO DEFAULT_BIBLE_ITEM_THEME = new ThemeDTO(ThemeDTO.DEFAULT_FONT,
            ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
            ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, 3, -1);
    
    private ThemeDTO songThemeOverride;
    private ThemeDTO bibleThemeOverride;

    public void setSongThemeOverride(ThemeDTO songTheme) {
        this.songThemeOverride = songTheme;
    }

    public void setBibleThemeOverride(ThemeDTO bibleTheme) {
        this.bibleThemeOverride = bibleTheme;
    }
    
    public ThemeDTO getTheme(TextDisplayable displayable, TextSection section) {
        if(QueleaProperties.get().getItemThemeOverride() && section.getTheme()!=null && !isDefaultItemTheme(displayable, section.getTheme())) {
            return section.getTheme();
        }
        if(songThemeOverride!=null && displayable instanceof SongDisplayable) {
            return songThemeOverride;
        }
        else if(bibleThemeOverride!=null && displayable instanceof BiblePassage) {
            return bibleThemeOverride;
        }
        return section.getTheme();
    }

    private boolean isDefaultItemTheme(TextDisplayable displayable, ThemeDTO theme) {
        return theme.equalsIgnoreNameAndFontSize(ThemeDTO.DEFAULT_THEME)
                || (displayable instanceof SongDisplayable && theme.equalsIgnoreNameAndFontSize(DEFAULT_SONG_ITEM_THEME))
                || (displayable instanceof BiblePassage && theme.equalsIgnoreNameAndFontSize(DEFAULT_BIBLE_ITEM_THEME));
    }
    
}
