package org.quelea.services.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for plain text files - also supports some tags.
 *
 * @author tomaszpio, Michael Berry
 */
public class PlainTextParser implements SongParser {

    private static final String DEFAULT_TITLE = "(Unknown)";
    private static final List<String> TITLE_PREFIXES = Arrays.asList("Title:");
    private static final List<String> AUTHOR_PREFIXES = Arrays.asList("Author:");
    private static final List<String> CCLI_PREFIXES = Arrays.asList("CCLI Song #", "CCLI number:", "CCLI:");
    private static final List<String> COPYRIGHT_PREFIXES = Arrays.asList("Copyright:", "©");
    private static final List<String> YEAR_PREFIXES = Arrays.asList("Year:");
    private static final List<String> PUBLISHER_PREFIXES = Arrays.asList("Publisher:");
    private static final List<String> KEY_PREFIXES = Arrays.asList("Key:");
    private static final List<String> CAPO_PREFIXES = Arrays.asList("Capo:");
    private static final List<String> NOTES_PREFIXES = Arrays.asList("Notes:", "Info:");

    private static void setAttribute(SongDisplayable song, String line) {
        assert isAttributeLine(line);
        if (getAttrFromPrefixes(line, TITLE_PREFIXES) != null) {
            song.setTitle(getAttrFromPrefixes(line, TITLE_PREFIXES));
        }
        if (getAttrFromPrefixes(line, AUTHOR_PREFIXES) != null) {
            song.setAuthor(getAttrFromPrefixes(line, AUTHOR_PREFIXES));
        }
        if (getAttrFromPrefixes(line, CCLI_PREFIXES) != null) {
            song.setCcli(getAttrFromPrefixes(line, CCLI_PREFIXES));
        }
        if (getAttrFromPrefixes(line, COPYRIGHT_PREFIXES) != null) {
            song.setCopyright(getAttrFromPrefixes(line, COPYRIGHT_PREFIXES));
        }
        if (getAttrFromPrefixes(line, YEAR_PREFIXES) != null) {
            song.setYear(getAttrFromPrefixes(line, YEAR_PREFIXES));
        }
        if (getAttrFromPrefixes(line, PUBLISHER_PREFIXES) != null) {
            song.setPublisher(getAttrFromPrefixes(line, PUBLISHER_PREFIXES));
        }
        if (getAttrFromPrefixes(line, KEY_PREFIXES) != null) {
            song.setKey(getAttrFromPrefixes(line, KEY_PREFIXES));
        }
        if (getAttrFromPrefixes(line, CAPO_PREFIXES) != null) {
            song.setCapo(getAttrFromPrefixes(line, CAPO_PREFIXES));
        }
        if (getAttrFromPrefixes(line, NOTES_PREFIXES) != null) {
            song.setInfo(getAttrFromPrefixes(line, NOTES_PREFIXES));
        }
    }

    private static boolean isAttributeLine(String line) {
        List<String> allPrefixes = new ArrayList<>();
        allPrefixes.addAll(TITLE_PREFIXES);
        allPrefixes.addAll(AUTHOR_PREFIXES);
        allPrefixes.addAll(CCLI_PREFIXES);
        allPrefixes.addAll(COPYRIGHT_PREFIXES);
        allPrefixes.addAll(YEAR_PREFIXES);
        allPrefixes.addAll(PUBLISHER_PREFIXES);
        allPrefixes.addAll(KEY_PREFIXES);
        allPrefixes.addAll(CAPO_PREFIXES);
        allPrefixes.addAll(NOTES_PREFIXES);

        for (String prefix : allPrefixes) {
            if (startsWithIgnoreCase(line, prefix)) {
                return true;
            }
        }
        return false;
    }

    private static String getAttrFromPrefixes(String str, List<String> startsWith) {
        for (String sw : startsWith) {
            if (startsWithIgnoreCase(str, sw)) {
                return str.substring(sw.length()).trim();
            }
        }
        return null;
    }

    private static boolean startsWithIgnoreCase(String str, String startsWith) {
        return str.toLowerCase().startsWith(startsWith.toLowerCase());
    }

    @Override
    public List<SongDisplayable> getSongs(File f, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        File[] listOfSongs;
        if (f.isDirectory()) {
            listOfSongs = f.listFiles();
        } else {
            listOfSongs = new File[]{f};
        }
        for (int i = 0; i < listOfSongs.length; i++) {
            if (listOfSongs[i].isFile()) {
                final String fileName = listOfSongs[i].getName();
                if (fileName.endsWith(".txt")) {
                    List<String> section = new ArrayList<>();
                    StringBuilder lyrics = new StringBuilder();
                    SongDisplayable song = new SongDisplayable(DEFAULT_TITLE, "");
                    try (BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(listOfSongs[i]), "UTF-8"))) {
                        String line;
                        while ((line = bfr.readLine()) != null) {
                            line = line.replace('\f', '\n');
                            if (!line.isEmpty()) {
                                if (isAttributeLine(line)) {
                                    setAttribute(song, line);
                                } else {
                                    section.add(line);
                                }
                            } else {
                                if(song.getTitle().equals(DEFAULT_TITLE) && section.size()==1) {
                                    song.setTitle(section.get(0));
                                }
                                else if(!isBlankLines(section)) {
                                    for(String sectionLine : section) {
                                        lyrics.append(sectionLine).append('\n');
                                    }
                                    lyrics.append('\n');
                                }
                                section.clear();
                            }
                        }
                        if (!isBlankLines(section)) {
                            for (String sectionLine : section) {
                                lyrics.append(sectionLine).append('\n');
                            }
                            lyrics.append('\n');
                        }
                        song.setLyrics(lyrics.toString());
                        ret.add(song);
                    }
                }
            }
        }
        return ret;
    }
    
    private static boolean isBlankLines(List<String> section) {
        for(String str : section) {
            if(!str.trim().isEmpty()) return false;
        }
        return true;
    }
}
