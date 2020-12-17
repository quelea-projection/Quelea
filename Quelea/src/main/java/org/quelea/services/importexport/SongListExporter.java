/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.importexport;

import org.quelea.data.db.SongManager;
import org.quelea.services.utils.LoggerUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Export a list of song titles and authors to a particular file.
 *
 * @author Michael
 */
public class SongListExporter {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    public void exportToFile(File file) {
        final List<String> songNames = Arrays.stream(SongManager.get().getSongs())
                .map(s -> {
                    String out = escape(s.getTitle()) + "," + escape(s.getAuthor());
                    if (s.getCcli() != null && !s.getCcli().isEmpty()) {
                        out += "," + s.getCcli();
                    }
                    return out;
                })
                .collect(Collectors.toList());

        try {
            Files.write(file.toPath(), String.join("\n", songNames).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Exception exporting song list", ex);
        }
    }

    private String escape(String csvEle) {
        return "\"" + csvEle.replace("\"", "\\\"") + "\"";
    }

}
