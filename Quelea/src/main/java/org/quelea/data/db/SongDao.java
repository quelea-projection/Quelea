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

    /**
     * Get all the songs, along with their themes, in a single query. This fails
     * as a whole if any song in the database is corrupt.
     *
     * @return all the songs.
     */
    @SuppressWarnings("unchecked")
    public List<Song> getSongs() {
        return session.createQuery("from Song s left join fetch s.theme t left join fetch t.textShadow").list();
    }

    /**
     * Get all the songs, loading each one separately when it's first accessed.
     * This is much slower than getSongs(), but a corrupt song only fails when
     * it's accessed, so the caller can skip it.
     *
     * @return all the songs.
     */
    @SuppressWarnings("unchecked")
    public List<Song> getSongsOneByOne() {
        Query getAllSongQuery = session.createQuery("from Song");
        Iterator<Song> iter = getAllSongQuery.iterate();
        List<Song> ret = new ArrayList<>();
        while (iter.hasNext()) {
            ret.add(iter.next());
        }
        return ret;
    }

}
