package com.joshondesign.tuneserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/23/12
 * Time: 11:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    private static ITunesDB itunes;

    public static void main(String ... args) throws InterruptedException {
        final ITunesParser parser = new ITunesParser() {
            @Override
            protected void onEnd(ITunesDB db) {
                itunes = db;
                u.p("parsing done!");
                u.p("total tracks = " + db.getTotalTrackCount());
                u.p("total playlists = " + db.playlists.size());
                u.p("total artists = " + db.artistMap.size());
                startServer(itunes);
            }
        };
        parser.start();
        u.p("started the parser");
    }

    public static void printSummary(ITunesDB itunes, OutputStream outputStream) {
        PrintWriter pw = new PrintWriter(outputStream);
        JSONPrinter j = new JSONPrinter(pw);
        j.open();
        j.println("\"artists\":[");
        j.indent();

        for(Artist artist : itunes.getArtists()) {
            j.open();
            j.indent();
            j.p("name", artist.getName());
            j.p("trackcount",artist.getTrackCount());
            j.p("albumcount",artist.getAlbumCount());
            j.println(",\"albums\":[");
            j.indent();
            boolean first = true;
            for(Album album : artist.getAlbums()) {
                if(first) {
                    j.println(" \""+album.getName()+"\"");
                } else {
                    j.println(",\""+album.getName()+"\"");
                }
                first = false;
            }
            j.outdent();
            j.println("],");
            j.outdent();
            j.close();
            j.println(",");
        }

        j.outdent();
        j.println("],");
        j.close();
        pw.close();
    }

    private static void startServer(ITunesDB itunes) {
        try {
            NanoHTTPD server = new CustomNanoHTTPD(1957, itunes);
            /*
            try {
                Thread.currentThread().sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } */
            /*
            final InetAddress address = server.getAddress();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    u.p("address = " + address.getCanonicalHostName());
                    u.p("address = " + address.getHostAddress());
                    u.p("address = " + address.getAddress());
                    for(byte b : address.getAddress()) {
                        u.p("byte = " + b);
                    }
                    u.p("address = " + address.getHostName());
                    try {
                        u.p("address local host = " + address.getLocalHost());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    JFrame frame = new JFrame("Tunes Now Server");
                    frame.add(new JLabel("we are running on : " + address.getHostAddress()));
                    frame.pack();
                    frame.setVisible(true);
                }
            });
            */
            u.p("opened server on port 1957");
            u.p("running on one or more of these interfaces:");
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()) {
                NetworkInterface net = en.nextElement();
//                u.p("=== network ");
//                u.p(net.getDisplayName());
//                u.p("loopback = " + net.isLoopback());
                if(!net.isLoopback()) {
                    for(InterfaceAddress ad : net.getInterfaceAddresses()) {
                        u.p("address = " + ad.getAddress().getHostAddress());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try {
            InetSocketAddress addr = new InetSocketAddress(1957);
            HttpServer server = HttpServer.create(addr, 0);

            server.createContext("/", new ListLibrary(itunes));
            server.createContext("/artist/", new ListArtist(itunes));
            server.createContext("/download/", new DownloadSong(itunes));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Server is listening on port " + addr.getHostName() + " " + addr.getAddress() + " " + addr.getPort() );
            dumpIP();

        } catch (Throwable thr) {
            thr.printStackTrace();
        }
        */
    }
                                                     /*

    private static void dumpIP() throws UnknownHostException {
        String hostName = InetAddress.getLocalHost().getHostName();
        InetAddress addrs[] = InetAddress.getAllByName(hostName);
        String myIp = "UNKNOWN";
        for (InetAddress addr: addrs) {

            System.out.println ("addr.getHostAddress() = " + addr.getHostAddress());
            System.out.println ("addr.getHostName() = " + addr.getHostName());
            System.out.println ("addr.isAnyLocalAddress() = " + addr.isAnyLocalAddress());
            System.out.println ("addr.isLinkLocalAddress() = " + addr.isLinkLocalAddress());
            System.out.println ("addr.isLoopbackAddress() = " + addr.isLoopbackAddress());
            System.out.println ("addr.isMulticastAddress() = " + addr.isMulticastAddress());
            System.out.println ("addr.isSiteLocalAddress() = " + addr.isSiteLocalAddress());
            System.out.println ("");
        }
        System.out.println ("\nIP = " + myIp);
    }
*/
    private static class CustomNanoHTTPD extends NanoHTTPD {
        private ITunesDB itunes;

        public CustomNanoHTTPD(int i, ITunesDB itunes) throws IOException {
            super(i);
            this.itunes = itunes;
        }
        @Override
        public NanoHTTPD.Response serve(String uri, String method, Properties header, Properties parms) {
            u.p("url = " + uri);
            if(uri.startsWith("/download/")) {
                return serveDownloadSong(uri, header);
            }
            if(uri.startsWith("/artists")) {
                return serveArtists(itunes);
            }
            if(uri.startsWith("/albums")) {
                return serveAlbums(itunes,parms);
            }
            if(uri.startsWith("/tracks")) {
                return serveTracks(itunes,parms);
            }
            return super.serve(uri, method, header, parms);
        }
        
        private Response serveTracks(ITunesDB itunes, Properties parms) {
            u.p("id = " + parms.getProperty("albumid"));
            Album album = itunes.getAlbumById(parms.getProperty("albumid"));

            u.p("album = " + album.getName());
            StringBuffer xml = new StringBuffer();
            xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xml.append("<library>\n");
            xml.append(" <tracks>\n");
            for(Track track  : album.getTracks()) {
                xml.append("  <track "
                        +" name=\""+track.getName().replace("&","_")+"\""
                        +" id='"+track.getId()+"'"
                        +" />\n");
            }
            xml.append(" </tracks>\n");
            xml.append("</library>\n");
            return new Response(HTTP_OK, MIME_XML, xml.toString());
        }


        private Response serveAlbums(ITunesDB itunes, Properties parms) {
            u.p("id = " + parms.getProperty("artistid"));
            Artist artist = itunes.getArtistByID(parms.getProperty("artistid"));
            u.p("artist = " + artist);
            StringBuffer xml = new StringBuffer();
            xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xml.append("<library>\n");
            xml.append(" <albums>\n");
            for(Album album : artist.getAlbums()) {
                u.p("album name = " + album.getName());
                u.p("id = " + album.getId());
                xml.append("  <album "
                        +" name=\""+album.getName().replace("&","_")+"\""
                        +" id='"+album.getId()+"'"
                        +" />\n");
            }
            xml.append(" </albums>\n");
            xml.append("</library>\n");
            return new Response(HTTP_OK, MIME_XML, xml.toString());
        }

        private Response serveArtists(ITunesDB itunes) {

            StringBuffer xml = new StringBuffer();
            xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xml.append("<library>\n");
            xml.append(" <artists>\n");
            for(Artist artist : itunes.sortedArtists) {
                xml.append("  <artist "
                        +" name=\""+artist.getName().replace("&","&amp;")+"\""
                        +" id='"+artist.getID()+"'"
                        +" albumcount='"+artist.getAlbumCount()+"'"
                        +" />\n");
            }
            xml.append(" </artists>\n");
            xml.append("</library>\n");
            return new Response(HTTP_OK, MIME_XML, xml.toString());
        }

        private NanoHTTPD.Response serveDownloadSong(String uri, Properties header) {
            try {
                String id = uri;
                u.p("getting song: " + id);
                String iid = id.substring(id.lastIndexOf("/")+1);
                u.p(iid);
                iid = iid.substring(0,iid.lastIndexOf("."));
                u.p(iid);
                Track track = itunes.findTrackById(Integer.parseInt(iid));
                u.p("track = " + track.getName() + " : " + track.getArtist() + " : " + track.getAlbum());
                u.p("going to stream file: \n" + track.getLocation());
                File f = u.streamToFile(new URI(track.getLocation()), File.createTempFile("foo",".mp3"));
                //File f = new File("/Users/josh/Sites/together.mp3");

                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0)
                    mime = (String) NanoHTTPD.theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
                if (mime == null)
                    mime = NanoHTTPD.MIME_DEFAULT_BINARY;

                // Support (simple) skipping:
                long startFrom = 0;
                String range = header.getProperty("Range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        if (minus > 0)
                            range = range.substring(0, minus);
                        try {
                            startFrom = Long.parseLong(range);
                        } catch (NumberFormatException nfe) {
                        }
                    }
                }

                FileInputStream fis = new FileInputStream(f);
                fis.skip(startFrom);
                NanoHTTPD.Response r = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, mime, fis);
                r.addHeader("Content-length", "" + (f.length() - startFrom));
                r.addHeader("Content-range", "" + startFrom + "-" + (f.length() - 1) + "/" + f.length());
                return r;
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

class DownloadSong implements HttpHandler {
    private ITunesDB itunes;

    public DownloadSong(ITunesDB itunes) {
        this.itunes = itunes;
    }

    public void handle(HttpExchange exchange) throws IOException {
        u.p("======================= ");
        u.p("" + exchange.getRequestURI());
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(parts[2]);
        u.p("method = " + exchange.getRequestMethod());
        u.p("getting song: " + id);
        Track track = itunes.findTrackById(id);
        u.p("going to stream file: \n" + track.getLocation());

        //dump headers
        Headers requestHeaders = exchange.getRequestHeaders();
        dumpSet(requestHeaders);
        u.p("--- responding");
        try {
            File file = new File("/Users/josh/Sites/together.mp3");
            u.p("filesize = " + file.length());
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "audio/mpeg");
            //responseHeaders.set("Cache-Control","no-cache");
            //responseHeaders.set("Accept-Ranges","bytes");
            /*
            responseHeaders.set("Content-Range",
                    "bytes 0-"+file.length()+"/"+file.length());
            responseHeaders.set("Content-Range",
                    "bytes 8022072456253489920-/"+file.length());
            u.p("file len = " + file.length());
            */
            dumpSet(responseHeaders);
            if(requestHeaders.containsKey("Range")) {
                u.p("this is a range request");
                String value = requestHeaders.get("Range").get(0);
                u.p("value = " + value);
                String p2 = value.split("=")[1];
                String[] ps = p2.split("-");
                u.p("starting of range = " + ps[0]);
                //u.p("ending of range = " + ps[1]);
                long startRange = Long.parseLong(ps[0]);
                u.p("range = " + startRange);
                responseHeaders.set("Content-Range",
                        "bytes 0-"+file.length()+"/"+file.length());
                dumpSet(responseHeaders);
                /*
                if(startRange > file.length()) {
                    u.p("out of range. can't satisfy it");
                    responseHeaders.set("Connection","close");
                    exchange.sendResponseHeaders(416,file.length());
                    exchange.close();
                    return;
                } */
                exchange.sendResponseHeaders(206, 1);
                exchange.close();
            } else {
                u.p("this is a normal request");
                responseHeaders.set("Content-Range",
                        "0-" + (file.length() - 1) + "/" + file.length());
                dumpSet(responseHeaders);
                exchange.sendResponseHeaders(200, file.length());
            }
            //URI file = new URI(track.getLocation());
            //URI file = new URI("file://localhost/Volumes/6207683")
            InputStream input = file.toURL().openStream();
            OutputStream output = exchange.getResponseBody();
            long total = 0;
            try {
                byte[] buff = new byte[1024*16];
                while(true) {
                    int n = input.read(buff);
                    if(n < 0) break;
                    output.write(buff,0,n);
                    total += n;
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            u.p("sent total bytes = " + total);
            exchange.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dumpSet(Headers requestHeaders) {
        Set<String> keySet = requestHeaders.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            List values = requestHeaders.get(key);
            String s = key + " = " + values.toString() + "\n";
            //responseBody.write(s.getBytes());
            u.p(s);
        }
    }
}

class ListArtist implements HttpHandler {
    private ITunesDB itunes;

    public ListArtist(ITunesDB itunes) {
        this.itunes = itunes;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            Headers responseHeaders = exchange.getResponseHeaders();
            u.p("" + exchange.getRequestURI());
            
            String[] parts = exchange.getRequestURI().getPath().split("/");
            String artistName = parts[2];
            String albumName =  parts[3];
            u.p("getting info for artist: " + artistName);
            u.p("album: = " + albumName);
            
            Artist artist = itunes.getArtistByName(artistName);
            Album album = artist.getAlbum(albumName);
            u.p("real album = " + album);




            responseHeaders.set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();
            PrintWriter pw = new PrintWriter(responseBody);
            JSONPrinter j = new JSONPrinter(pw);
            j.open();
            j.println("\"tracks\":[");
            j.indent();
            
            for(Track track : album.getTracks()) {
                j.open();
                j.indent();
                j.p("name", track.getName());
                j.p("location", track.getLocation());
                j.p("id",track.getId());
                j.outdent();
                j.close();
                j.println(",");
            }
            
            j.outdent();
            j.println("],");
            j.close();

            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class ListLibrary implements HttpHandler {
    private ITunesDB itunes;

    public ListLibrary(ITunesDB itunes) {
        this.itunes = itunes;
    }

    public void handle(HttpExchange exchange) throws IOException {
        u.p("on: " + exchange.getLocalAddress());
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);

            OutputStream responseBody = exchange.getResponseBody();
            Headers requestHeaders = exchange.getRequestHeaders();
            /*
            Set<String> keySet = requestHeaders.keySet();
            Iterator<String> iter = keySet.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                List values = requestHeaders.get(key);
                String s = key + " = " + values.toString() + "\n";
                responseBody.write(s.getBytes());
            } */

            Main.printSummary(itunes,responseBody);
        }
    }
}