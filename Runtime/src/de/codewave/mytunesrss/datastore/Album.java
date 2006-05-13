/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

/**
 * de.codewave.mytunesrss.datastore.Album
 */
public class Album {
    private String myName;
    private int myArtistCount;
    private int myTrackCount;

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(int artistCount) {
        myArtistCount = artistCount;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }
}