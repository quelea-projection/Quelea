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

import de.suse.lib.openlyrics.OpenLyricsException;

/**
 * Tempo property for the OpenLyrics Object.
 *
 * @author bo
 */
public class TempoProperty {
    public static final String TYPE_BPM = "bpm";
    public static final String TYPE_TEXT = "text";
    private String tempo;
    private String type;


    /**
     * Exception for the tempo property.
     */
    public static class TempoPropertyException extends OpenLyricsException {
        public TempoPropertyException(String message) {
            super(message);
        }
    }

    /**
     * Constructor of the tempo property.
     *
     * @param tempo
     * @param type
     * @throws de.immanuel.lib.openlyrics.TempoProperty.TempoPropertyException
     */
    public TempoProperty(String tempo, String type) throws TempoPropertyException {
        this.tempo = tempo;
        this.type = type;
        if (!this.type.equals(TempoProperty.TYPE_BPM) && !this.type.equals(TempoProperty.TYPE_TEXT)) {
            throw new TempoPropertyException("Unknown tempo type: " + type);
        }
    }

    /**
     * Get type of the tempo (bpm or text).
     *
     * @return
     */
    public String getType() {
        return type;
    }


    /**
     * Get tempo.
     *
     * @return
     */
    public String getTempo() {
        return tempo;
    }
}
