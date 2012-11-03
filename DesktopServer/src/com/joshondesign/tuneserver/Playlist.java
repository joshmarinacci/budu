package com.joshondesign.tuneserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 9, 2010
 * Time: 1:13:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Playlist {
    private String name;
    private List<Integer> trackIDs;

    public Playlist() {
        trackIDs = new ArrayList<Integer>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "name='" + name + '\'' +
                '}';
    }

    public void addTrack(int id) {
        trackIDs.add(id);
    }

    public List<? extends Integer> getTrackIDs() {
        return trackIDs;
    }

    public int getTrackCount() {
        return trackIDs.size();
    }
}
