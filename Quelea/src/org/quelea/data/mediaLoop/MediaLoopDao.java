package org.quelea.data.mediaLoop;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.quelea.data.db.model.MediaLoop;
import org.quelea.services.utils.LoggerUtils;

/**
 * Implements mediaLoop entities retrieval operations.
 *
 * @author tomaszpio@gmail.com
 */
public class MediaLoopDao {

    private Session session = null;

    public MediaLoopDao(Session session) {
        this.session = session;
    }

    public MediaLoop getMediaLoopById(long id) {
        return (MediaLoop) session.load(MediaLoop.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<MediaLoop> getMediaLoops() {
        Query getAllMediaLoopQuery = session.createQuery("from MediaLoop");
        List<MediaLoop> loops = new ArrayList();
        try {
            loops = getAllMediaLoopQuery.list();
        } catch (HibernateException ex) {
            //media loop not compatible
            LoggerUtils.getLogger().log(Level.SEVERE, ex.getMessage(), ex.getStackTrace());
        }
        return loops;
    }

}
