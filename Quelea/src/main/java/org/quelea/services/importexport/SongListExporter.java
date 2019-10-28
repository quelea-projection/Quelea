/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.importexport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.quelea.data.db.SongManager;
import org.quelea.services.utils.LoggerUtils;

/**
 * Export a list of song titles and authors to a particular file.
 * @author Michael
 */
public class SongListExporter {

	private static final Logger LOGGER = LoggerUtils.getLogger();

	public void exportToFile(File file) {
		final List<String> songNames = Arrays.stream(SongManager.get().getSongs())
				.map(s -> escape(s.getTitle()) + "," + escape(s.getAuthor()))
				.collect(Collectors.toList());

		try {
			Files.write(file.toPath(), String.join("\n", songNames).getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
		} catch (IOException ex) {
			LOGGER.log(Level.INFO, "Exception exporting song list", ex);
		}
	}
	
	private String escape(String csvEle) {
		return "\"" + csvEle.replace("\"", "\\\"") + "\"";
	}

}
