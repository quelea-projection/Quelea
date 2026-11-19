package org.quelea.services.importexport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("OpenLP database import")
public class OpenLPParserTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-openlp-test").toString());
        }
    }

    @Test
    public void importsValidSongsAndSkipsMalformedLyricsAndEmptyTitles() throws Exception {
        Path db = directory.resolve("songs.sqlite");
        try (Connection connection = createDatabase(db)) {
            insert(connection, "Grace", "<song><lyrics><verse>Amazing grace</verse><verse>Sing with joy</verse></lyrics></song>");
            insert(connection, "Broken", "<song>");
            insert(connection, "", "<song><lyrics><verse>Untitled</verse></lyrics></song>");
        }
        List<SongDisplayable> songs = new OpenLPParser().getSongs(db.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        SongDisplayable song = songs.get(0);
        Assertions.assertEquals("Grace", song.getTitle());
        Assertions.assertEquals("Amazing grace\n\nSing with joy", song.getLyrics(false, false, false));
        Assertions.assertEquals("123", song.getCcli());
        Assertions.assertEquals("Notes", song.getInfo());
        Assertions.assertEquals("2020", song.getCopyright());
        Assertions.assertTrue(Files.exists(Files.move(db, directory.resolve("renamed.sqlite"))));
    }

    @Test
    public void skipsRowsWithNullLyricsAndContinuesImport() throws Exception {
        Path db = directory.resolve("nulls.sqlite");
        try (Connection connection = createDatabase(db)) {
            insert(connection, "Missing lyrics", null);
            insert(connection, "Good", "<song><lyrics><verse>Words</verse></lyrics></song>");
        }
        List<SongDisplayable> songs = new OpenLPParser().getSongs(db.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Good", songs.get(0).getTitle());
    }

    @Test
    public void skipsRowsWithUnexpectedOrEmptyXmlRoots() throws Exception {
        Path db = directory.resolve("roots.sqlite");
        try (Connection connection = createDatabase(db)) {
            insert(connection, "Wrong root", "<other/>");
            insert(connection, "Empty song", "<song/>");
            insert(connection, "Good", "<song><lyrics><verse>Words</verse></lyrics></song>");
        }
        List<SongDisplayable> songs = new OpenLPParser().getSongs(db.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Good", songs.get(0).getTitle());
    }

    @Test
    public void importsPrettyPrintedXml() throws Exception {
        Path db = directory.resolve("pretty.sqlite");
        try (Connection connection = createDatabase(db)) {
            insert(connection, "Pretty", "<song>\n  <metadata>Ignore</metadata>\n  <lyrics>\n"
                    + "    <verse>First &amp; last</verse>\n    <!-- comment -->\n"
                    + "    <verse>Second verse</verse>\n  </lyrics>\n</song>");
        }
        List<SongDisplayable> songs = new OpenLPParser().getSongs(db.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("First & last\n\nSecond verse", songs.get(0).getLyrics(false, false, false));
    }

    private Connection createDatabase(Path path) throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE songs (title TEXT, lyrics TEXT, ccli_number TEXT, comments TEXT, copyright TEXT)");
        }
        return connection;
    }

    private void insert(Connection connection, String title, String lyrics) throws Exception {
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO songs VALUES (?, ?, ?, ?, ?)")) {
            insert.setString(1, title);
            insert.setString(2, lyrics);
            insert.setString(3, " 123 ");
            insert.setString(4, " Notes ");
            insert.setString(5, " 2020 ");
            insert.executeUpdate();
        }
    }
}
