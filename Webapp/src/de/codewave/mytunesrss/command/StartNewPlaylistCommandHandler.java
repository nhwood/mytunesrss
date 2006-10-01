/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class StartNewPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getStates().put("addToPlaylistMode", Boolean.TRUE);
        getSession().setAttribute("playlist", new Playlist());
        getSession().setAttribute("playlistContent", new LinkedHashSet<Track>());
        String backUrl = getRequestParameter("backUrl", null);
        if (StringUtils.isNotEmpty(backUrl)) {
            redirect(backUrl);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}