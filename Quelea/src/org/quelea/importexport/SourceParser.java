/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.importexport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.displayable.Song;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class SourceParser implements SongParser {

    private static final String START_PARSE_LYRICS = "<!-- InstanceBeginEditable name=\"content\" -->";
    private static final String START_PARSE_AUTHOR = "<!-- InstanceBeginEditable name=\"Author\" -->";
    private static final String END_PARSE = "<!-- InstanceEndEditable -->";

    @Override
    public List<Song> getSongs(File location) throws IOException {
        if (!location.isDirectory()) {
            throw new RuntimeException("Must be a directory.");
        }
        File[] files = location.listFiles();
        List<Song> ret = new ArrayList<Song>();
        for (File file : files) {
            if (file.getName().endsWith("htm") || file.getName().endsWith("html")) {
                String contents = Utils.getTextFromFile(file.getAbsolutePath(), null);
                contents = contents.replaceAll("(&nbsp;)+", " ");
                contents = contents.replaceAll(" +", " ");
                contents = contents.replace("&amp;", "&");
                contents = contents.replace("&quot;", "'");
                contents = contents.replace("&lsquo;", "\"");
                contents = contents.replace("&rsquo;", "\"");
                contents = contents.replace("&lt;", "<");
                contents = contents.replace("&gt;", ">");
                String lyrics = getLyrics(contents);
                if (lyrics == null) {
                    continue;
                }
                String title = getTitle(lyrics, 0);
                Song newSong = new Song(title, getAuthor(contents));
                newSong.setLyrics(lyrics);
                ret.add(newSong);
            }
            else {
                System.err.println("Invalid file: " + file.getName());
            }
        }
        return ret;
    }

    private String getTitle(String lyrics, int index) {
        String splitStr = lyrics.split("\n")[index];
        if(splitStr==null) {
            return "";
        }
        String title = splitStr.trim();
        if (title.endsWith(",")) {
            title = title.substring(0, title.length() - 1);
        }
        if (title.endsWith(";")) {
            title = title.substring(0, title.length() - 1);
        }
        if (title.startsWith("'")) {
            title = title.substring(1);
        }
        if (title.startsWith(".")) {
            title = title.substring(1);
        }
        title = title.replaceAll("\\(.+?\\)", "");
        title = title.trim();
        if (title.isEmpty()) {
            return getTitle(lyrics, index + 1);
        }
        return title;
    }

    private String getAuthor(String contents) {
        int beginIndex = contents.indexOf(START_PARSE_AUTHOR) + START_PARSE_AUTHOR.length();
        if (beginIndex == -1) {
            return "";
        }
        contents = contents.substring(beginIndex);
        contents = contents.substring(0, contents.indexOf(END_PARSE));
        return contents.trim();
    }

    private String getLyrics(String contents) {
        int beginIndex = contents.indexOf(START_PARSE_LYRICS) + START_PARSE_LYRICS.length();
        if (beginIndex == -1) {
            return null;
        }
        contents = contents.substring(beginIndex);
        contents = contents.substring(0, contents.indexOf(END_PARSE));
        contents = contents.replace("\n", "");
        StringBuilder lyrics = new StringBuilder();
        Pattern pattern = Pattern.compile("\\<p\\>(.+?)\\</p\\>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {
            String section = matcher.group(0);
            section = section.replace("<br>", "\n");
            section = section.replace("<br/>", "\n");
            section = Utils.removeTags(section);
            String[] lines = section.split("\n");
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].trim();
            }
            StringBuilder verseBuilder = new StringBuilder();
            for (String line : lines) {
                verseBuilder.append(line).append("\n");
            }
            lyrics.append(verseBuilder.toString()).append("\n");
        }
        String ret = lyrics.toString().trim();
        if (ret.isEmpty()) {
            return null;
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        SourceParser parser = new SourceParser();
        List<Song> songs = parser.getSongs(new File("E:\\hymns"));
        for (Song song : songs) {
            System.out.println(song.getTitle());
            System.out.println(song.getAuthor());
            System.out.println(song.getLyrics());
            System.out.println();
            System.out.println("-----");
            System.out.println();
        }
    }
}
