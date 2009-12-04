/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.MediaType;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {
    public static final String PSEUDO_ID_ALL_BY_ARTIST = "PlaylistAllByArtist";
    public static final String PSEUDO_ID_ALL_BY_ALBUM = "PlaylistAllByAlbum";
    public static final String PSEUDO_ID_RANDOM = "PlaylistRandom";
    public static final String PSEUDO_ID_MOST_PLAYED = "PlaylistMostPlayed";
    public static final String PSEUDO_ID_LAST_UPDATED = "PlaylistLastUpdated";

    private String myId;
    private SortOrder mySortOrder;
    private List<String> myRestrictionPlaylistIds = Collections.emptyList();

    public FindPlaylistTracksQuery(String id, SortOrder sortOrder) {
        myId = id;
        mySortOrder = sortOrder;
    }

    public FindPlaylistTracksQuery(User user, String id, SortOrder sortOrder) {
        this(id, sortOrder);
        myRestrictionPlaylistIds = user.getPlaylistIds();
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("restricted", !myRestrictionPlaylistIds.isEmpty() && (myRestrictionPlaylistIds.size() > 1 || !myRestrictionPlaylistIds.get(0).equals(myId)));
        if (PSEUDO_ID_ALL_BY_ALBUM.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracks", conditionals);
            conditionals.put("albumorder", Boolean.TRUE);
            myId = null;
        } else if (PSEUDO_ID_ALL_BY_ARTIST.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracks", conditionals);
            conditionals.put("artistorder", Boolean.TRUE);
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_LAST_UPDATED)) {
            statement = MyTunesRssUtils.createStatement(connection, "findLastUpdatedTracks", conditionals);
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_MOST_PLAYED)) {
            statement = MyTunesRssUtils.createStatement(connection, "findMostPlayedTracks", conditionals);
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_RANDOM)) {
            statement = MyTunesRssUtils.createStatement(connection, "findRandomTracks", conditionals);
            String[] splitted = myId.split("_", 4);
            statement.setString("mediatype", StringUtils.trimToNull(splitted[1].split("-", 2)[0]));
            statement.setBoolean("protected", "p".equals(splitted[1].split("-", 2)[1]));
            statement.setInt("maxCount", Integer.parseInt(splitted[2]));
            if (splitted.length > 3) {
                String sourceId = splitted[3];
                QueryResult<Playlist> playlists = new FindPlaylistQuery(null, sourceId, null, false).execute(connection);
                if (playlists.getResultSize() == 1) {
                    Playlist playlist = playlists.nextResult();
                    statement.setString("sourcePlaylistId", StringUtils.trimToNull(playlist.getId()));
                }
            }
            myId = null;
        } else {
            conditionals.put("indexorder", mySortOrder != SortOrder.Album && mySortOrder != SortOrder.Artist);
            conditionals.put("albumorder", mySortOrder == SortOrder.Album);
            conditionals.put("artistorder", mySortOrder == SortOrder.Artist);
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracks", conditionals);
        }
        if (myId != null) {
            String[] parts = StringUtils.split(myId, "@");
            statement.setString("id", parts[0]);
            if (parts.length == 3) {
                statement.setInt("firstIndex", Integer.parseInt(parts[1]));
                statement.setInt("lastIndex", Integer.parseInt(parts[2]));
                conditionals.put("index", Boolean.TRUE);
            }
        }
        statement.setItems("restrictedPlaylistIds", myRestrictionPlaylistIds);
        return execute(statement, new TrackResultBuilder());
    }
}