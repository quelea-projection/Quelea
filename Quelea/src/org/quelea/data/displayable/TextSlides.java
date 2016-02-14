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
package org.quelea.data.displayable;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.Background;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.BibleVerse;
import org.quelea.services.utils.Utils;

/**
 *
 * @author Fabian
 */


public class TextSlides implements TextDisplayable, Serializable {
    
    private List<TextSection> textSections;
    private ThemeDTO theme;
    private String title;
    
    public TextSlides(String title, List<TextSection> textSections) {
        this(title, textSections, new ThemeDTO(ThemeDTO.DEFAULT_FONT,
                ThemeDTO.DEFAULT_FONT_COLOR, ThemeDTO.DEFAULT_FONT, ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR,
                ThemeDTO.DEFAULT_BACKGROUND, ThemeDTO.DEFAULT_SHADOW, false, false, false, true, 3, -1));
    }
    
    public TextSlides(String title, List<TextSection> textSections, ThemeDTO theme) {
        this.textSections = textSections;
        this.theme = theme;
        this.title = title;
    }
    
    /**
     * Get the text sections in this passage.
     * <p>
     * @return the text sections in this passage.
     */
    @Override
    public TextSection[] getSections() {
        return textSections.toArray(new TextSection[textSections.size()]);
    }
    
    /**
     * Get the current theme
     * @return the theme
     */
    @Override
    public ThemeDTO getTheme() {
        return this.theme;
    }
    
    /**
     * Set the current theme
     * @param theme the theme to set 
     */
    @Override
    public void setTheme(ThemeDTO theme) {
        this.theme = theme;
        for (TextSection ts : getSections()) {
            ts.setTheme(theme);
        }
    }
    
    /**
     * Don't need any resources, return an empty collection.
     * <p>
     * @return an empty list, always.
     */
    @Override
    public Collection<File> getResources() {
        ArrayList<File> ret = new ArrayList<>();
        for (TextSection section : getSections()) {
            ThemeDTO sectionTheme = section.getTheme();
            if (sectionTheme != null) {
                Background background = sectionTheme.getBackground();
                ret.addAll(background.getResources());
            }
        }
        return ret;
    }
    
    @Override
    public void dispose() {
        //Nothing needed here.
    }
    
    /**
     * Get the bible preview icon.
     * <p>
     * @return the bible preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/text.png"));
    }

    /**
     * Get the preview text.
     * <p>
     * @return the preview text.
     */
    @Override
    public String getPreviewText() {
        return title;
    }
    
    /**
     * Get the XML behind this bible passage.
     * <p/>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<textslides title=\"");
        ret.append(Utils.escapeXML(title));
        ret.append("\">");
        /* // TODO: not yet complete!
        for (TextSection section : getSections()) {
            ret.append(section.toXML());
        }*/
        ret.append("<theme>");
        ret.append(theme.asString());
        ret.append("</theme>");
        ret.append("</passage>");
        return ret.toString();
    }
    
    /**
     * We support clear, so return true.
     * <p>
     * @return true, always.
     */
    @Override
    public boolean supportClear() {
        return true;
    }
}
