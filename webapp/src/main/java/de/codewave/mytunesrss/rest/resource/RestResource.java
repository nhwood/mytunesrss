/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.rest.representation.*;
import de.codewave.utils.MiscUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RestResource {

    protected List<ArtistRepresentation> toArtistRepresentations(UriInfo uriInfo, HttpServletRequest request, List<Artist> artists) {
        List<ArtistRepresentation> representations = new ArrayList<ArtistRepresentation>();
        for (Artist artist : artists) {
            representations.add(toArtistRepresentation(uriInfo, request, artist));
        }
        return representations;
    }

    protected ArtistRepresentation toArtistRepresentation(UriInfo uriInfo, HttpServletRequest request, Artist artist) {
        ArtistRepresentation representation = new ArtistRepresentation(artist);
        representation.setAlbumsUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getAlbums").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(artist.getName())));
        representation.setTagsUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getTags").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(artist.getName())));
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (user.isPlaylist()) {
            representation.setM3uUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc(request, "artist=" + b64(artist.getName())), enc(request, "type=M3u"), enc(request, "_cdi=" + ue(artist.getName()) + ".m3u")));
            representation.setXspfUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc(request, "artist=" + b64(artist.getName())), enc(request, "type=Xspf"), enc(request, "_cdi=" + ue(artist.getName()) + ".xspf")));
        }
        if (user.isRss()) {
            representation.setRssUri(getAppURI(request, MyTunesRssCommand.CreateRss, enc(request, "artist=" + b64(artist.getName())), enc(request, "_cdi=" + ue(artist.getName()) + ".xml")));
        }
        if (user.isDownload()) {
            representation.setDownloadUri(getAppURI(request, MyTunesRssCommand.GetZipArchive, enc(request, "artist=" + b64(artist.getName())), enc(request, "_cda=" + ue(artist.getName()) + ".zip")));
        }
        return representation;
    }

    protected List<AlbumRepresentation> toAlbumRepresentations(UriInfo uriInfo, HttpServletRequest request, List<Album> albums) {
        List<AlbumRepresentation> representations = new ArrayList<AlbumRepresentation>();
        for (Album album : albums) {
            representations.add(toAlbumRepresentation(uriInfo, request, album));
        }
        return representations;
    }

    protected AlbumRepresentation toAlbumRepresentation(UriInfo uriInfo, HttpServletRequest request, Album album) {
        AlbumRepresentation representation = new AlbumRepresentation(album);
        representation.setTracksUri(uriInfo.getBaseUriBuilder().path(AlbumResource.class).path(AlbumResource.class, "getAlbumTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist()), MiscUtils.getUtf8UrlEncoded(album.getName())));
        representation.setArtistUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist())));
        representation.setTagsUri(uriInfo.getBaseUriBuilder().path(AlbumResource.class).path(AlbumResource.class, "getTags").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist()), MiscUtils.getUtf8UrlEncoded(album.getName())));
        if (StringUtils.isNotBlank(album.getImageHash())) {
            representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc(request, "hash=" + album.getImageHash())));
        }
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (user.isPlaylist()) {
            representation.setM3uUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc(request, "album=" + b64(album.getName())), enc(request, "type=M3u"), enc(request, "_cdi=" + ue(album.getName()) + ".m3u")));
            representation.setXspfUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc(request, "album=" + b64(album.getName())), enc(request, "type=Xspf"), enc(request, "_cdi=" + ue(album.getName()) + ".xspf")));
        }
        if (user.isRss()) {
            representation.setRssUri(getAppURI(request, MyTunesRssCommand.CreateRss, enc(request, "album=" + b64(album.getName())), enc(request, "_cdi=" + ue(album.getName()) + ".xml")));
        }
        if (user.isDownload()) {
            representation.setDownloadUri(getAppURI(request, MyTunesRssCommand.GetZipArchive, enc(request, "album=" + b64(album.getName())), enc(request, "albumartist=" + b64(album.getArtist())), enc(request, "_cda=" + ue(album.getName()) + ".zip")));
        }
        return representation;
    }

    protected List<GenreRepresentation> toGenreRepresentations(UriInfo uriInfo, List<Genre> genres) {
        List<GenreRepresentation> representations = new ArrayList<GenreRepresentation>();
        for (Genre genre : genres) {
            representations.add(toGenreRepresentation(uriInfo, genre));
        }
        return representations;
    }

    protected GenreRepresentation toGenreRepresentation(UriInfo uriInfo, Genre genre) {
        GenreRepresentation representation = new GenreRepresentation(genre);
        representation.setTracksUri(uriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())));
        representation.setAlbumsUri(uriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreAlbums").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())));
        representation.setArtistsUri(uriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreArtists").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())));
        return representation;
    }

    protected List<PlaylistRepresentation> toPlaylistRepresentations(UriInfo uriInfo, HttpServletRequest request, List<Playlist> playlists) {
        List<PlaylistRepresentation> representations = new ArrayList<PlaylistRepresentation>();
        for (Playlist playlist : playlists) {
            representations.add(toPlaylistRepresentation(uriInfo, request, playlist));
        }
        return representations;
    }

    protected PlaylistRepresentation toPlaylistRepresentation(UriInfo uriInfo, HttpServletRequest request, Playlist playlist) {
        PlaylistRepresentation representation = new PlaylistRepresentation(playlist);
        Playlist currentlyEditedPlaylist = (Playlist) request.getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST);
        if (playlist == currentlyEditedPlaylist) {
            representation.setTracksUri(uriInfo.getBaseUriBuilder().path(EditPlaylistResource.class).path(EditPlaylistResource.class, "getPlaylistTracks").build());
        } else {
            representation.setTracksUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTracks").build(playlist.getId()));
        }
        if (playlist.getId() != null) {
            // persistent playlist
            representation.setChildrenUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getPlaylistChildren").build(playlist.getId()));
            if (StringUtils.isNotBlank(playlist.getContainerId())) {
                representation.setParentUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).build(playlist.getContainerId()));
            }
            representation.setTagsUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTags").build(playlist.getId()));
            if (MyTunesRssWebUtils.getAuthUser(request).isDownload()) {
                representation.setDownloadUri(getAppURI(request, MyTunesRssCommand.GetZipArchive, enc(request, "playlist=" + playlist.getId()), enc(request, "_cda=" + ue(playlist.getName()) + ".zip")));
            }
        }
        return representation;
    }

    protected List<TrackRepresentation> toTrackRepresentations(UriInfo uriInfo, HttpServletRequest request, List<Track> tracks) {
        List<TrackRepresentation> representations = new ArrayList<TrackRepresentation>();
        for (Track track : tracks) {
            representations.add(toTrackRepresentation(uriInfo, request, track));
        }
        return representations;
    }

    protected TrackRepresentation toTrackRepresentation(UriInfo uriInfo, HttpServletRequest request, Track track) {
        TrackRepresentation representation = new TrackRepresentation(track);
        if (StringUtils.isNotBlank(track.getImageHash())) {
            representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc(request, "hash=" + track.getImageHash())));
        }
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (user.isPlaylist()) {
            representation.setM3uUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc(request, "track=" + track.getId()), enc(request, "type=M3u"), enc(request, "_cdi=" + ue(track.getName()) + ".m3u")));
            representation.setXspfUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc(request, "track=" + track.getId()), enc(request, "type=Xspf"), enc(request, "_cdi=" + ue(track.getName()) + ".xspf")));
        }
        if (user.isRss()) {
            representation.setRssUri(getAppURI(request, MyTunesRssCommand.CreateRss, enc(request, "track=" + track.getId()), enc(request, "_cdi=" + ue(track.getName()) + ".xml")));
        }
        request.setAttribute("downloadPlaybackServletUrl", MyTunesRssWebUtils.getServletUrl(request)); // prepare MyTunesFunctions
        if (user.isDownload()) {
            representation.setDownloadUri(UriBuilder.fromUri(MyTunesFunctions.downloadUrl(request, track, null)).build());
        }
        if (user.isDownload() && MyTunesRssWebUtils.isHttpLiveStreaming(request, track, true, true)) {
            representation.setHttpLiveStreamUri(UriBuilder.fromUri(MyTunesFunctions.httpLiveStreamUrl(request, track, null)).build());
        }
        if (user.isDownload()) {
            representation.setPlaybackUri(UriBuilder.fromUri(MyTunesFunctions.playbackUrl(request, track, null)).build());
        }
        representation.setTagsUri(uriInfo.getBaseUriBuilder().path(TrackResource.class).path(TrackResource.class, "getTags").build(track.getId()));
        representation.setArtistUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(track.getArtist())));
        representation.setAlbumUri(uriInfo.getBaseUriBuilder().path(AlbumResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(track.getAlbumArtist()), MiscUtils.getUtf8UrlEncoded(track.getAlbum())));
        return representation;
    }

    public URI getAppURI(HttpServletRequest request, MyTunesRssCommand command, String... paths) {
        UriBuilder uriBuilder = UriBuilder.fromUri(MyTunesRssWebUtils.getCommandCall(request, command));
        User authUser = MyTunesRssWebUtils.getAuthUser(request);
        if (authUser != null) {
            uriBuilder.path(MyTunesRssWebUtils.encryptPathInfo(request, "auth=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssBase64Utils.encode(authUser.getName()) + " " + MyTunesRssBase64Utils.encode(authUser.getPasswordHash()))));
        }
        for (String path : paths) {
            uriBuilder.path(path);
        }
        return uriBuilder.build();
    }

    protected String enc(HttpServletRequest request, String s) {
        return MyTunesRssWebUtils.encryptPathInfo(request, s);
    }

    protected String b64(String s) {
        return MyTunesRssBase64Utils.encode(s);
    }

    protected String ue(String s) {
        return MiscUtils.getUtf8UrlEncoded(s);
    }
}
