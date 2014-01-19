package org.quelea.data.db;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.quelea.data.db.model.Theme;

/**
 * Implements theme entities retrieval operations.
 * @author tomaszpio@gmail.com
 */
public class ThemeDao {
    private Session session = null;
    public ThemeDao(Session session) {
        this.session = session;
    }
    
    public Theme getThemeById(long id){
       return (Theme) session.load(Theme.class, id);
    }
    
    @SuppressWarnings("unchecked")
    public List<Theme> getThemes() {
        Query getAllTheme = session.createQuery("from Theme");
        return (List<Theme>)getAllTheme.list();
    }
}
