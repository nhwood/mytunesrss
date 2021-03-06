/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WatchfolderDatasourceConfig extends DatasourceConfig implements CommonTrackDatasourceConfig, CommonPhotoDatasourceConfig {

    public static final String DEFAULT_TITLE_FALLBACK = "[[[file:(.*)\\..*]]]";
    public static final String DEFAULT_ALBUM_FALLBACK = "[[[dir:0]]]";
    public static final String DEFAULT_ARTIST_FALLBACK = "[[[dir:1]]]";
    public static final String DEFAULT_SERIES_FALLBACK = "[[[dir:1]]]";
    public static final String DEFAULT_SEASON_FALLBACK = "[[[dir:0:Season ([0-9]+)]]]";
    public static final String DEFAULT_EPISODE_FALLBACK = "[[[file:[0-9]+[eEx]([0-9]+)]]]";
    public static final String DEFAULT_PHOTO_ALBUM_PATTERN = "[[[dir:1]]] - [[[dir:0]]]";

    private long myMinFileSize;
    private long myMaxFileSize;
    private Pattern myIncludePattern;
    private Pattern myExcludePattern;
    private String myTitleFallback = DEFAULT_TITLE_FALLBACK;
    private String myAlbumFallback = DEFAULT_ALBUM_FALLBACK;
    private String myArtistFallback = DEFAULT_ARTIST_FALLBACK;
    private String mySeriesFallback = DEFAULT_SERIES_FALLBACK;
    private String mySeasonFallback = DEFAULT_SEASON_FALLBACK;
    private String myEpisodeFallback = DEFAULT_EPISODE_FALLBACK;
    private VideoType myVideoType = VideoType.Movie;
    private String myPhotoAlbumPattern = DEFAULT_PHOTO_ALBUM_PATTERN;
    private boolean myIgnoreFileMeta;
    private String myArtistDropWords = "";
    private String myId3v2TrackComment = "";
    private String myDisabledMp4Codecs = "";
    private List<String> myTrackImagePatterns = new ArrayList<>();
    private ImageImportType myTrackImageImportType = ImageImportType.Auto;
    private ImageImportType myPhotoThumbnailImportType = ImageImportType.OnDemand;
    private boolean myUseSingleImageInFolder;
    private boolean myImportPlaylists = true;

    public WatchfolderDatasourceConfig(WatchfolderDatasourceConfig source) {
        super(source);
        myMinFileSize = source.getMinFileSize();
        myMaxFileSize = source.getMaxFileSize();
        myIncludePattern = source.myIncludePattern;
        myExcludePattern = source.myExcludePattern;
        myTitleFallback = source.getTitleFallback();
        myAlbumFallback = source.getAlbumFallback();
        myArtistFallback = source.getArtistFallback();
        mySeriesFallback = source.getSeriesFallback();
        mySeasonFallback = source.getSeasonFallback();
        myEpisodeFallback = source.getEpisodeFallback();
        myVideoType = source.getVideoType();
        myPhotoAlbumPattern = source.getPhotoAlbumPattern();
        myIgnoreFileMeta = source.isIgnoreFileMeta();
        myArtistDropWords = source.getArtistDropWords();
        myId3v2TrackComment = source.getId3v2TrackComment();
        myDisabledMp4Codecs = source.getDisabledMp4Codecs();
        myTrackImagePatterns = new ArrayList<>(source.getTrackImagePatterns());
        myTrackImageImportType = source.getTrackImageImportType();
        myPhotoThumbnailImportType = source.getPhotoThumbnailImportType();
        myUseSingleImageInFolder = source.isUseSingleImageInFolder();
        myImportPlaylists = source.isImportPlaylists();
    }

    public WatchfolderDatasourceConfig(String id, String name, String definition) {
        super(id, name, definition);
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Watchfolder;
    }

    public long getMinFileSize() {
        return myMinFileSize;
    }

    public void setMinFileSize(long minFileSize) {
        myMinFileSize = minFileSize;
    }

    public long getMaxFileSize() {
        return myMaxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        myMaxFileSize = maxFileSize;
    }

    public String getIncludePattern() {
        return myIncludePattern != null ? myIncludePattern.pattern() : null;
    }

    public void setIncludePattern(String includePattern) {
        myIncludePattern = StringUtils.isNotBlank(includePattern) ? Pattern.compile(includePattern, Pattern.CASE_INSENSITIVE) : null;
    }

    public String getExcludePattern() {
        return myExcludePattern != null ? myExcludePattern.pattern() : null;
    }

    public void setExcludePattern(String excludePattern) {
        myExcludePattern = StringUtils.isNotBlank(excludePattern) ? Pattern.compile(excludePattern, Pattern.CASE_INSENSITIVE) : null;
    }

    public String getTitleFallback() {
        return myTitleFallback;
    }

    public void setTitleFallback(String titleFallback) {
        myTitleFallback = titleFallback;
    }

    public String getAlbumFallback() {
        return myAlbumFallback;
    }

    public void setAlbumFallback(String albumFallback) {
        myAlbumFallback = albumFallback;
    }

    public String getArtistFallback() {
        return myArtistFallback;
    }

    public void setArtistFallback(String artistFallback) {
        myArtistFallback = artistFallback;
    }

    public String getSeriesFallback() {
        return mySeriesFallback;
    }

    public void setSeriesFallback(String seriesFallback) {
        mySeriesFallback = seriesFallback;
    }

    public String getSeasonFallback() {
        return mySeasonFallback;
    }

    public void setSeasonFallback(String seasonFallback) {
        mySeasonFallback = seasonFallback;
    }

    public String getEpisodeFallback() {
        return myEpisodeFallback;
    }

    public void setEpisodeFallback(String episodeFallback) {
        myEpisodeFallback = episodeFallback;
    }

    public VideoType getVideoType() {
        return myVideoType;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public String getPhotoAlbumPattern() {
        return myPhotoAlbumPattern;
    }

    public void setPhotoAlbumPattern(String photoAlbumPattern) {
        myPhotoAlbumPattern = photoAlbumPattern;
    }

    public boolean isIncluded(File file) {
        if (file.length() < myMinFileSize) {
            return false;
        }
        if (myMaxFileSize > 0 && file.length() > myMaxFileSize) {
            return false;
        }
        String name = file.getName();
        if (myExcludePattern != null) {
            if (myExcludePattern.matcher(name).matches()) {
                return false;
            }
        }
        return myIncludePattern == null || myIncludePattern.matcher(name).matches();
    }

    public boolean isIgnoreFileMeta() {
        return myIgnoreFileMeta;
    }

    public void setIgnoreFileMeta(boolean ignoreFileMeta) {
        myIgnoreFileMeta = ignoreFileMeta;
    }

    @Override
    public String getArtistDropWords() {
        return myArtistDropWords;
    }

    @Override
    public void setArtistDropWords(String artistDropWords) {
        myArtistDropWords = artistDropWords;
    }

    public String getId3v2TrackComment() {
        return myId3v2TrackComment;
    }

    public void setId3v2TrackComment(String id3v2TrackComment) {
        myId3v2TrackComment = id3v2TrackComment;
    }

    @Override
    public String getDisabledMp4Codecs() {
        return myDisabledMp4Codecs;
    }

    @Override
    public void setDisabledMp4Codecs(String disabledMp4Codecs) {
        myDisabledMp4Codecs = disabledMp4Codecs;
    }

    @Override
    public List<String> getTrackImagePatterns() {
        return new ArrayList<>(myTrackImagePatterns);
    }

    @Override
    public void setTrackImagePatterns(List<String> trackImagePatterns) {
        this.myTrackImagePatterns = new ArrayList<>(trackImagePatterns);
    }

    @Override
    public ImageImportType getTrackImageImportType() {
        return myTrackImageImportType;
    }

    @Override
    public void setTrackImageImportType(ImageImportType trackImageImportType) {
        myTrackImageImportType = trackImageImportType;
    }

    @Override
    public ImageImportType getPhotoThumbnailImportType() {
        return myPhotoThumbnailImportType;
    }

    @Override
    public void setPhotoThumbnailImportType(ImageImportType photoThumbnailImportType) {
        myPhotoThumbnailImportType = photoThumbnailImportType;
    }

    @Override
    public boolean isUploadable() {
        return true;
    }

    @Override
    public boolean isUseSingleImageInFolder() {
        return myUseSingleImageInFolder;
    }

    public void setUseSingleImageInFolder(boolean useSingleImageInFolder) {
        myUseSingleImageInFolder = useSingleImageInFolder;
    }

    public boolean isImportPlaylists() {
        return myImportPlaylists;
    }

    public void setImportPlaylists(boolean importPlaylists) {
        myImportPlaylists = importPlaylists;
    }
}
