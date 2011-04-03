/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.graphics.ImageUtils;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandlePhotoImagesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlePhotoImagesStatement.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<String, String>();
    private static final Image IMAGE_UP_TO_DATE = new Image(null, (byte[]) null);

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }


    private long myLastUpdateTime;
    private File myFile;
    private String myPhotoId;
    private Image myImage;

    public HandlePhotoImagesStatement(File file, String photoId, long lastUpdateTime) {
        myLastUpdateTime = lastUpdateTime;
        myPhotoId = photoId;
        myFile = file;
    }

    public HandlePhotoImagesStatement(File file, String photoId, Image image, long lastUpdateTime) {
        myLastUpdateTime = lastUpdateTime;
        myPhotoId = photoId;
        myFile = file;
        myImage = image;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            Image image = getImage();
            if (image != IMAGE_UP_TO_DATE && image != null && image.getData() != null && image.getData().length > 0) {
                String imageHash = MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.digest(image.getData()));
                List<Integer> imageSizes = new GetImageSizesQuery(imageHash).execute(connection).getResults();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image with hash \"" + imageHash + "\" has " + imageSizes.size() + " entries in database.");
                }
                if (!imageSizes.contains(Integer.valueOf(128))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 128.");
                    }
                    new InsertImageStatement(imageHash, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                }
                new UpdateImageForPhotoStatement(myPhotoId, imageHash).execute(connection);
            }
        } catch (Throwable t) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not extract image from file \"" + myFile.getAbsolutePath() + "\".", t);
            }
        }
    }

    private Image getImage() throws IOException {
        if (myImage != null) {
            return myImage;
        }
        if (myFile.isFile() && myFile.lastModified() >= myLastUpdateTime) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
            }
            return new Image(IMAGE_TO_MIME.get(FilenameUtils.getExtension(myFile.getName()).toLowerCase()), FileUtils.readFileToByteArray(myFile));
        } else {
            return IMAGE_UP_TO_DATE;
        }
    }

}