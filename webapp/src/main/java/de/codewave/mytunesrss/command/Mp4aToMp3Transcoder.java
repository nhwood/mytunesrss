package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class Mp4aToMp3Transcoder extends Transcoder {
    private boolean myActive;

    public Mp4aToMp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track.getId(), track.getFile(), request, webConfig);
        myActive = webConfig.isFaad2();
    }

    public boolean isAvailable() {
        return super.isAvailable() && MyTunesRss.CONFIG.isValidLameBinary() && MyTunesRss.CONFIG.isValidFaad2Binary();
    }

    public boolean isActive() {
        return super.isActive() || myActive;
    }

    public InputStream getStream() throws IOException {
        return new Faad2LameTranscoderStream(getFile(), getTargetBitrate(), getTargetSampleRate());
    }

    protected String getTranscoderId() {
        return "faad2lame_" + getTargetBitrate() + "_" + getTargetSampleRate();
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}