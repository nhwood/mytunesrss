/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {
    public static final String PSEUDO_ID_ALL_BY_ARTIST = "PlaylistAllByArtist";
    public static final String PSEUDO_ID_ALL_BY_ALBUM = "PlaylistAllByAlbum";
    public static final String PSEUDO_ID_RANDOM = "PlaylistRandom";
    public static final String PSEUDO_ID_MOST_PLAYED = "PlaylistMostPlayed";
    public static final String PSEUDO_ID_LAST_UPDATED = "PlaylistLastUpdated";

    public static enum SortOrder {
        Album(), Artist()
    }

    private String myId;
    private SortOrder mySortOrder;
    private String myRestrictionPlaylistId;

    public FindPlaylistTracksQuery(String id, SortOrder sortOrder) {
        myId = id;
        mySortOrder = sortOrder;
    }

    public FindPlaylistTracksQuery(User user, String id, SortOrder sortOrder) {
        this(id, sortOrder);
        myRestrictionPlaylistId = user.getPlaylistId();
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        String suffix = StringUtils.isEmpty(myRestrictionPlaylistId) || myRestrictionPlaylistId.equals(myId) ? "" : "Restricted";
        if (PSEUDO_ID_ALL_BY_ALBUM.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracksOrderedByAlbum" + suffix);
            myId = null;
        } else if (PSEUDO_ID_ALL_BY_ARTIST.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracksOrderedByArtist" + suffix);
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_LAST_UPDATED)) {
            statement = MyTunesRssUtils.createStatement(connection, "findLastUpdatedTracks" + suffix);
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_MOST_PLAYED)) {
            statement = MyTunesRssUtils.createStatement(connection, "findMostPlayedTracks" + suffix);
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_RANDOM)) {
            statement = MyTunesRssUtils.createStatement(connection, "findRandomTracks" + suffix);
            String[] splitted = myId.split("_", 4);
            statement.setBoolean("audio", splitted[1].contains("a"));
            statement.setBoolean("video", splitted[1].contains("v"));
            statement.setBoolean("protected", splitted[1].contains("p"));
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
        } else if (mySortOrder == SortOrder.Album) {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByAlbum" + suffix);
        } else if (mySortOrder == SortOrder.Artist) {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByArtist" + suffix);
        } else {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByIndex" + suffix);
        }
        if (myId != null) {
            String[] parts = StringUtils.split(myId, "@");
            statement.setString("id", parts[0]);
            if (parts.length == 3) {
                statement.setInt("firstIndex", Integer.parseInt(parts[1]));
                statement.setInt("lastIndex", Integer.parseInt(parts[2]));
            }
        }
        statement.setString("restrictedPlaylistId", myRestrictionPlaylistId);
        return execute(statement, new TrackResultBuilder());
    }

    public static class TrackResultBuilder implements ResultBuilder<Track> {
        private TrackResultBuilder() {
            // intentionally left blank
        }

        public Track create(ResultSet resultSet) throws SQLException {
            Track track = new Track();
            track.setId(resultSet.getString("ID"));
            track.setName(resultSet.getString("NAME"));
            track.setArtist(resultSet.getString("ARTIST"));
            track.setOriginalArtist(resultSet.getString("ORIGINAL_ARTIST"));
            track.setAlbum(resultSet.getString("ALBUM"));
            track.setTime(resultSet.getInt("TIME"));
            track.setTrackNumber(resultSet.getInt("TRACK_NUMBER"));
            track.setFile(new File(resultSet.getString("FILE")));
            track.setProtected(resultSet.getBoolean("PROTECTED"));
            track.setVideo(resultSet.getBoolean("VIDEO"));
            track.setGenre(resultSet.getString("GENRE"));
            track.setMp4Codec(resultSet.getString("MP4CODEC"));
            track.setTsPlayed(resultSet.getLong("TS_PLAYED"));
            track.setTsUpdated(resultSet.getLong("TS_UPDATED"));
            track.setLastImageUpdate(resultSet.getLong("LAST_IMAGE_UPDATE"));
            track.setPlayCount(resultSet.getLong("PLAYCOUNT"));
            track.setImageCount(resultSet.getInt("IMAGECOUNT"));
            track.setComment(resultSet.getString("COMMENT"));
            track.setPosNumber(resultSet.getInt("POS_NUMBER"));
            track.setPosSize(resultSet.getInt("POS_SIZE"));
            return track;
        }
    }
}