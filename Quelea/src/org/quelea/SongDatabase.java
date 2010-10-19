package org.quelea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.quelea.display.Song;

/**
 * The class that controls the database that stores all the song data.
 * @author Michael
 */
public class SongDatabase {

    private Connection conn;

    /**
     * Initialise the song database.
     */
    public SongDatabase() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:database/quelea", "", "");
            Statement stat = conn.createStatement();
            try {
                stat.executeUpdate("CREATE TABLE Songs (id INTEGER IDENTITY,"
                        + "title varchar_ignorecase(256),"
                        + "author varchar_ignorecase(256),"
                        + "lyrics varchar_ignorecase(" + Integer.MAX_VALUE + "),"
                        + "background varchar(256))");
            }
            catch(SQLException ex) { //Horrible but only way with hsqldb
                System.out.println("Songs table already exists.");
            }
            stat.close();
        }
        catch(ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        catch(SQLException ex) {
            ex.printStackTrace();
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
        ResultSet ret = stat.executeQuery(expression);
        stat.close();
        return ret;
    }

    /**
     * Get all the songs in the database.
     * @return an array of all the songs in the database.
     */
    public Song[] getSongs() {
        try {
            final ResultSet rs = runSelectExpression("select * from songs");
            List<Song> songs = new ArrayList<Song>();
            while(rs.next()) {
                songs.add(new Song(rs.getString("title"), rs.getString("author")){
                    {
                        setLyrics(rs.getString("lyrics"));
                        setID(rs.getInt("id"));
                    }
                });
            }
            return songs.toArray(new Song[songs.size()]);
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Add a song to the database.
     * @param song the song to add.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean addSong(Song song) {
        try {
            PreparedStatement stat = conn.prepareStatement("insert into songs(title, author, lyrics) values(?, ?, ?)");
            stat.setString(1, song.getTitle());
            stat.setString(2, song.getAuthor());
            stat.setString(3, song.getLyrics());
            stat.executeUpdate();
            Statement stId = conn.createStatement();
            int id=-1;
            stId.execute("call IDENTITY()");
            ResultSet resultSet = stId.getResultSet();
            while(resultSet.next()) {
                id = resultSet.getInt(1);
            }
            song.setID(id);
            stat.close();
            return true;
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Update a song in the database.
     * @param song the song to update.
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean updateSong(Song song) {
        try {
            PreparedStatement stat = conn.prepareStatement("update songs set title=?, author=?, lyrics=? where id=?");
            stat.setString(1, song.getTitle());
            stat.setString(2, song.getAuthor());
            stat.setString(3, song.getLyrics());
            stat.setInt(4, song.getID());
            stat.executeUpdate();
            return true;
        }
        catch(SQLException ex) {
            ex.printStackTrace();
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
            stat.setInt(1, song.getID());
            stat.executeUpdate();
            return true;
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
