package org.quelea;

import java.io.File;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextSection;
import org.quelea.utils.DatabaseListener;
import org.quelea.utils.LoggerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.QueleaProperties;

/**
 * The class that controls the database.
 * @author Michael
 */
public final class SongDatabase {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final SongDatabase INSTANCE = new SongDatabase();
    private Connection conn;
    private boolean error;
    private final Set<DatabaseListener> listeners;

    /**
     * Initialise the song database.
     */
    private SongDatabase() {
        listeners = new HashSet<DatabaseListener>();
        try {
            LOGGER.log(Level.INFO, "Loading database");
            Class.forName("org.hsqldb.jdbcDriver");
            String location = new File(QueleaProperties.getQueleaUserHome(), "database").getAbsolutePath();
            conn = DriverManager.getConnection("jdbc:hsqldb:"+location, "", "");
            Statement stat = conn.createStatement();
            try {
                stat.executeUpdate("CREATE TABLE Songs (id INTEGER IDENTITY,"
                        + "title varchar_ignorecase(256),"
                        + "author varchar_ignorecase(256),"
                        + "lyrics varchar_ignorecase(" + Integer.MAX_VALUE + "),"
                        + "background varchar(256))");
            }
            catch(SQLException ex) { //Horrible but only way with hsqldb
                LOGGER.log(Level.INFO, "Songs table already exists.");
            }
            stat.close();
            LOGGER.log(Level.INFO, "Loaded database.");
        }
        catch(ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't find the database library.", ex);
            error = true;
        }
        catch(SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQL excpetion - hopefully this is just because quelea is already running", ex);
            error = true;
        }
    }

    /**
     * Get the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static SongDatabase get() {
        return INSTANCE;
    }

    /**
     * Determine if an error occurred initialising the database.
     * @return true if an error occurred, false if all is ok.
     */
    public boolean errorOccurred() {
        return error;
    }

    /**
     * Register a database listener with this database.
     * @param listener the listener.
     */
    public void registerDatabaseListener(DatabaseListener listener) {
        listeners.add(listener);
    }

    /**
     * Fire off the database listeners.
     */
    public void fireUpdate() {
        for(DatabaseListener listener : listeners) {
            listener.update();
        }
    }

    /**
     * Run a select expression (query) that returns a result set.
     * @param expression the select expression to run.
     * @return the result set returned from the SQL query.
     * @throws SQLException if the query fails for some reason.
     */
    private ResultSet runSelectExpression(String expression) throws SQLException {
        Statement stat = conn.createStatement();
        try {
            return stat.executeQuery(expression);
        }
        finally {
            stat.close();
        }
    }

    /**
     * Get all the songs in the database.
     * @return an array of all the songs in the database.
     */
    public Song[] getSongs() {
        try {
            final ResultSet rs = runSelectExpression("select * from songs");
            try {
                List<Song> songs = new ArrayList<Song>();
                while(rs.next()) {
                    songs.add(new Song(rs.getString("title"), rs.getString("author")) {

                        {
                            setLyrics(rs.getString("lyrics"));
                            setID(rs.getInt("id"));
                            for(TextSection section : getSections()) {
                                section.setTheme(Theme.parseDBString(rs.getString("background")));
                            }
                        }
                    });
                }
                return songs.toArray(new Song[songs.size()]);
            }
            finally {
                rs.close();
            }
        }
        catch(SQLException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get the songs", ex);
            return null;
        }
    }

    /**
     * Add a song to the database.
     * @param song       the song to add.
     * @param fireUpdate true if the update should be fired to listeners when adding this song, false otherwise.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean addSong(Song song, boolean fireUpdate) {
        try {
            PreparedStatement stat = conn.prepareStatement("insert into songs(title, author, lyrics, background) values(?, ?, ?, ?)");
            try {
                stat.setString(1, song.getTitle());
                stat.setString(2, song.getAuthor());
                stat.setString(3, song.getLyrics());
                String theme = "";
                if(song.getSections().length > 0 && song.getSections()[0].getTheme() != null) {
                    theme = song.getSections()[0].getTheme().toDBString();
                }
                stat.setString(4, theme);
                stat.executeUpdate();
                int id = -1;
                Statement stId = conn.createStatement();
                stId.execute("call IDENTITY()");
                ResultSet resultSet = stId.getResultSet();
                stId.close();
                try {
                    while(resultSet.next()) {
                        id = resultSet.getInt(1);
                    }
                }
                finally {
                    resultSet.close();
                }
                song.setID(id);
                return true;
            }
            finally {
                stat.close();
            }
        }
        catch(SQLException ex) {
            LOGGER.log(Level.WARNING, "SQL exception occured adding the song: " + song, ex);
            return false;
        }
        finally {
            if(fireUpdate) {
                fireUpdate();
            }
        }
    }

    /**
     * Update a song in the database.
     * @param song the song to update.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean updateSong(Song song) {
        try {
            if(song.getID() == -1) {
                LOGGER.log(Level.INFO, "Updating song that doesn't exist, adding instead");
                return addSong(song, true);
            }
            else {
                LOGGER.log(Level.INFO, "Updating song");
                PreparedStatement stat = conn.prepareStatement("update songs set title=?, author=?, lyrics=?, background=? where id=?");
                try {
                    stat.setString(1, song.getTitle());
                    stat.setString(2, song.getAuthor());
                    stat.setString(3, song.getLyrics());
                    String theme = "";
                    if(song.getSections().length > 0 && song.getSections()[0].getTheme() != null) {
                        theme = song.getSections()[0].getTheme().toDBString();
                    }
                    stat.setString(4, theme);
                    stat.setInt(5, song.getID());
                    stat.executeUpdate();
                    return true;
                }
                finally {
                    stat.close();
                    fireUpdate();
                }
            }
        }
        catch(SQLException ex) {
            LOGGER.log(Level.WARNING, "SQL exception occured updating the song: " + song, ex);
            return false;
        }
    }

    /**
     * Remove a song from the database.
     * @param song the song to remove.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean removeSong(Song song) {
        try {
            PreparedStatement stat = conn.prepareStatement("delete from songs where id=?");
            try {
                stat.setInt(1, song.getID());
                stat.executeUpdate();
                return true;
            }
            finally {
                stat.close();
                fireUpdate();
            }
        }
        catch(SQLException ex) {
            LOGGER.log(Level.WARNING, "SQL exception occured removing the song: " + song, ex);
            return false;
        }
    }
}
