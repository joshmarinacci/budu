package com.joshondesign.tuneserver;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Jul 2, 2010
* Time: 6:22:19 PM
* To change this template use File | Settings | File Templates.
*/
public class Track {
    private int id;
    private String name;
    private String artist;
    private String album;
    private String location;
    private int size;
    private String kind;

    public void setID(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

    public String getName() {
        return name;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public int getId() {
        return id;
    }

    public void setSize(int size) {
        this.size = size;
    }


    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLocation() {
        return location;
    }
}
