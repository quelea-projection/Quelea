/*
 * Author: Bo Maryniuk <bo@suse.de>
 *
 * Copyright (c) 2013 Bo Maryniuk. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *     3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY BO MARYNIUK "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package de.suse.lib.openlyrics.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Title property for OpenLyrics object.
 *
 * @author bo
 */
public class TitleProperty {
    private Map<Locale, List<String>> title;

    public TitleProperty() {
        this.title = new HashMap<>();
    }
    
    public List<String> getTitles(Locale locale) {
        return title.get(locale);
    }

    /**
     * Add title in locale.
     *
     * @param locale
     * @param title
     */
    public void addTitle(Locale locale, String title) {
        if (!this.title.containsKey(locale)) {
            this.title.put(locale, new ArrayList<>());
        }
        this.title.get(locale).add(title);
    }
    
    public List<String> getAllTitles() {
        List<String> ret = new ArrayList<>();
        for(List<String> titles : title.values()) {
            ret.addAll(titles);
        }
        return ret;
    }


    /**
     * Get title in default locale.
     *
     * @return
     */
    public String getDefaultTitle() {
        List<String> titles = this.title.get(Locale.getDefault());
        if(titles!=null && !titles.isEmpty()) {
            return titles.get(0);
        }
        return null;
    }

    /**
     * Get available title locales.
     *
     * @return
     */
    public List<Locale> getTitleLocales() {
        List<Locale> locales = new ArrayList<>();
        Iterator<Locale> titleIterator = this.title.keySet().iterator();
        while (titleIterator.hasNext()) {
            locales.add(titleIterator.next());
        }

        if (!locales.contains(Locale.getDefault())) {
            locales.add(Locale.getDefault());
        }

        return Collections.unmodifiableList(locales);
    }
}
