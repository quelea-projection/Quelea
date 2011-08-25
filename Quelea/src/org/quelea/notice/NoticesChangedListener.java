package org.quelea.notice;

import java.util.List;

/**
 *
 * @author Michael
 */
public interface NoticesChangedListener {
    
    void noticesUpdated(List<Notice> notices);
    
}
