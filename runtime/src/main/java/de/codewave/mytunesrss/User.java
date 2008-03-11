package de.codewave.mytunesrss;

import de.codewave.utils.servlet.RangeHeader;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * de.codewave.mytunesrss.User
 */
public class User {
    private static final Log LOG = LogFactory.getLog(User.class);

    public enum QuotaType {
        None, Day, Week, Month;

        @Override
        public String toString() {
            return MyTunesRssUtils.getBundleString("editUser.quotaType." + name());
        }
    }

    private boolean myActive = true;
    private String myName;
    private byte[] myPasswordHash;
    private boolean myDownload;
    private boolean myRss;
    private boolean myPlaylist;
    private boolean myUpload;
    private boolean myPlayer;
    private boolean myChangePassword;
    private QuotaType myQuotaType;
    private long myDownBytes;
    private long myQuotaDownBytes;
    private long myBytesQuota;
    private long myResetTime;
    private long myQuotaResetTime;
    private int myMaximumZipEntries;
    private String myFileTypes;
    private int mySessionTimeout = 10;
    private boolean mySpecialPlaylists;
    private boolean myTranscoder;
    private int myBandwidthLimit;
    private String myPlaylistId;
    private boolean mySaveWebSettings;
    private String myWebSettings;
    private boolean myCreatePlaylists;
    private boolean myEditWebSettings;

    public User(String name) {
        myName = name;
        myResetTime = System.currentTimeMillis();
        myQuotaResetTime = myResetTime;
        myQuotaType = QuotaType.None;
    }

    public boolean isActive() {
        return myActive;
    }

    public void setActive(boolean active) {
        myActive = active;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public byte[] getPasswordHash() {
        return myPasswordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        myPasswordHash = passwordHash;
    }

    public boolean isDownload() {
        return myDownload;
    }

    public void setDownload(boolean download) {
        myDownload = download;
    }

    public boolean isPlaylist() {
        return myPlaylist;
    }

    public void setPlaylist(boolean playlist) {
        myPlaylist = playlist;
    }

    public boolean isRss() {
        return myRss;
    }

    public void setRss(boolean rss) {
        myRss = rss;
    }

    public boolean isUpload() {
        return MyTunesRss.REGISTRATION.isRegistered() && myUpload;
    }

    public void setUpload(boolean upload) {
        myUpload = upload;
    }

    public boolean isPlayer() {
        return MyTunesRss.REGISTRATION.isRegistered() && myPlayer;
    }

    public void setPlayer(boolean player) {
        myPlayer = player;
    }

    public boolean isChangePassword() {
        return MyTunesRss.REGISTRATION.isRegistered() && myChangePassword;
    }

    public void setChangePassword(boolean changePassword) {
        myChangePassword = changePassword;
    }

    public long getBytesQuota() {
        return myBytesQuota;
    }

    public void setBytesQuota(long bytesQuota) {
        myBytesQuota = bytesQuota;
    }

    public long getDownBytes() {
        return myDownBytes;
    }

    public void setDownBytes(long downBytes) {
        myDownBytes = downBytes;
    }

    public long getQuotaDownBytes() {
        return myQuotaDownBytes;
    }

    public void setQuotaDownBytes(long quotaDownBytes) {
        myQuotaDownBytes = quotaDownBytes;
    }

    public long getQuotaResetTime() {
        return myQuotaResetTime;
    }

    public void setQuotaResetTime(long quotaResetTime) {
        myQuotaResetTime = quotaResetTime;
    }

    public QuotaType getQuotaType() {
        return MyTunesRss.REGISTRATION.isRegistered() ? (myQuotaType != null ? myQuotaType : QuotaType.None) : QuotaType.None;
    }

    public void setQuotaType(QuotaType quotaType) {
        myQuotaType = quotaType;
    }

    public boolean isQuota() {
        return myQuotaType != null && myQuotaType != QuotaType.None;
    }

    public long getResetTime() {
        return myResetTime;
    }

    public void setResetTime(long resetTime) {
        myResetTime = resetTime;
    }

    public int getMaximumZipEntries() {
        return myMaximumZipEntries;
    }

    public void setMaximumZipEntries(int maximumZipEntries) {
        myMaximumZipEntries = maximumZipEntries;
    }

    public String getFileTypes() {
        return myFileTypes;
    }

    public void setFileTypes(String fileTypes) {
        myFileTypes = fileTypes;
    }

    public int getSessionTimeout() {
        return mySessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        mySessionTimeout = sessionTimeout;
    }

    public boolean isSpecialPlaylists() {
        return mySpecialPlaylists;
    }

    public void setSpecialPlaylists(boolean specialPlaylists) {
        mySpecialPlaylists = specialPlaylists;
    }

    public boolean isTranscoder() {
        return myTranscoder;
    }

    public void setTranscoder(boolean transcoder) {
        myTranscoder = transcoder;
    }

    public int getBandwidthLimit() {
        return myBandwidthLimit;
    }

    public void setBandwidthLimit(int bandwidthLimit) {
        myBandwidthLimit = bandwidthLimit;
    }

    public String getPlaylistId() {
        return myPlaylistId;
    }

    public void setPlaylistId(String playlistId) {
        myPlaylistId = playlistId;
    }

    public boolean isSaveWebSettings() {
        return mySaveWebSettings;
    }

    public void setSaveWebSettings(boolean saveWebSettings) {
        mySaveWebSettings = saveWebSettings;
    }

    public boolean isCreatePlaylists() {
        return myCreatePlaylists;
    }

    public void setCreatePlaylists(boolean createPlaylists) {
        myCreatePlaylists = createPlaylists;
    }

    public boolean isEditWebSettings() {
        return myEditWebSettings;
    }

    public void setEditWebSettings(boolean editWebSettings) {
        myEditWebSettings = editWebSettings;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().equals(((User)object).getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public boolean isQuotaExceeded() {
        if (myQuotaType != null && myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return myBytesQuota > 0 && myQuotaDownBytes >= myBytesQuota;
        }
        return false;
    }

    public long getQuotaRemaining() {
        if (myQuotaType != null && myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return Math.max(myBytesQuota - myQuotaDownBytes, 0);
        }
        return 0;
    }

    private void checkQuotaReset() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(myQuotaResetTime));
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        switch (myQuotaType) {
            case Month:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, 1);
                break;
            case Week:
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case Day:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            myQuotaDownBytes = 0;
            myQuotaResetTime = System.currentTimeMillis();
        }
    }

    public StreamSender.OutputStreamWrapper getOutputStreamWrapper(final int bitrate) {
        return getOutputStreamWrapper(bitrate, 0, null);
    }

    public StreamSender.OutputStreamWrapper getOutputStreamWrapper(int bitrate, int dataOffset, RangeHeader rangeHeader) {
        final int limit = Math.min(bitrate, getBandwidthLimit() > 0 ? getBandwidthLimit() : Integer.MAX_VALUE);
        if (limit > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using bandwidth limited output stream with " + limit + " kbit.");
            }
            return new MyTunesRSSOutputStreamWrapper(bitrate, dataOffset, rangeHeader.getFirstByte(), 2);
        }
        return new StreamSender.OutputStreamWrapper() {
            public OutputStream wrapStream(OutputStream stream) {
                return stream;
            }
        };
    }

    public String getWebSettings() {
        return myWebSettings;
    }

    public void setWebSettings(String webSettings) {
        myWebSettings = webSettings;
    }

    public void loadFromPreferences(JXPathContext settings) {
        setActive(JXPathUtils.getBooleanValue(settings, "active", true));
        setPasswordHash(JXPathUtils.getByteArray(settings, "password", null));
        setRss(JXPathUtils.getBooleanValue(settings, "featureRss", false));
        setPlaylist(JXPathUtils.getBooleanValue(settings, "featurePlaylist", false));
        setDownload(JXPathUtils.getBooleanValue(settings, "featureDownload", false));
        setUpload(JXPathUtils.getBooleanValue(settings, "featureUpload", false));
        setPlayer(JXPathUtils.getBooleanValue(settings, "featurePlayer", false));
        setChangePassword(JXPathUtils.getBooleanValue(settings, "featureChangePassword", false));
        setSpecialPlaylists(JXPathUtils.getBooleanValue(settings, "featureSpecialPlaylists", false));
        setCreatePlaylists(JXPathUtils.getBooleanValue(settings, "featureCreatePlaylists", false));
        setEditWebSettings(JXPathUtils.getBooleanValue(settings, "featureEditWebSettings", false));
        setResetTime(JXPathUtils.getLongValue(settings, "resetTime", System.currentTimeMillis()));
        setQuotaResetTime(JXPathUtils.getLongValue(settings, "quotaResetTime", System.currentTimeMillis()));
        setDownBytes(JXPathUtils.getLongValue(settings, "downBytes", 0));
        setQuotaDownBytes(JXPathUtils.getLongValue(settings, "quotaDownBytes", 0));
        setBytesQuota(JXPathUtils.getLongValue(settings, "bytesQuota", 0));
        setQuotaType(QuotaType.valueOf(JXPathUtils.getStringValue(settings, "quotaType", QuotaType.None.name())));
        setMaximumZipEntries(JXPathUtils.getIntValue(settings, "maximumZipEntries", 0));
        setFileTypes(JXPathUtils.getStringValue(settings, "fileTypes", null));
        setTranscoder(JXPathUtils.getBooleanValue(settings, "featureTranscoder", false));
        setSessionTimeout(JXPathUtils.getIntValue(settings, "sessionTimeout", 10));
        setBandwidthLimit(MyTunesRss.REGISTRATION.isRegistered() ? JXPathUtils.getIntValue(settings, "bandwidthLimit", 0) : 0);
        setPlaylistId(MyTunesRss.REGISTRATION.isRegistered() ? JXPathUtils.getStringValue(settings, "playlistId", null) : null);
        setSaveWebSettings(MyTunesRss.REGISTRATION.isRegistered() && JXPathUtils.getBooleanValue(settings, "saveWebSettings", false));
        setWebSettings(MyTunesRss.REGISTRATION.isRegistered() ? JXPathUtils.getStringValue(settings, "webSettings", null) : null);
    }

    public void saveToPreferences(Document settings, Element users) {
        users.appendChild(DOMUtils.createTextElement(settings, "name", getName()));
        if (getPasswordHash() != null && getPasswordHash().length > 0) {
            users.appendChild(DOMUtils.createByteArrayElement(settings, "password", getPasswordHash()));
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "active", isActive()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureRss", isRss()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featurePlaylist", isPlaylist()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureDownload", isDownload()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureUpload", isUpload()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featurePlayer", isPlayer()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureChangePassword", isChangePassword()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureSpecialPlaylists", isSpecialPlaylists()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureCreatePlaylists", isCreatePlaylists()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureEditWebSettings", isEditWebSettings()));
        users.appendChild(DOMUtils.createLongElement(settings, "resetTime", getResetTime()));
        users.appendChild(DOMUtils.createLongElement(settings, "quotaResetTime", getQuotaResetTime()));
        users.appendChild(DOMUtils.createLongElement(settings, "downBytes", getDownBytes()));
        users.appendChild(DOMUtils.createLongElement(settings, "quotaDownBytes", getQuotaDownBytes()));
        users.appendChild(DOMUtils.createLongElement(settings, "bytesQuota", getBytesQuota()));
        users.appendChild(DOMUtils.createTextElement(settings, "quotaType", getQuotaType().name()));
        users.appendChild(DOMUtils.createIntElement(settings, "maximumZipEntries", getMaximumZipEntries()));
        users.appendChild(DOMUtils.createTextElement(settings, "fileTypes", getFileTypes() != null ? getFileTypes() : ""));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureTranscoder", isTranscoder()));
        users.appendChild(DOMUtils.createIntElement(settings, "sessionTimeout", getSessionTimeout()));
        users.appendChild(DOMUtils.createIntElement(settings, "bandwidthLimit", getBandwidthLimit()));
        if (StringUtils.isNotEmpty(getPlaylistId())) {
            users.appendChild(DOMUtils.createTextElement(settings, "playlistId", getPlaylistId()));
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "saveWebSettings", isSaveWebSettings()));
        if (StringUtils.isNotEmpty(getWebSettings())) {
            users.appendChild(DOMUtils.createTextElement(settings, "webSettings", getWebSettings()));
        }
    }
}
