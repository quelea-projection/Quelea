package org.quelea.data.mediaLoop;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.quelea.data.db.model.MediaLoop;
/**
 * Implements mediaLoop entities retrieval operations.
 * @author tomaszpio@gmail.com
 */
public class MediaLoopDao {

    private Session session = null;

    public MediaLoopDao(Session session) {
        this.session = session;
    }
    
    public MediaLoop getMediaLoopById(long id){
       return (MediaLoop) session.load(MediaLoop.class, id);
    }
    
    @SuppressWarnings("unchecked")
    public List<MediaLoop> getMediaLoops() {
        Query getAllMediaLoopQuery = session.createQuery("from MediaLoop");
        return (List<MediaLoop>)getAllMediaLoopQuery.list();
    }
    
}
