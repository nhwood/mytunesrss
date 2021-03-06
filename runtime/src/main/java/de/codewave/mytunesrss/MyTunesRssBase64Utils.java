/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.MiscUtils;
import org.apache.commons.codec.binary.Base64;

/**
 * de.codewave.mytunesrss.MyTunesRssBase64Utils
 */
public class MyTunesRssBase64Utils {
    public static String encode(byte[] bytes) {
        if (bytes != null) {
            byte[] encoded = Base64.encodeBase64URLSafe(bytes);
            return MiscUtils.getUtf8String(encoded);
        }
        return null;
    }

    public static String encode(String text) {
        if (text != null) {
            return encode(MiscUtils.getUtf8Bytes(text));
        }
        return null;
    }

    public static byte[] decode(String base64) {
        if (base64 != null) {
            return Base64.decodeBase64(MiscUtils.getUtf8Bytes(base64));
        }
        return null;
    }

    public static String decodeToString(String base64) {
        if (base64 != null) {
            return MiscUtils.getUtf8String(decode(base64));
        }
        return null;
    }
}