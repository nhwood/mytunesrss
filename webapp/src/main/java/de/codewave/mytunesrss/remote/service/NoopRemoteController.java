package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.datastore.statement.Track;

import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.remote.service.NoopRemoteController
 */
public class NoopRemoteController implements RemoteController {
    public void loadPlaylist(String playlistId) {
        // intentionally left blank
    }

    public void loadAlbum(String albumName, String albumArtistName) {
        // intentionally left blank
    }

    public void loadArtist(String artistName, boolean fullAlbums) {
        // intentionally left blank
    }

    public void loadGenre(String genreName) {
        // intentionally left blank
    }

    public void loadTracks(String[] trackIds) {
        // intentionally left blank
    }

    public void addTracks(String[] trackIds, boolean startPlaybackIfStopped) {
        // intentionally left blank
    }

    public void clearPlaylist() {
        // intentionally left blank
    }

    public void play(int index) {
        // intentionally left blank
    }

    public void pause() {
        // intentionally left blank
    }

    public void stop() {
        // intentionally left blank
    }

    public void next() {
        // intentionally left blank
    }

    public void prev() {
        // intentionally left blank
    }

    public void seek(int percentage) {
        // intentionally left blank
    }

    public RemoteTrackInfo getCurrentTrackInfo() {
        return null;
    }

    public void setVolume(int percentage) {
        // intentionally left blank
    }

    public boolean setFullScreen(boolean fullScreen) {
        return false;
    }

    public void shuffle() {
        // intentionally left blank
    }

    public List<Track> getPlaylist() {
        return Collections.emptyList();
    }

    public Track getTrack(int index) {
        return null;
    }

    public void setAirtunesTargets(String[] airtunesTargets) throws Exception {
        // intentionally left blank
    }
}