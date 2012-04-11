package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.SaveSmartPlaylistCommandHandler
 */
public class SaveSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (StringUtils.isBlank(getRequestParameter("smartPlaylist.playlist.name", null))) {
            addError(new BundleError("error.needPlaylistNameForSave"));
        }
        if (!isError()) {
            Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
            Enumeration<String> namesEnum = getRequest().getParameterNames();
            while (namesEnum.hasMoreElements()) {
                String name = namesEnum.nextElement();
                if (name.startsWith("type_")) {
                    String suffix = name.substring(5);
                    SmartFieldType type = SmartFieldType.valueOf(getRequestParameter("type_" + suffix, null));
                    String pattern = getRequestParameter("pattern_" + suffix, null);
                    boolean invert = getBooleanRequestParameter("invert_" + suffix, false);
                    if (StringUtils.isNotBlank(pattern)) {
                        smartInfos.add(new SmartInfo(type, pattern, invert));
                    }
                }
            }
            SaveMyTunesSmartPlaylistStatement statement = new SaveMyTunesSmartPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter("smartPlaylist.playlist.userPrivate", false), smartInfos);
            statement.setId(getRequestParameter("smartPlaylist.playlist.id", null));
            statement.setName(getRequestParameter("smartPlaylist.playlist.name", null));
            statement.setTrackIds(Collections.<String>emptyList());
            getTransaction().executeStatement(statement);
            getTransaction().executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, statement.getPlaylistIdAfterExecute()));
            forward(MyTunesRssCommand.ShowPlaylistManager);
        } else {
            getRequest().setAttribute("smartPlaylist", createRedisplayModel(null));
            forward(MyTunesRssResource.EditSmartPlaylist);
        }
    }

    protected Map<String, Object> createRedisplayModel(String remove) throws IOException, ServletException {
        Map<String, Object> playlistModel = new HashMap<String, Object>();
        playlistModel.put("name", getRequestParameter("smartPlaylist.playlist.name", null));
        playlistModel.put("id", getRequestParameter("smartPlaylist.playlist.id", null));
        playlistModel.put("userPrivate", getRequestParameter("smartPlaylist.playlist.userPrivate", null));
        List<Map<String, String>> smartInfos = new ArrayList<Map<String, String>>();
        Enumeration<String> namesEnum = getRequest().getParameterNames();
        while (namesEnum.hasMoreElements()) {
            String name = namesEnum.nextElement();
            if (name.startsWith("type_")) {
                String suffix = name.substring(5);
                if (!suffix.equals(remove)) {
                    Map<String, String> smartInfo = new HashMap<String, String>();
                    smartInfo.put("fieldType", getRequestParameter("type_" + suffix, ""));
                    smartInfo.put("pattern", getRequestParameter("pattern_" + suffix, ""));
                    smartInfo.put("invert", getRequestParameter("invert_" + suffix, ""));
                    smartInfos.add(smartInfo);
                }
            }
        }
        Map<String, Object> smartPlaylistModel = new HashMap<String, Object>();
        smartPlaylistModel.put("smartInfos", smartInfos);
        smartPlaylistModel.put("playlist", playlistModel);
        return smartPlaylistModel;
    }
}