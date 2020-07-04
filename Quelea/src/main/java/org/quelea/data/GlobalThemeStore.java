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
    
    private ThemeDTO songThemeOverride;
    private ThemeDTO bibleThemeOverride;

    public void setSongThemeOverride(ThemeDTO songTheme) {
        this.songThemeOverride = songTheme;
    }

    public void setBibleThemeOverride(ThemeDTO bibleTheme) {
        this.bibleThemeOverride = bibleTheme;
    }
    
    public ThemeDTO getTheme(TextDisplayable displayable, TextSection section) {
        if(QueleaProperties.get().getItemThemeOverride() && section.getTheme()!=null && !section.getTheme().equalsIgnoreNameAndFontSize(ThemeDTO.DEFAULT_THEME)) {
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
    
}
