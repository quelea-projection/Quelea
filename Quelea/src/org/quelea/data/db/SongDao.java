package org.quelea.data.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.quelea.data.db.model.Song;

/**
 * Implements song entities retrieval operations.
 *
 * @author tomaszpio@gmail.com
 */
public class SongDao {

    private Session session = null;

    public SongDao(Session session) {
        this.session = session;
    }

    public Song getSongById(long id) {
        return (Song) session.load(Song.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Song> getSongs() {
        Query getAllSongQuery = session.createQuery("from Song");
        /*
         * We use an iterator here (rather then just doing getAllSongQuery.list()
         * because the latter will fall over if there are any corrupt songs in 
         * the database. This will force it to go through, and we can remove
         * the corrupt individual songs manually later.
         */
        Iterator<Song> iter = getAllSongQuery.iterate();
        List<Song> ret = new ArrayList<>();
        while (iter.hasNext()) {
            ret.add(iter.next());
        }
        return ret;
    }

}
