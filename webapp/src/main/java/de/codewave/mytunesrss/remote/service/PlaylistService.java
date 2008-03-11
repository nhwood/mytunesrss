package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * Service for playlist retrieval and management.
 */
public class PlaylistService {
    /**
     * Get all visible playlist from the database.
     *
     * @return All visible playlists.
     *
     * @throws SQLException
     */
    public Object getPlaylists() throws SQLException {
        User user = MyTunesRssRemoteEnv.getUser();
        if (user != null) {
        FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, false, false);
        return MyTunesRssRemoteEnv.getRenderMachine().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        return null;
    }

    /**
     * Get all visible playlists owned by the authorized user.
     *
     * @return All visible playlists owned by the authorized user.
     *
     * @throws SQLException
     */
    public Object getOwnPlaylists() throws SQLException {
        User user = MyTunesRssRemoteEnv.getUser();
        if (user != null) {
        FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, false, true);
        return MyTunesRssRemoteEnv.getRenderMachine().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        return null;
    }

    /**
     * Get an URL for retrieving the playlist with the specified ID.
     *
     * @param playlistId ID of the playlist.
     * @param type       Playlist type (M3u or Xspf).
     *
     * @return The URL for the specified playlist.
     */
    public String getPlaylistUrl(String playlistId, String type) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, "playlist=" + playlistId + "/type=" + StringUtils.capitalize(
                type.toLowerCase()));
    }

    /**
     * Get an URL for retrieving an RSS feed for the playlist with the specified ID.
     *
     * @param playlistId ID of the playlist.
     *
     * @return The URL for the RSS feed.
     */
    public String getRssUrl(String playlistId) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreateRss, "playlist=" + playlistId);
    }
}
