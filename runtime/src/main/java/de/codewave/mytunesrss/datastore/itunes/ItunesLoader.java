/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.utils.sql.*;
import de.codewave.utils.xml.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.ItunesLoaderr
 */
public class ItunesLoader {
    private static final Log LOG = LogFactory.getLog(ItunesLoader.class);

    static File getFileForLocation(String location) {
        try {
            return new File(new URI(location).getPath());
        } catch (URISyntaxException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create URI from location \"" + location + "\".", e);
            }
        }
        return null;
    }

    public static void loadFromITunes(URL iTunesLibraryXml, DataStoreSession storeSession, long timeLastUpdate, Collection<String> existsingPlaylistIds)
            throws SQLException {
        TrackListener trackListener = null;
        PlaylistListener playlistListener = null;
        if (iTunesLibraryXml != null) {
            PListHandler handler = new PListHandler();
            Map<Long, String> trackIdToPersId = new HashMap<Long, String>();
            LibraryListener libraryListener = new LibraryListener(timeLastUpdate);
            trackListener = new TrackListener(storeSession, libraryListener, trackIdToPersId);
            playlistListener = new PlaylistListener(storeSession, libraryListener, trackIdToPersId);
            handler.addListener("/plist/dict", libraryListener);
            handler.addListener("/plist/dict[Tracks]/dict", trackListener);
            handler.addListener("/plist/dict[Playlists]/array", playlistListener);
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Parsing iTunes: \"" + iTunesLibraryXml.toString() + "\".");
                }
                XmlUtils.parseApplePList(iTunesLibraryXml, handler);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not read data from iTunes xml file.", e);
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Inserted/updated " + trackListener.getUpdatedCount() + " iTunes tracks.");
            }
            trackListener.commitLastSeen();
            existsingPlaylistIds.removeAll(playlistListener.getExistingIds());
        }
    }
}