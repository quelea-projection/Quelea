package org.quelea.bible;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and manages the available getBibles.
 * @author Michael
 */
public final class BibleManager {

    private static final BibleManager INSTANCE = new BibleManager();
    private final List<Bible> bibles;

    /**
     * Create a new bible manager.
     */
    private BibleManager() {
        bibles = new ArrayList<Bible>();
        loadBibles();
    }

    /**
     * Get the instance of this singleton class.
     * @return the instance of this singleton class.
     */
    public static BibleManager get() {
        return INSTANCE;
    }

    /**
     * Get all the bibles held in this manager.
     * @return all the getBibles.
     */
    public Bible[] getBibles() {
        return bibles.toArray(new Bible[bibles.size()]);
    }

    /**
     * Reload all the bibles from the bibles directory into this bible manager.
     */
    public void loadBibles() {
        bibles.clear();
        File biblesFile = new File("bibles");
        if(!biblesFile.exists()) {
            biblesFile.mkdir();
        }
        for(File file : new File("bibles").listFiles()) {
            if(file.getName().toLowerCase().endsWith(".xml")) {
                Bible bible = Bible.parseBible(file);
                if(bible != null) {
                    bibles.add(bible);
                }
            }
        }
    }

}
