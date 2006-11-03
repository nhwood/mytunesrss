/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.mp3;

import de.codewave.camel.mp3.*;
import de.codewave.camel.mp3.framebody.v2.*;
import de.codewave.camel.mp3.framebody.v3.*;
import de.codewave.camel.mp3.structure.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.io.*;
import org.apache.commons.logging.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.mp3.ID3Utils
 */
public class ID3Utils {
    private static final Log LOG = LogFactory.getLog(ID3Utils.class);

    public static Image getImage(Track track) {
        File file = track.getFile();
        if (file.exists() && "mp3".equals(IOUtils.getSuffix(file))) {
            try {
                Id3v2Tag id3v2Tag = Mp3Utils.readId3v2Tag(file);
                if (id3v2Tag != null && id3v2Tag.getFrames() != null) {
                    for (Frame frame : id3v2Tag.getFrames()) {
                        if ("APIC".equals(frame.getId())) {
                            APICFrameBody frameBody = new APICFrameBody(frame);
                            return new Image(frameBody.getMimeType(), frameBody.getPictureData());
                        } else if ("PIC".equals(frame.getId())) {
                            PICFrameBody frameBody = new PICFrameBody(frame);
                            return new Image(frameBody.getMimeType(), frameBody.getPictureData());
                        }
                    }
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not extract artwork for \"" + file + "\".", e);
                }
            }
        }
        return null;
    }
}