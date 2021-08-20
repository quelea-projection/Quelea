/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.quelea.data.ThemeDTO;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 *
 * @author Michael
 */
public class ThemeUtils {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    
    /**
     * Get a list of themes currently in use on this window.
     * <p/>
     * @return the list of themes displayed.
     */
    public static ObservableList<ThemeDTO> getThemes() {
        List<ThemeDTO> themesList = new ArrayList<>();
        File themeDir = new File(QueleaProperties.get().getQueleaUserHome(), "themes");
        if (!themeDir.exists()) {
            themeDir.mkdir();
        }
        for (File file : themeDir.listFiles()) {
            if (file.getName().endsWith(".th")) {
                String fileText = Utils.getTextFromFile(file.getAbsolutePath(), "");
                if (fileText.trim().isEmpty()) {
                    continue;
                }
                final ThemeDTO theme = ThemeDTO.fromString(fileText, Collections.emptyMap());
                if (theme == ThemeDTO.DEFAULT_THEME) {
                    LOGGER.log(Level.WARNING, "Error parsing theme file: {0}", fileText);
                    continue;  //error
                }
                theme.setFile(file);
                themesList.add(theme);
            }
        }
        return FXCollections.observableArrayList(themesList);
    }
    
}
