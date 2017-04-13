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
    private Map<Locale, String> title;

    public TitleProperty() {
        this.title = new HashMap<Locale, String>();
    }


    /**
     * Add title in locale.
     *
     * @param locale
     * @param title
     */
    public void addTitle(Locale locale, String title) {
        if (!this.title.containsKey(locale)) {
            this.title.put(locale, title);
        }
    }


    /**
     * Get title in default locale.
     *
     * @return
     */
    public String getDefaultTitle() {
        return this.title.get(Locale.getDefault());
    }


    /**
     * Get title in specific locale. If nothing happening, get title in default locale.
     *
     * @param locale
     * @return
     */
    public String getTitle(Locale locale) {
        return this.title.get(locale) != null ? this.title.get(locale) : this.getDefaultTitle();
    }


    /**
     * Get available title locales.
     *
     * @return
     */
    public List<Locale> getTitleLocales() {
        List<Locale> locales = new ArrayList<Locale>();
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
