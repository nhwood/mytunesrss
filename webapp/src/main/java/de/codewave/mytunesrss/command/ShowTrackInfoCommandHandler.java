/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.command.ShowTrackInfoCommandHandler
 */
public class ShowTrackInfoCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowTrackInfoCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        String trackId = getRequest().getParameter("track");
        Collection<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId})).getResults();
        if (!tracks.isEmpty()) {
            Track track = tracks.iterator().next();
            getRequest().setAttribute("track", track);
            if (FileSupportUtils.isMp3(track.getFile())) {
                try (FileInputStream inputStream = new FileInputStream(track.getFile())) {
                    getRequest().setAttribute("mp3info", Mp3Utils.getMp3Info(inputStream));
                } catch (Exception e) {
                    LOGGER.info("Could not get MP3 info from track.", e);
                }
            }
            TranscoderConfig forcedTranscoder = getAuthUser().getForceTranscoder(track);
            TranscoderConfig selectedTranscoder = MyTunesRssUtils.getTranscoder(getWebConfig().getActiveTranscoders(), track);
            getRequest().setAttribute("originalDownloadLink", forcedTranscoder == null && selectedTranscoder != null);
        }
        forward(MyTunesRssResource.TrackInfo);
    }
}