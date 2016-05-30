package org.quelea.services.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.windows.main.StatusPanel;

/**
 *
 * @author tomaszpio
 */
public class PlainTextParser implements SongParser {

    @Override
    public List<SongDisplayable> getSongs(File dirLocation, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<SongDisplayable>();
        File[] listOfSongs = dirLocation.listFiles();
        for (int i = 0; i < listOfSongs.length; i++) {

            if (listOfSongs[i].isFile()) {
                final String fileName = listOfSongs[i].getName();
                if (fileName.substring(fileName.length() - 4, fileName.length()).equals(".txt")) {
                    FileReader reader = new FileReader(listOfSongs[i]);
                    BufferedReader bfr = new BufferedReader(reader);
                    String line = "";
                    boolean titleRead = false;
                    String title = "";
                    String author = "";
                    ArrayList<String> section = new ArrayList<>();
                    int sectionCount = 0;
                    SongDisplayable song = new SongDisplayable(title, "");
                    while ((line = bfr.readLine()) != null) {
                        line = line.replace('\f', '\n');
                        if (!titleRead) {
                            if (!line.isEmpty()) {
                                title = line;
                                titleRead = true;
                                song.setTitle(title);
                            }
                        }
                        if (!line.isEmpty()) {
                            if (line.startsWith("Title: ")) {
                                title = line.substring(7);
                                song.setTitle(title);
                            } else if (line.startsWith("Author: ")) {
                                author = line.substring(8);
                                song.setAuthor(author);
                            } else {
                                section.add(line);
                            }
                        } else {
                            String[] sectionsArray = new String[section.size()];
                            song.addSection(sectionCount,
                                    new TextSection("", section.toArray(sectionsArray), section.toArray(sectionsArray),
                                            true));
                            sectionCount++;
                            section.clear();
                        }
                    }
                    if (section.size() > 0) {
                        String[] sectionsArray = new String[section.size()];
                        song.addSection(sectionCount,
                                new TextSection("", section.toArray(sectionsArray), section.toArray(sectionsArray),
                                        true));
                        section.clear();
                    }
                    ret.add(song);
                }
            }
        }

        return ret;
    }
}
