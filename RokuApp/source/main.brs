' first screen. ListScreen
' select artists, playlists, genre, albums

Function main() as void
    GetGlobalAA().baseURL = "http://192.168.0.5:1957/"
    sec = CreateObject("roRegistrySection", "budu")
    if (sec.Exists("baseurl")) then 
        GetGlobalAA().baseURL = sec.Read("baseurl")
    end if

    menuOptions = [
        {
            Title:"Playlists",
            ID:"1",
            ShortDescriptionLine1:"iTunes Playlists",
            HDSmallIconUrl: "pkg:/images/dessert_small.png",
        },
        {
            Title:"Artists",
            ID:"2",
            ShortDescriptionLine1:"Search by Artist",
            HDSmallIconUrl: "pkg:/images/dinner_small.png",
        },
        {
            Title:"Albums",
            ID:"3",
            ShortDescriptionLine1:"Search by Album",
            HDSmallIconUrl: "pkg:/images/lunch_small.png",
        },
        {
            Title:"Genres",
            ID:"4",
            ShortDescriptionLine1:"Search by Genre",
            HDSmallIconUrl: "pkg:/images/breakfast_small.png",
        }
        {
            Title:"Set Server",
            ID:"5",
            ShortDescriptionLine1:"set desktop server",
            HDSmallIconUrl: "pkg:/images/breakfast_small.png",
        }
    ]
    
    screen = CreateObject("roListScreen")
    screen.SetContent(menuOptions)
    
    screen.SetHeader("server: " + GetGlobalAA().baseURL)
    screen.SetTitle("Tunes Now")
    screen.setBreadcrumbText("Menu","Playlists")
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    
    menuFunctions = [ShowPlaylistsMenu, ShowArtistsMenu, ShowAlbumsMenu, ShowGenresMenu, ShowSetServer]
    
    screen.show()
    
    while(true)
        msg = wait(0,port)
        if (type(msg) = "roListScreenEvent")
            print "list screen event"
            if(msg.isListItemFocused())
                print "focused"
                screen.setBreadcrumbText("Menu", menuOptions[msg.GetIndex()].Title)
            endif
            if(msg.isListItemSelected())
                print "selected"
                menuFunctions[msg.GetIndex()]()
            endif
        endif
    end while
    
End Function

Function ShowSetServer() as void
    screen = createObject("roKeyboardScreen")
    screen.setText("192.168.0.")
    screen.addButton(1, "save")
    screen.addButton(2, "cancel")
    screen.setDisplayText("server ip address")
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    screen.show()
    while(true)
        msg = wait(0,port)
        if type(msg) = "roKeyboardScreenEvent"
             if msg.isScreenClosed()
                 return 
             else if msg.isButtonPressed() then
                 print "Evt:"; msg.GetMessage ();" idx:"; msg.GetIndex()
                 if msg.GetIndex() = 1
                     searchText = screen.GetText()
                     print "new ip address = "; searchText 
                     GetGlobalAA().baseURL = "http://"+searchText+":1957/"
                     sec = CreateObject("roRegistrySection", "budu")
                     sec.Write("baseurl", GetGlobalAA().baseURL)
                     sec.Flush() 'commit it

                     return
                 endif
             endif
         endif
    end while
    
end function

Function  ShowPlaylistsMenu() as void
    ' a list of play lists
    screen = CreateObject("roListScreen")
    list = DownloadPlaylists()
    screen.SetContent(list)
    screen.SetHeader("Playlists a")
    screen.SetTitle("Playlists b")
    screen.setBreadcrumbText("Menu","Playlists")
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    screen.show()
    while(true)
        msg = wait(0,port)
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemFocused())
            endif                    
            if(msg.isListItemSelected())
                ShowPlaylist(list[msg.getIndex()])
            endif
        endif
        if(msg.isScreenClosed())
            return
        endif
    end while
End Function

Function DownloadPlaylists() as object
    
    print "making an http request"
    http = CreateObject("roUrlTransfer")
    http.setUrl(GetGlobalAA().baseURL+"playlists/")
    print "connecting to " + http.getURL()
    xmlstring = http.getToString()
    print "donwnloaded"
    xml = CreateObject("roXMLElement")
    success = xml.parse(xmlstring)
    print "success parsing = ";success
    print "xml = ";xml.getname()

    playlists = CreateObject("roArray",0,true)
    print "count = ";playlists.Count()
    for each playlist in xml.playlists.playlist
        playlists.Push({
            Title:playlist@name,
            id:playlist@id,
            trackcount:playlist@trackcount,
            ShortDescriptionLine1: playlist@trackcount +" songs",
        })
    end for
    return playlists
    
End Function

Function ShowPlaylist(playlist) as void
    print "showing a playlist"
    screen = CreateObject("roListScreen")
    list = DownloadTracksForPlaylist(playlist)
    screen.setContent(list)
    screen.setHeader("Songs for Playlist")
    screen.setTitle(playlist.Title)
    screen.setBreadcrumbText("Songs",playlist.Title)
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    screen.show()
    while(true)
        msg = wait(0,port)
        if(msg.isScreenClosed())
            return
        endif
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemSelected())
                ShowSequencePlayer(list,list[msg.getIndex()],msg.getIndex())
            endif
        endif
    end while
end function

Function DownloadTracksForPlaylist(playlist) as object
    print "making an http request"
    http = CreateObject("roUrlTransfer")
    http.setUrl(GetGlobalAA().baseURL+"playlist?playlistid="+playlist.id)
    xmlstring = http.getToString()
    print "donwnloaded"
    xml = CreateObject("roXMLElement")
    success = xml.parse(xmlstring)
    print "success parsing = ";success
    print "xml = ";xml.getname()

    tracks = CreateObject("roArray",0,true)
    for each track in xml.tracks.track
'        print "track = ";track@name
        tracks.Push({
            Title:track@name,
            id:track@id,
            ContentType:"audio",
            name:track@name
            description:track@name + "foo",
            shortDescriptionLine1:track@name,
            Album:track@album,
            Artist:track@artist,
            format:"mp3",
            Length:Int((track@totaltime).toFloat()/1000),
            url:GetGlobalAA().baseURL+"download/"+track@id+".mp3"
        })
    end for
    return tracks
end function


Function ShowSequencePlayer(list, item, index) as void
    port = CreateObject("roMessagePort")
    screen = CreateObject("roSpringboardScreen")
    screen.setDescriptionStyle("audio")
    screen.setProgressIndicatorEnabled(true)
    screen.addButton(1,"Pause/Resume")
    screen.setStaticRatingEnabled(false)
    screen.setContent(item)
    screen.setMessagePort(port)
    
    player = createObject("roAudioPlayer")
    player.setMessagePort(port)
    player.setLoop(false)
    player.clearContent()
    
    player.stop()
    player.clearContent()
    player.setContentList(list)
    player.setNext(index)
    player.play()
    screen.show()
    screen.setProgressIndicator(60,100)
    
    startTime = CreateObject( "roDateTime" )
    currentTime = CreateObject( "roDateTime" )
    

    playing = true
    while(true)
        msg = wait(1000,port)
        currentTime.mark()
        diff = currentTime.asSeconds()-startTime.asSeconds()
        screen.setProgressIndicator(diff,list[index].Length)
        if msg <> invalid then
            if(msg.isScreenClosed())
                return
            endif
            print type(msg)
            print msg.getmessage()
            if type(msg) = "roAudioPlayerEvent"
                if msg.isStatusMessage() then
                    if (msg.getMessage() = "startup progress") then 
                        print "startup progress"
                    endif
                    if (msg.getMessage() = "start of play") then
                        print "   status message  " + msg.getMessage()
                        print "   index = "; msg.getIndex()
                        print "   started next track"
                        startTime.mark()
                        currentTime.mark()
                    endif
                    if msg.isRequestSucceeded()
                        print "request succeeded on track: ";msg.getIndex()
                    endif
                    if msg.isRequestFailed()
                        print "request succeeded on track: ";msg.getIndex()
                    endif
                endif
            endif
            if type(msg) = "roSpringboardScreenEvent"
                if msg.isRemoteKeyPressed() 
                    print "remote key pressed "; msg.getIndex()
                    if msg.getIndex()=4
                        index = index - 1
                        if(index < 0)
                            index = 0
                        endif
                        screen.setContent(list[index])
                        player.stop()
                        player.setNext(index)
                        player.play()
                    endif
                    if msg.getIndex()=5
                        index = index + 1
                        screen.setContent(list[index])
                        player.stop()
                        player.setNext(index)
                        player.play()
                    endif
                endif
                if msg.isButtonPressed()
                    print "button pressed"; msg.getIndex()
                    if(msg.getIndex() = 1) 
                        if(playing)
                            player.pause()
                            playing = false
                        else
                            player.resume()
                            playing = true
                        endif
                    endif
                endif
                if msg.isButtonInfo()
                    print "button info "; msg.getIndex()
                endif
            endif
        endif
    end while
end function

Function ShowArtist(artist) as void
    ' the main view for an artist. list their albums
    screen = CreateObject("roListScreen")
    list = DownloadAlbumsForArtist(artist)
    screen.setContent(list)
    screen.setHeader("Albums for Artist")
    screen.setTitle(artist.Title)
    screen.setBreadcrumbText("Artists",artist.Title)
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    screen.show()
    while(true)
        msg = wait(0,port)
        if(msg.isScreenClosed())
            return
        endif
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemSelected())
                ShowAlbum(artist, list[msg.GetIndex()])
            endif
        endif
    end while
End Function

Function ShowAlbum(artist, album) as void
    screen = createobject("roListScreen")
    list = DownloadTracksForAlbum(album)
    screen.setContent(list)
    screen.setHeader(album.Title)
    screen.SetTitle("Tunes Now")
    screen.setBreadcrumbText("Artists",artist.Title)
    port = createobject("roMessagePort")
    screen.setmessageport(port)
    screen.show()
    while(true)
        msg = wait(0,port)
        if(msg.isScreenClosed())
            return
        endif
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemSelected())
                ShowSequencePlayer(list, list[msg.getIndex()], msg.getIndex())
            endif
        endif
    end while
End Function

Function  ShowArtistsMenu() as void
    ' a list of artists, with an image for each artist
    screen = CreateObject("roListScreen")
    list = DownloadArtists()
    screen.SetContent(list)
    screen.SetHeader("All Artists")
    screen.SetTitle("currently playing: ")
    screen.setBreadcrumbText("Menu","Artists")
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    screen.show()
    while(true)
        msg = wait(0,port)
        if(type(msg) = "roListScreenEvent")
            print "event"
            if(msg.isListItemFocused())
            endif                    
            if(msg.isListItemSelected())
                ShowArtist(list[msg.GetIndex()])
            endif
        endif
        if(msg.isScreenClosed())
            return
        endif
    end while
End Function

Function DownloadArtists() as object
    print "making an http request"
    http = CreateObject("roUrlTransfer")
    http.setUrl(GetGlobalAA().baseURL+"artists/")
    print "connecting to " + http.getURL()
    xmlstring = http.getToString()
    print "donwnloaded"
    xml = CreateObject("roXMLElement")
    success = xml.parse(xmlstring)
    print "success parsing = ";success
    print "xml = ";xml.getname()

    artists = CreateObject("roArray",0,true)
    print "count = ";artists.Count()
    for each artist in xml.artists.artist
        artists.Push({
            Title:artist@name,
            id:artist@id,
            albumcount:artist@albumcount,
            ShortDescriptionLine1: artist@albumcount +" albums",
        })
    end for
    return artists
End Function

Function DownloadAlbumsForArtist(artist) as object
    print "making an http request"
    http = CreateObject("roUrlTransfer")
    http.setUrl(GetGlobalAA().baseURL+"albums?artistid="+artist.id)
    xmlstring = http.getToString()
    print "donwnloaded"
    xml = CreateObject("roXMLElement")
    success = xml.parse(xmlstring)
    print "success parsing = ";success
    print "xml = ";xml.getname()

    albums = CreateObject("roArray",0,true)
    for each album in xml.albums.album
        print "album = ";album.Title
        albums.Push({
            Title:album@name,
            id:album@id,
            ContentType:"episode",
            name:album@name
            description:artist.Title + " : " + album@name + "album",
            shortDescriptionLine1:album@name,
        })
    end for
    return albums
end function

Function DownloadTracksForAlbum(album) as object
    print "downloading tracks for album" + album.Title + " " + album.id
    url = GetGlobalAA().baseURL+"tracks?albumid="+album.id
    print "   from url " + url
    http = CreateObject("roUrlTransfer")
    http.setUrl(url)
    xmlstring = http.getToString()
    print "donwnloaded"
    xml = CreateObject("roXMLElement")
    success = xml.parse(xmlstring)
    print "success parsing = ";success
    print "xml = ";xml.getname()
    tracks = CreateObject("roArray",0,true)
    for each track in xml.tracks.track
        print "track = ";track@name
        tracks.Push({
            Title:track@name,
            id:track@id,
            ContentType:"episode",
            name:track@name,
            url:GetGlobalAA().baseURL+"download/"+track@id+".mp3",
            format:"mp3"
        })
    end for
    return tracks
end function


Function  ShowAlbumsMenu() as void
    ' a list of albums, with an image for each album
    screen = CreateObject("roListScreen")
    list = DownloadAlbums()
    screen.SetContent(list)
    screen.SetHeader("Albums a")
    screen.SetTitle("Albums b")
    screen.setBreadcrumbText("Menu","Albums")
    port = CreateObject("roMessagePort")
    screen.show()
    while(true)
        msg = wait(0,port)
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemFocused())
            endif                    
            if(msg.isListItemSelected())
            endif
        endif
    end while
End Function

Function DownloadAlbums() as object
    playlists = [
        {
            Title:"Best of Tears for Fears",
        },
        {
            Title:"Christmas Masters",
        },
        {
            Title:"The White Album",
        },
        {
            Title:"Joshua Tree",
        }
    ]
    return playlists
End Function




Function  ShowGeneresMenu() as void
    ' a list of genres
    screen = CreateObject("roListScreen")
    list = DownloadGenres()
    screen.SetContent(list)
    screen.SetHeader("Genres a")
    screen.SetTitle("Genres b")
    screen.setBreadcrumbText("Menu","Genres")
    port = CreateObject("roMessagePort")
    screen.show()
    while(true)
        msg = wait(0,port)
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemFocused())
            endif                    
            if(msg.isListItemSelected())
            endif
        endif
    end while
End Function


Function DownloadGenres() as object
    playlists = [
        {
            Title:"Holiday",
        },
        {
            Title:"Classical",
        },
        {
            Title:"Electronica",
        },
        {
            Title:"Punk Jazz",
        }
    ]
    return playlists
End Function

