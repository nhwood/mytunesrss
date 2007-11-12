/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (isSessionAuthorized()) {
            String album = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("album"));
            if (StringUtils.isEmpty(album)) {
                album = null;
            }
            String genre = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("genre"));
            if (StringUtils.isEmpty(genre)) {
                genre = null;
            }
            getRequest().setAttribute("artistPager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            FindArtistQuery findArtistQuery = new FindArtistQuery(getAuthUser(),
                                                                  getDisplayFilter().getTextFilter(),
                                                                  album,
                                                                  genre,
                                                                  getIntegerRequestParameter("page", -1));
            DataStoreQuery.QueryResult<Artist> queryResult = getTransaction().executeQuery(findArtistQuery);
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Artist> artists;
            if (pageSize > 0 && queryResult.getResultSize() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(queryResult.getResultSize(), current);
                getRequest().setAttribute("indexPager", pager);
                artists = queryResult.getResults(current * pageSize, pageSize);
            } else {
                artists = queryResult.getResults();
            }
            getRequest().setAttribute("artists", artists);
            forward(MyTunesRssResource.BrowseArtist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}