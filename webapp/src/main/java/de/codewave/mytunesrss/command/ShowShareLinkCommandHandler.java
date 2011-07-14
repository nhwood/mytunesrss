/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShowShareLinkCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            if (getRequest().getParameter("rss") != null) {
                getRequest().setAttribute("rss", getRequest().getParameter("rss"));
            }
            if (getRequest().getParameter("playlist") != null) {
                getRequest().setAttribute("playlist", getRequest().getParameter("playlist"));
            }
            if (getRequest().getParameter("download") != null) {
                getRequest().setAttribute("download", getRequest().getParameter("download"));
            }
            if (getRequest().getParameter("jukebox") != null) {
                Map<String, String> jukeboxes = new LinkedHashMap<String, String>();
                for (FlashPlayerConfig flashPlayerConfig : MyTunesFunctions.flashPlayerConfigs()) {
                    jukeboxes.put(flashPlayerConfig.getName(), getRequest().getParameter("jukebox").replace("#ID#", flashPlayerConfig.getId()));
                }
                getRequest().setAttribute("jukeboxes", jukeboxes);
            }
            getRequest().setAttribute("twitterText", MessageFormat.format(MyTunesRssWebUtils.getBundleString(getRequest(), "twitter.template"), getRequest().getParameter("text")));
            getRequest().setAttribute("facebookText", MessageFormat.format(MyTunesRssWebUtils.getBundleString(getRequest(), "facebook.template"), getRequest().getParameter("text")));
            forward(MyTunesRssResource.ShareLink);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
