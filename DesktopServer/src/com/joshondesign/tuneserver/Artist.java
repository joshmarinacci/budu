package com.joshondesign.tuneserver;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/23/12
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Artist {
    private String name;
    private List<Track> tracks = new ArrayList<Track>();
    private Map<String,Album> albums = new HashMap<String, Album>();
    private String id;
    
    private static int CURRENT_ID = 0;

    public Artist(String artist) {
        this.name = artist;
        this.id = "id"+CURRENT_ID++;
    }

    public void addTrack(Track currentTrack) {
        this.tracks.add(currentTrack);
    }

    public String getName() {
        return name;
    }

    public int getTrackCount() {
        return tracks.size();
    }

    public void processAlbums() {
        for(Track track : tracks) {
            if(!albums.containsKey(track.getAlbum())) {
                if(track.getAlbum() == null) {
                    //u.p("  dropping bad album");
                    continue;
                }
                albums.put(track.getAlbum(),new Album(track.getAlbum()));
            }
            albums.get(track.getAlbum()).addTrack(track);
        }
    }

    public Iterable<String> getAlbumNames() {
        return albums.keySet();
    }

    public int getAlbumCount() {
        return albums.size();
    }

    public Iterable<Album> getAlbums() {
        return albums.values();
    }

    public Album getAlbum(String albumName) {
        return albums.get(albumName);
    }

    public String getID() {
        return id;
    }

    public void removeAlbum(Album album) {
        this.albums.remove(album);
    }
}
