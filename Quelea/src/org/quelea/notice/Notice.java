package org.quelea.notice;

/**
 *
 * @author Michael
 */
public class Notice {
    
    private String str;
    private int times;

    public Notice(String str, int times) {
        this.str = str;
        this.times = times;
    }
    
    public void copyAttributes(Notice other) {
        this.str = other.str;
        this.times = other.times;
    }
    
    public String getStr() {
        return str;
    }

    public int getTimes() {
        return times;
    }
    
    public void decrementTimes() {
        times--;
    }
    
    public String toString() {
        return str;
    }
    
}
