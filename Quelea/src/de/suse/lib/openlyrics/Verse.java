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

package de.suse.lib.openlyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verse object of the OpenLyrics set of verses.
 * 
 * @author bo
 */
public class Verse {

    private String name;
    private List<VerseLine> lines;


    /**
     * Constructor.
     *
     */
    public Verse() {
        this.lines = new ArrayList<VerseLine>();
    }


    /**
     * Add verse line.
     *
     * @param line
     */
    public void addLine(VerseLine line) {
        this.lines.add(line);
    }


    /**
     * Get lines in the verse.
     *
     * @return
     */
    public List<VerseLine> getLines() {
        return Collections.unmodifiableList(this.lines);
    }


    /**
     * Set name of the verse (ID)
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Get the name (ID) of the verse
     * @return
     */
    public String getName() {
        return this.name;
    }
}
