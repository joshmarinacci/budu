package com.joshondesign.tuneserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ITunesDB {
    private List<Track> tracks;
    Map<String,Track> artistMap;
    private Map<String, Track> albumMap;
    public List<Playlist> playlists;
    Map<String,Artist> artists = new HashMap<String,Artist>();
    private Map<Integer, Track> trackmap = new HashMap<Integer, Track>();
    public List<Artist> sortedArtists;

    public ITunesDB() {
        tracks = new ArrayList<Track>();
        playlists = new ArrayList<Playlist>();
        artistMap = new HashMap<String,Track>();
        albumMap = new HashMap<String,Track>();
    }

    public Track findTrackById(int id) {
        for(Track t :tracks) {
            if(t.getId() == id) return t;
        }
        return null;
    }

    //TODO: why do we have a duplicate method here?
    public Track getTrackById(Integer id) {
        return findTrackById(id);
    }


    public void addTrackToArtist(Track currentTrack) {
        if(!artists.containsKey(currentTrack.getArtist())) {
            artists.put(currentTrack.getArtist(),new Artist(currentTrack.getArtist()));
        }
        artists.get(currentTrack.getArtist()).addTrack(currentTrack);
    }

    public Iterable<? extends Artist> getArtists() {
        return artists.values();
    }

    public Artist getArtistByName(String artistName) {
        return artists.get(artistName);
    }

    public void removeArtist(Artist artist) {
        this.artists.remove(artist.getName());
    }

    public void addTrack(Track currentTrack) {
        this.tracks.add(currentTrack);
        this.trackmap.put(currentTrack.getId(),currentTrack);
    }

    public int getTotalTrackCount() {
        return this.tracks.size();
    }

    public Artist getArtistByID(String artistid) {
        for(Artist artist : artists.values()) {
            if(artist.getID().equals(artistid)) return artist;
        }
        return null;
    }

    public Album getAlbumById(String albumid) {
        for(Artist artist : artists.values()) {
            for(Album album : artist.getAlbums()) {
                if(album.getId().equals(albumid)) {
                    return album;
                }
            }
        }
        return null;
    }

    public void addTrackToAlbum(String sartist, String album, Track currentTrack) {
        //db.albumMap.put(currentTrack.getAlbum(),currentTrack);
        //Artist artist = getArtistByName(sartist);
        //artist.get
    }

    public int getTotalAlbumCount() {
        return this.albumMap.size();
    }

    public Playlist getPlaylistByID(int id) {
        for(Playlist list : playlists) {
            if(list.getId() == id) return list;
        }
        return null;
    }

    public Iterable<? extends Genre> getGenres() {
        return new ArrayList<Genre>();
    }
}
