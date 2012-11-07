package org.quelea.db;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.quelea.db.model.Song;
/**
 * Implements song entities retrieval operations.
 * @author tomaszpio@gmail.com
 */
public class SongDao {

    private Session session = null;

    public SongDao(Session session) {
        this.session = session;
    }
    
    public Song getSongById(long id){
       return (Song) session.load(Song.class, id);
    }
    
    public List<Song> getSongs() {
        Query getAllSongQuery = session.createQuery("from Song");
        return getAllSongQuery.list();
    }
    
}
