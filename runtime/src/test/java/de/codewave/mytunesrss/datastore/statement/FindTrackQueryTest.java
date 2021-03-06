package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTestUtils;
import de.codewave.mytunesrss.config.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class FindTrackQueryTest {

    private User user;

    @Before
    public void setUp() throws Exception {
        MyTunesRssTestUtils.before();
        user = new User("junit");
        MyTunesRss.CONFIG.addUser(user);
    }

    @After
    public void tearDown() {
        MyTunesRssTestUtils.after();
    }

    @Test
    public void testQueries() throws SQLException, IOException {
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        MyTunesRss.CACHE_DATA_PATH = MyTunesRssTestUtils.createTempDir().getAbsolutePath();
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        MyTunesRss.PREFERENCES_DATA_PATH = MyTunesRssTestUtils.createTempDir().getAbsolutePath();
        MyTunesRss.LUCENE_TRACK_SERVICE.deleteLuceneIndex();
        for (SortOrder sortOrder : SortOrder.values()) {
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getForAlbum(user, new String[]{"Moon Child"}, new String[]{"Metallica"}, sortOrder));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getForArtist(user, new String[]{"ABBA"}, sortOrder));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getForGenre(user, new String[]{"Pop"}, sortOrder));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getForSearchTerm(user, "lady gaga", 25, sortOrder, 100));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getMovies(user));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getTvShowEpisodes(user));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getTvShowSeriesEpisodes(user, "Once Upon a Time"));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getTvShowSeriesSeasonEpisodes(user, "Once Upon a Time", 1));
            MyTunesRss.STORE.executeQuery(FindTrackQuery.getForIds(new String[]{"1", "2", "3", "4"}));
        }
    }
}
