package org.quelea.services.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for plain text files - also supports some tags and chordpro chords.
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
    
    private void addSectionToLyrics(List<String> section, StringBuilder lyrics) {
        for (String sectionLine : section) {
            Pattern pattern = Pattern.compile("\\[" + LineTypeChecker.CHORD_REGEX + "\\]");
            Matcher matcher = pattern.matcher(sectionLine);
            
            sectionLine = sectionLine.replaceAll("\\[" + LineTypeChecker.CHORD_REGEX + "\\]", "");

            StringBuilder chordLine = new StringBuilder();
            int offset = 0;
            while (matcher.find()) {
                int chordPos = matcher.start() - offset;
                while (chordLine.length() < chordPos) {
                    chordLine.append(' ');
                }
                if (chordLine.length() > chordPos) {
                    chordLine.append(' ');
                    
                    String startStr = sectionLine.substring(0,chordPos);
                    String endStr = sectionLine.substring(chordPos,sectionLine.length());
                    
                    sectionLine = startStr;
                    for(int i=0 ; i<chordLine.length() - chordPos ; i++) {
                        sectionLine += "_";
                        offset--;
                    }
                    sectionLine += endStr;
                }
                offset += matcher.group().length();
                chordLine.append(matcher.group().substring(1, matcher.group().length() - 1));
            }

            if (!chordLine.toString().isEmpty()) {
                lyrics.append(chordLine.toString()).append('\n');
            }

            lyrics.append(sectionLine).append('\n');
        }
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
                    String defaultSongTitle = fileName.substring(0,fileName.length()-4);
                    if(defaultSongTitle.isEmpty()) {
                        defaultSongTitle = DEFAULT_TITLE;
                    }
                    SongDisplayable song = new SongDisplayable(defaultSongTitle, "");
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
                                if (song.getTitle().equals(DEFAULT_TITLE) && section.size() == 1) {
                                    song.setTitle(section.get(0));
                                } else if (!isBlankLines(section)) {
                                    addSectionToLyrics(section, lyrics);
                                    lyrics.append('\n');
                                }
                                section.clear();
                            }
                        }
                        if (!isBlankLines(section)) {
                            addSectionToLyrics(section, lyrics);
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
        for (String str : section) {
            if (!str.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
