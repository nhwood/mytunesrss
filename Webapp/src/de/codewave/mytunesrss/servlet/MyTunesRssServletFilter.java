/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.utils.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssServletFilter
 */
public class MyTunesRssServletFilter implements Filter {
    private static final Log LOG = LogFactory.getLog(MyTunesRssServletFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MyTunesRSS servlet filter invoked.");
        }
        HttpSession session = ((HttpServletRequest)servletRequest).getSession();
        if (session.getAttribute("urlMap") == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating servlet URL map in session.");
            }
            Map<String, String> servletUrls = new HashMap<String, String>();
            servletUrls.put("search", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SearchServlet.class));
            servletUrls.put("sort", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SortServlet.class));
            servletUrls.put("rss", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, RSSFeedServlet.class));
            servletUrls.put("select", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SelectServlet.class));
            servletUrls.put("login", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, LoginServlet.class));
            servletUrls.put("playlist", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, PlayListFeedServlet.class));
            servletUrls.put("mp3", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, MusicDeliveryServlet.class));
            servletUrls.put("searchPage", ServletUtils.getApplicationUrl((HttpServletRequest)servletRequest) + "/search.jsp");
            session.setAttribute("urlMap", servletUrls);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }
}