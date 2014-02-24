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
package org.quelea.services.languages.spelling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks text for spelling errors, and provides correction suggestions.
 * <p/>
 * @author Michael
 */
public class Speller {

    public static final String SPELLING_REGEX = "([^\\p{Alnum}'\\-])+";
    private static HashMap<File, HashSet<String>> dictionaries = new HashMap<>();
    private HashSet<String> ignoreWords;
    private HashSet<String> words;
    private Dictionary dict;

    /**
     * Create a new speller with a specified dictionary file. Must exist.
     * <p/>
     * @param dict the dictionary file. Must be a list of words in the
     * dictionary, one per line.
     */
    public Speller(Dictionary dict) {
        ignoreWords = new HashSet<>();
        setDictionary(dict);
    }

    /**
     * Set this speller to use a dictionary.
     * <p/>
     * @param dict the dictionary to use.
     */
    public final void setDictionary(Dictionary dict) {
        if(dict == null) {
            return;
        }
        this.dict = dict;
        if(dictionaries.containsKey(dict.getDictFile())) {
            words = dictionaries.get(dict.getDictFile());
        }
        else {
            words = new HashSet<>();
            dictionaries.put(dict.getDictFile(), words);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dict.getDictFile()), "UTF-8"));
                String line;
                while((line = reader.readLine()) != null) {
                    line = sanitiseWord(line);
                    if(!line.isEmpty()) {
                        dictionaries.get(dict.getDictFile()).add(line);
                    }
                }
            }
            catch(IOException ex) {
                throw new IllegalArgumentException("Error reading dictionary file", ex);
            }
        }
    }

    /**
     * Check to see if a particular word is in the dictionary.
     * <p/>
     * @param word the word to check.
     * @return true if it is correct, false if not.
     */
    public boolean checkWord(String word) {
        word = sanitiseWord(word);
        if(ignoreWords == null || words == null || word.length()==1) {
            return true;
        }
        return words.contains(word) || ignoreWords.contains(word);
    }

    /**
     * Add a word to the ignore list.
     * <p/>
     * @param word the word to add.
     */
    public void addIgnoreWord(String word) {
        word = sanitiseWord(word);
        ignoreWords.add(word);
    }

    /**
     * Get the list of misspelt words, if any, in the given text.
     * <p/>
     * @param text the text to check.
     * @return the list of misspelt words.
     */
    public Set<String> getMisspeltWords(String text) {
        Set<String> ret = new HashSet<>();
        if(text.trim().isEmpty()) {
            return ret;
        }
        String[] tempWords = Pattern.compile(SPELLING_REGEX, Pattern.UNICODE_CHARACTER_CLASS).split(text);
        for(String word : tempWords) {
            if(!checkWord(word)) {
                ret.add(word);
            }
        }
        return ret;
    }

    /**
     * Get suggestions for a misspelt word.
     * <p/>
     * @param misspell the misspelt word.
     * @return the best suggestions from the dictionary file.
     */
    public List<String> getSuggestions(String misspell) {
        PriorityQueue<Suggestion> list = new PriorityQueue<>();
        for(String word : words) {
            int distance = LevenshteinDistance.computeLevenshteinDistance(word, misspell);
            if(word.length() == misspell.length()) {
                distance--;
            }
            if(anagram(word, misspell)) {
                distance -= word.length();
            }
            if(distance < 5) {
                list.add(new Suggestion(word, distance));
            }
        }
        List<String> ret = new ArrayList<>();
        int count = 0;
        for(Suggestion word : list) {
            ret.add(word.getWord());
            count++;
            if(count == 6) {
                break;
            }
        }
        return ret;
    }

    /**
     * Check a block of text to see if the spelling is ok.
     * <p/>
     * @param text the text to check for correctness.
     * @param checkLastWord true if the last word should be checked, false if it
     * should be ignored (since the user may be part way through typing it.)
     * @return true if the check is all ok, false if there are misspelt words.
     */
    public boolean checkText(String text, boolean checkLastWord) {
        if(text.trim().isEmpty()) {
            return true;
        }
        String[] tempWords = Pattern.compile(SPELLING_REGEX, Pattern.UNICODE_CHARACTER_CLASS).split(text);
        for(int i = 0; i < tempWords.length; i++) {
            if(i == tempWords.length - 1 && !checkLastWord) {
                break;
            }
            String word = tempWords[i];
            if(!checkWord(word)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a word to the dictionary file.
     * <p/>
     * @param word the word to add.
     */
    public void addWord(String word) {
        word = sanitiseWord(word);
        try {
            if(!words.contains(word)) {
                words.add(word);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dict.getDictFile(), true), "UTF-8"));
                out.append(System.getProperty("line.separator") + word).close();
            }
        }
        catch(IOException ex) {
            throw new RuntimeException("Couldn't add word to dictionary file", ex);
        }
    }

    private String sanitiseWord(String word) {
        word = word.trim().toLowerCase();
        word = Pattern.compile("[^\\p{Alnum}'\\- ]", Pattern.UNICODE_CHARACTER_CLASS).matcher(word).replaceAll("");
        return word;
    }

    private boolean anagram(String firstWord, String secondWord) {
        char[] word1 = firstWord.toLowerCase().toCharArray();
        char[] word2 = secondWord.toLowerCase().toCharArray();
        Arrays.sort(word1);
        Arrays.sort(word2);
        return Arrays.equals(word1, word2);
    }
}
