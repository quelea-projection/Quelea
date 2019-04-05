/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.utils;

import java.util.List;
import org.quelea.services.utils.LyricLine;

/**
 *
 * @author Michael
 */
public class WrapTextResult {

    List<LyricLine> newText;
    double fontSize;

    public WrapTextResult(List<LyricLine> newText, double fontSize) {
        this.newText = newText;
        this.fontSize = fontSize;
    }
    
    public List<LyricLine> getNewText() {
        return newText;
    }

    public double getFontSize() {
        return fontSize;
    }
    
    
}
