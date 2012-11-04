package com.joshondesign.tuneserver;

import java.io.File;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A parser for the iTunes Database Format
 */
class ITunesParser extends BackgroundTask<File,ITunesDB> {
    public static void main(String ... args) throws InterruptedException {
        new ITunesParser().start();
    }
    ITunesDB db = new ITunesDB();
    private DefaultHandler handler = new DefaultHandler(){
        public int dictLevel = 0;
        public boolean insideKey;
        public boolean nextIsTracks;
        public boolean doingTracks;
        public boolean insideTrack;
        public String currentKey;
        public String currentQname;
        public Track currentTrack;
        public boolean nextIsPlaylists;
        public boolean doingPlaylists;
        public boolean insidePlaylist;
        public Playlist currentPlaylist;

        @Override
        public void startDocument() throws SAXException {
            p("starting document");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if("dict".equals(qName)) {
                dictLevel++;
            }
            if("key".equals(qName)) {
                insideKey = true;
            }
            if("dict".equals(qName) && dictLevel == 2 && nextIsTracks) {
                p("starting tracks");
                doingTracks = true;
                nextIsTracks = false;
            }
            if("dict".equals(qName) && dictLevel == 2 && nextIsPlaylists) {
                p("starting tracks");
                doingPlaylists = true;
                nextIsPlaylists = false;
            }
            if("dict".equals(qName) && dictLevel == 3 && doingTracks) {
                insideTrack = true;
                currentTrack = new Track();
            }
            if("dict".equals(qName) && dictLevel == 2 && doingPlaylists) {
                insidePlaylist = true;
                currentPlaylist = new Playlist();
            }
            currentQname = qName;

            if(insidePlaylist && currentPlaylist != null) {
                if("Folder".equals(currentKey)) {
                    //u.p("inside a folder");
                    if("true".equals(qName)) {
                        p("it is a folder!");
                        currentPlaylist = null;
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(insideKey && dictLevel == 1) {
                String value = new String(ch,start,length);
                p("value = " + value);
                if("Tracks".equals(value)) {
                    nextIsTracks = true;
                }
                if("Playlists".equals(value)) {
                    nextIsPlaylists = true;
                }
            }
            if(insideTrack && insideKey) {
                currentKey = new String(ch,start,length);
            }
            if("integer".equals(currentQname) && "Track ID".equals(currentKey) && currentTrack != null) {
                String value = new String(ch,start,length);
                currentTrack.setID(Integer.parseInt(value));
            }
            if("string".equals(currentQname) && "Name".equals(currentKey) && currentTrack != null) {
                String value = new String(ch,start,length);
                currentTrack.setName(value);
                if(currentTrack.getId() == 3321) {
                    u.p("id = " + currentTrack.getId());
                    u.p("name = " + currentTrack.getName());
                }
            }
            if("string".equals(currentQname) && "Artist".equals(currentKey) && currentTrack != null) {
                currentTrack.setArtist(append(currentTrack.getArtist(),ch,start,length));
                //db.addTrackToArtist(currentTrack);
                //db.artistMap.put(currentTrack.getArtist(),currentTrack);
                //if(currentTrack.getArtist() != null && currentTrack.getArtist().startsWith("Dick")) {
//                    p(currentTrack.getArtist());
//                }
            }
            if("string".equals(currentQname) && "Album".equals(currentKey) && currentTrack != null) {
                currentTrack.setAlbum(new String(ch,start,length));
                db.addTrackToAlbum(currentTrack.getArtist(),currentTrack.getAlbum(),currentTrack);
            }
            if("string".equals(currentQname) && "Location".equals(currentKey) && currentTrack != null) {

                if(currentTrack.getLocation() == null) {
                    currentTrack.setLocation(clean(new String(ch,start,length)));
                } else {
                    currentTrack.setLocation(clean(currentTrack.getLocation()
                            +new String(ch,start,length)));
                }
            }
            if("string".equals(currentQname) && "Size".equals(currentKey) && currentTrack != null) {
                currentTrack.setSize(Integer.parseInt(new String(ch,start,length)));
            }
            if("string".equals(currentQname) && "Kind".equals(currentKey) && currentTrack != null) {
                currentTrack.setKind(new String(ch,start,length));
            }
            if(insidePlaylist && insideKey) {
                currentKey = new String(ch,start,length);
            }
            if(insidePlaylist && currentPlaylist != null) {
                String value = new String(ch,start,length);
                if("string".equals(currentQname) && "Name".equals(currentKey)) {
                    if(blacklisted(value)) {
                        currentPlaylist = null;
                    } else {
                        currentPlaylist.setName(value);
                    }
                }
                if("integer".equals(currentQname) && "Playlist ID".equals(currentKey)) {
                    u.p("got the id of a playlist: " + value);
                    currentPlaylist.setId(Integer.parseInt(value));
                }
                //pick up the track id inside the playlist
                if("Track ID".equals(currentKey) && "integer".equals(currentQname)) {
                    currentPlaylist.addTrack(Integer.parseInt(value));
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if("dict".equals(qName)) {
                if(dictLevel == 2) {
                    doingTracks = false;
                }
                dictLevel--;
                if(insideTrack && currentTrack != null) {
                    if(currentTrack.getArtist() != null) {
                    db.addTrack(currentTrack);
                    db.addTrackToArtist(currentTrack);
                    db.artistMap.put(currentTrack.getArtist(),currentTrack);
                    }
                    currentTrack = null;
                }
                insideTrack = false;
                if(insidePlaylist && currentPlaylist != null && dictLevel == 1) {
                    db.playlists.add(currentPlaylist);
                    currentPlaylist = null;
                }
            }
            if("key".equals(qName)) {
                insideKey = false;
            }
        }

        @Override
        public void endDocument() throws SAXException {
            p("ending document");
        }
    };

    private String append(String artist, char[] ch, int start, int length) {
        if(artist == null) {
            return clean(new String(ch,start,length));
        } else {
            return artist + clean(new String(ch,start, length));
        }
    }

    private String clean(String s) {
        //return s.replace("%20"," ");
        return s;
    }


    private void p(String s) {
        System.out.println(s);
    }

    private boolean blacklisted(String name) {
        if("Library".equals(name)) return true;
        if("Music".equals(name)) return true;
        if("Movies".equals(name)) return true;
        if("TV Shows".equals(name)) return true;
        if("Podcasts".equals(name)) return true;
        if("Purchased".equals(name)) return true;

        return false;
    }

    protected ITunesDB onWork(File data) {
        try {
            long time = System.currentTimeMillis();
            db = new ITunesDB();
            //File itunesdb = new File("/Users/joshmarinacci/Music/iTunes/iTunes Music Library.xml");
            //File itunesdb = new File("/Users/joshmarinacci/projects/personal/Bedrock/itunes.xml");
            File itunesdb = new File(System.getProperty("user.home")+"/Music/iTunes/iTunes Music Library.xml");
            p("parsing itunes db at : " + itunesdb);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            parser.parse(itunesdb, handler);
            
            for(Artist artist : db.getArtists()) {
                artist.processAlbums();
            }


            //remove artists that have no albums

            List<Artist> artists = new ArrayList<Artist>(db.artists.values());
            ListIterator<Artist> at = artists.listIterator();
            while(at.hasNext()) {
                Artist artist = at.next();
                if(artist.getAlbumCount() <= 0) {
                    //u.p("removing " + artist.getName());
                    db.removeArtist(artist);
                    at.remove();
                }
            }

            Collections.sort(artists, new Comparator<Artist>() {
                public int compare(Artist artist, Artist artist1) {
                    return artist.getName().compareTo(artist1.getName());
                }
            });

            db.sortedArtists = artists;

            long afterTime = System.currentTimeMillis();
            p("parse time = " + (afterTime - time) + "ms");
            u.p("total artist count " + db.sortedArtists.size());
            u.p("total playlist count " + db.playlists.size());
            for(Playlist pl : db.playlists) {
                u.p("  " + pl.getName() + " " +  pl.getTrackCount());
            }
            return db;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
