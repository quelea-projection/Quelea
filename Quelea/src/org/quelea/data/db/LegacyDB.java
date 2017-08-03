/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.data.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * The class used for loading the legacy database - used for importing any songs
 * into the new database format on the first run of the new version of Quelea.
 * <p/>
 * @author Michael
 */
public class LegacyDB {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final LegacyDB INSTANCE = new LegacyDB();
    private Connection conn;

    /**
     * Initialise the song database.
     */
    private LegacyDB() {
        try {
            LOGGER.log(Level.INFO, "Loading legacy database");
            Class.forName("org.hsqldb.jdbcDriver");
            String location = new File(new File(QueleaProperties.getQueleaUserHome(), "database"), "database").getAbsolutePath();
            conn = DriverManager.getConnection("jdbc:hsqldb:" + location, "", "");
            try(Statement stat = conn.createStatement()) {
                try {
                    stat.executeUpdate("CREATE TABLE Songs (id INTEGER IDENTITY,"
                            + "title varchar_ignorecase(256),"
                            + "author varchar_ignorecase(256),"
                            + "lyrics varchar_ignorecase(" + Integer.MAX_VALUE + "),"
                            + "background varchar(256),"
                            + ")");
                }
                catch(SQLException ex) { //Horrible but only way with hsqldb
                    LOGGER.log(Level.INFO, "Songs table already exists.");
                }
            }
            addColumns();
            LOGGER.log(Level.INFO, "Loaded database.");
        }
        catch(ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't find the legacy database library.", ex);
        }
        catch(SQLException ex) {
            LOGGER.log(Level.SEVERE, "Legacy SQL excpetion - hopefully this is just because Quelea is already running", ex);
        }
    }

    /**
     * Add any extra columns into the database that may not already be there.
     * This way we can silently upgrade the database if we need to add in new
     * columns later.
     */
    private void addColumns() {
        addColumn("ccli", "varchar_ignorecase(256)");
        addColumn("copyright", "varchar_ignorecase(256)");
        addColumn("year", "varchar_ignorecase(256)");
        addColumn("publisher", "varchar_ignorecase(256)");
        addColumn("tags", "varchar_ignorecase(256)");
        addColumn("key", "varchar_ignorecase(256)");
        addColumn("capo", "varchar_ignorecase(256)");
        addColumn("info", "varchar_ignorecase(20000)");
    }

    /**
     * Add a column into the database, silently fail since if it already exists
     * this means the database already contains the column.
     * <p/>
     * @param name the name of the column.
     * @param dataType the data type of the column.
     */
    private void addColumn(String name, String dataType) {
        String nameDataType = name + " " + dataType;
        try(Statement stat = conn.createStatement()) {
            stat.executeUpdate("ALTER TABLE Songs ADD COLUMN " + nameDataType);
            LOGGER.log(Level.INFO, "Added {0}", nameDataType);
        }
        catch(SQLException ex) {
            LOGGER.log(Level.INFO, "{0} already exists", nameDataType);
        }
    }

    /**
     * Get the singleton instance of this class.
     * <p/>
     * @return the singleton instance of this class.
     */
    public static LegacyDB get() {
        return INSTANCE;
    }

    /**
     * Run a select expression (query) that returns a result set.
     * <p/>
     * @param expression the select expression to run.
     * @return the result set returned from the SQL query.
     * @throws SQLException if the query fails for some reason.
     */
    private ResultSet runSelectExpression(String expression) throws SQLException {
        try(Statement stat = conn.createStatement()) {
            return stat.executeQuery(expression);
        }
    }

    /**
     * Get all the songs in the database.
     * <p/>
     * @return an array of all the songs in the database.
     */
    public SongDisplayable[] getSongs() {
        try(ResultSet rs = runSelectExpression("select * from songs")) {
            List<SongDisplayable> songs = new ArrayList<>();
            while(rs.next()) {
                String[] tagsArr = rs.getString("tags").split(" ");
                List<String> tags = new ArrayList<>(tagsArr.length);
                for(String str : tagsArr) {
                    tags.add(str);
                }
                ThemeDTO theme = ThemeDTO.fromString(rs.getString("background"));
                SongDisplayable song = new SongDisplayable.Builder(rs.getString("title"), rs.getString("author"))
                        .ccli(rs.getString("ccli"))
                        .year(rs.getString("year"))
                        .publisher(rs.getString("publisher"))
                        .copyright(rs.getString("copyright"))
                        .key(rs.getString("key"))
                        .capo(rs.getString("capo"))
                        .info(rs.getString("info"))
                        .tags(rs.getString("tags"))
                        .lyrics(rs.getString("lyrics"))
                        .theme(theme).get();
                songs.add(song);
            }
            return songs.toArray(new SongDisplayable[songs.size()]);
        }
        catch(SQLException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get the songs", ex);
            return null;
        }
    }
}
