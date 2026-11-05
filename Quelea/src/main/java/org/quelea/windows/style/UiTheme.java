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

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.ColorScheme;
import javafx.application.Platform;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * The colour themes available for the operator interface. Each theme is a
 * palette file in {@code org/quelea/style/} (see theme-light.css for the list
 * of colour names a theme defines).
 * <p>
 * To add a theme: create {@code theme-<id>.css}, add a constant here and add
 * its label to the language files.
 *
 * @author Quelea contributors
 */
public enum UiTheme {

    /**
     * Follows the operating system's light / dark setting at start-up.
     */
    SYSTEM("system", "theme.system.label", null, false),
    LIGHT("light", "default.theme.label", "theme-light.css", false),
    DARK("dark", "dark.theme.label", "theme-dark.css", true),
    MIDNIGHT("midnight", "theme.midnight.label", "theme-midnight.css", true),
    WARM("warm", "theme.warm.label", "theme-warm.css", false),
    HIGH_CONTRAST("high-contrast", "theme.highcontrast.label", "theme-high-contrast.css", true);

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static UiTheme resolvedSystemTheme;

    private final String id;
    private final String labelKey;
    private final String stylesheet;
    private final boolean dark;

    UiTheme(String id, String labelKey, String stylesheet, boolean dark) {
        this.id = id;
        this.labelKey = labelKey;
        this.stylesheet = stylesheet;
        this.dark = dark;
    }

    /**
     * @return the id stored in quelea.properties.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the (translated) name shown in the options window.
     */
    public String getLabel() {
        return LabelGrabber.INSTANCE.getLabel(labelKey);
    }

    /**
     * @return true if this theme uses light text on dark surfaces. For
     * {@link #SYSTEM} this is the resolved system setting.
     */
    public boolean isDark() {
        return this == SYSTEM ? resolveSystem().dark : dark;
    }

    /**
     * @return the palette stylesheet, as a resource path.
     */
    public String getStylesheet() {
        UiTheme theme = this == SYSTEM ? resolveSystem() : this;
        return "org/quelea/style/" + theme.stylesheet;
    }

    /**
     * @return the theme chosen in the options (never null).
     */
    public static UiTheme current() {
        return fromId(QueleaProperties.get().getUiTheme());
    }

    /**
     * Find a theme by its stored id.
     *
     * @param id the id, may be null.
     * @return the theme, or {@link #LIGHT} if the id is unknown.
     */
    public static UiTheme fromId(String id) {
        for (UiTheme theme : values()) {
            if (theme.id.equals(id)) {
                return theme;
            }
        }
        return LIGHT;
    }

    /**
     * Find a theme by the label shown in the options window.
     *
     * @param label the label.
     * @return the theme, or null if there is none with that label.
     */
    public static UiTheme fromLabel(String label) {
        for (UiTheme theme : values()) {
            if (theme.getLabel().equals(label)) {
                return theme;
            }
        }
        return null;
    }

    /**
     * Work out light or dark from the operating system, once: the theme only
     * changes after a restart.
     */
    private static synchronized UiTheme resolveSystem() {
        if (resolvedSystemTheme == null) {
            try {
                resolvedSystemTheme = Platform.getPreferences().getColorScheme() == ColorScheme.DARK ? DARK : LIGHT;
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Couldn't read the system colour scheme, using the light theme", ex);
                resolvedSystemTheme = LIGHT;
            }
        }
        return resolvedSystemTheme;
    }
}
