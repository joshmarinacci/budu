package com.joshondesign.tuneserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/23/12
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Album {
    private String name;
    private List<Track> tracks = new ArrayList<Track>();
    private static int CURRENT_ID = 0;
    private String id;

    public Album(String album) {
        this.name = album;
        this.id = "id"+CURRENT_ID++;
    }

    public void addTrack(Track track) {
        this.tracks.add(track);
    }

    public Iterable<Track> getTracks() {
        return tracks;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
