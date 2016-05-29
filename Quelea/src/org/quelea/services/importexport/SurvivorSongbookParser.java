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
package org.quelea.services.importexport;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.windows.main.StatusPanel;

/**
 * Parses a PDF from the survivor songbook, this must be the acetates PDF containing only the lyrics (not the guitar
 * chords or sheet music!)
 * @author Michael
 */
public class SurvivorSongbookParser implements SongParser {

    /**
     * Get all the songs in the PDF document.
     * @return a list of all the songs.
     * @throws IOException if something went wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        PDDocument document = PDDocument.load(location);
        List<SongDisplayable> pdfSongs = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();
        List<String> songParts = new ArrayList<>();
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            String pageText = getPageText(document, stripper, i);
            if (pageText.trim().isEmpty()) {
                continue;
            }
            songParts.add(pageText);
            boolean twoPart = pageText.contains("(1 of");
            if (i < document.getNumberOfPages() - 1) { //This section in case the original (1 of x) is missed out
                String nextPageText = getPageText(document, stripper, i + 1);
                if (nextPageText.contains("(2 of")) {
                    twoPart = true;
                }
            }
            if (!twoPart) {
                SongDisplayable song = processSong(songParts.toArray(new String[songParts.size()]));
                if (song != null) {
                    pdfSongs.add(song);
                }
                songParts.clear();
            }
        }
        document.close();
        if (pdfSongs == null) {
            return new ArrayList<>();
        }
        else {
            return pdfSongs;
        }
    }

    /**
     * Get the text on a page in the PDF document.
     * @param document the document.
     * @param stripper the PDF stripper used to get the text.
     * @param page     the page number.
     * @return the text on the given page.
     * @throws IOException if something went wrong.
     */
    private String getPageText(PDDocument document, PDFTextStripper stripper, int page) throws IOException {
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        StringWriter textWriter = new StringWriter();
        stripper.writeText(document, textWriter);
        return textWriter.toString().replace("’", "'").replace("`", "'");
    }

    /**
     * Given a number of parts, get a song.
     * @param parts the parts (one part per page in the PDF) of the song.
     * @return the song object from these parts.
     */
    private SongDisplayable processSong(String[] parts) {
        //May look like I'm checking the same thing twice, but I'm not I promise!!!
        if (parts[0].contains("first line")
                || parts[0].contains("first line")
                || parts[0].contains("Thank you for your support!")
                || parts[0].contains("Thank you for your support!")) {
            return null;
        }
        String author = "";
        for (int i = 0; author.isEmpty() && i < parts.length; i++) {
            author = getAuthor(parts[i]);
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replaceAll("\\([0-9] of [0-9]\\)", "\n"); //Remove (x of x) text
            parts[i] = removeFooter(parts[i]);
        }
        StringBuilder songLyrics = new StringBuilder();
        for (String part : parts) {
            for (String line : part.split("\n")) {
                String trimLine = line.trim();
                if (!trimLine.isEmpty() && trimLine.charAt(0) == '(') { //Remove brackets from first words
                    trimLine = trimLine.replace("(", "");
                    trimLine = trimLine.replace(")", "");
                }
                if (!trimLine.isEmpty() && trimLine.charAt(0) == '_') { //Remove starting underscores
                    trimLine = trimLine.substring(1);
                }
                if (!trimLine.toLowerCase().contains("(chorus)")) { //Remove starting chorus markers
                    songLyrics.append(trimLine).append("\n");
                }
            }
            songLyrics.append("\n");
        }
        String songLyricsStr = songLyrics.toString().trim();
        String title = songLyricsStr.split("\n")[0];
        if (!title.isEmpty() && !Character.isLetterOrDigit(title.charAt(title.length() - 1))) { //Remove ending punctuation from titles
            title = title.substring(0, title.length() - 1);
        }
        SongDisplayable song = new SongDisplayable(title, author);
        song.setLyrics(songLyricsStr);
        song.removeDuplicateSections();
        return song;
    }

    /**
     * Remove the footer (copyright information, ccli number, all that jazz.)
     * @param text the page.
     * @return the text with the footer removed.
     */
    private String removeFooter(String text) {
        String[] parts = text.split("\n");
        int endIndex = -1;
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].toLowerCase().contains("ccl licence no.")
                    || parts[i].contains("©")
                    || parts[i].toLowerCase().contains("copyright")
                    || parts[i].toLowerCase().contains("(c)")
                    || parts[i].toLowerCase().contains("kingswaysongs")) {
                endIndex = i;
                break;
            }
        }
        if (endIndex == -1) {
            return text;
        }
        int startIndex = endIndex - 1;
        while (parts[startIndex].trim().isEmpty()) {
            startIndex--;
        }
        while (!parts[startIndex].trim().isEmpty()) {
            startIndex--;
        }
        while (parts[startIndex].trim().isEmpty()) {
            startIndex--;
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i <= startIndex; i++) {
            ret.append(parts[i]).append("\n");
        }
        return ret.toString();
    }

    /**
     * Get the author from the text. Footer must be on when this method is called!
     * @param text the page text.
     * @return the author.
     */
    private String getAuthor(String text) {
        String[] parts = text.split("\n");
        int index = -1;
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].toLowerCase().contains("copyright")
                    || parts[i].contains("©")) {
                index = i - 1;
                break;
            }
        }
        if (index > -1) {
            return parts[index].trim();
        }
        else {
            int i = parts.length - 5;
            if (i < 0) {
                i = 0;
            }
            for (; i < parts.length; i++) {
                if (parts[i].trim().equalsIgnoreCase("Traditional")) {
                    return parts[i];
                }
            }
            return "";
        }
    }
}
