/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.sql.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.ShowPlaylistManagerCommandHandler
 */
public class ShowPlaylistManagerCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        DataStoreQuery.QueryResult<Playlist> queryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), PlaylistType.MyTunes, null, false, true));
        int pageSize = getWebConfig().getEffectivePageSize();
        List<Playlist> playlists;
        if (pageSize > 0 && queryResult.getResultSize() > pageSize) {
            int current = getSafeIntegerRequestParameter("index", 0);
            Pager pager = createPager(queryResult.getResultSize(), current);
            getRequest().setAttribute("pager", pager);
            playlists = queryResult.getResults(current * pageSize, pageSize);
        } else {
            playlists = queryResult.getResults();
        }
        getRequest().setAttribute("playlists", playlists);
        forward(MyTunesRssResource.PlaylistManager);
    }
}