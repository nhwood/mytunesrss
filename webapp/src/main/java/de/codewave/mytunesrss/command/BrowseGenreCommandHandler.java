/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.FindGenreQuery;
import de.codewave.mytunesrss.datastore.statement.Genre;
import de.codewave.mytunesrss.datastore.statement.PagerConfig;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseAlbumCommandHandler
 */
public class BrowseGenreCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException, SQLException {
        if (isSessionAuthorized()) {
            String page = getRequest().getParameter("page");
            getRequest().setAttribute("genrePager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            DataStoreQuery.QueryResult<Genre> queryResult;
            if (StringUtils.isNotEmpty(page)) {
                queryResult = getTransaction().executeQuery(new FindGenreQuery(getAuthUser(), Integer.parseInt(page)));
            } else {
                queryResult = getTransaction().executeQuery(new FindGenreQuery(getAuthUser(), -1));
            }
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Genre> genres;
            if (pageSize > 0 && queryResult.getResultSize() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(queryResult.getResultSize(), current);
                getRequest().setAttribute("indexPager", pager);
                genres = queryResult.getResults(current * pageSize, pageSize);
            } else {
                genres = queryResult.getResults();
            }
            getRequest().setAttribute("genres", genres);
            forward(MyTunesRssResource.BrowseGenre);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}