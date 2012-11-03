' first screen. ListScreen
' select artists, playlists, genre, albums

Function main() as void
    GetGlobalAA().baseURL = "http://192.168.0.5:1957/"
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
    ]
    
    screen = CreateObject("roListScreen")
    screen.SetContent(menuOptions)
    
    screen.SetHeader("Welcome to The Machine")
    screen.SetTitle("Tunes Now")
    screen.setBreadcrumbText("Menu","Playlists")
    port = CreateObject("roMessagePort")
    screen.SetMessagePort(port)
    
    menuFunctions = [ShowPlaylistsMenu, ShowArtistsMenu, ShowAlbumsMenu, ShowGenresMenu]
    
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


Function  ShowPlaylistsMenu() as void
    ' a list of play lists
    screen = CreateObject("roListScreen")
    list = DownloadPlaylists()
    screen.SetContent(list)
    screen.SetHeader("Playlists a")
    screen.SetTitle("Playlists b")
    screen.setBreadcrumbText("Menu","Playlists")
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
        if(msg.isScreenClosed())
            return
        endif
    end while
End Function

Function DownloadPlaylists() as object
    playlists = [
        {
            Title:"Best of the 80s",
        },
        {
            Title:"Best of the 90s",
        },
        {
            Title:"Sleepy Mix",
        },
        {
            Title:"Rockin' Round the Tree",
        }
    ]
    return playlists
End Function


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
    
    player = createObject("roAudioPlayer")
    player.setMessagePort(port)
    player.setLoop(false)
    player.clearContent()
    
    
    index = 0
    screen.show()
    while(true)
        msg = wait(0,port)
        if (msg.getMessage() = "startup progress") then 
            goto quick
        endif
        if(msg.isScreenClosed())
            return
        endif
        print type(msg)
        print msg.getmessage()
        if(type(msg) = "roListScreenEvent")
            if(msg.isListItemSelected())
                track = list[msg.getindex()]
                print "== streaming url" + track.url
                player.stop()
                player.clearContent()
                player.setContentList(list)
                player.setNext(msg.getIndex())
                index = msg.getIndex() - 1
                player.play()
                track.HDSmallIconUrl = "pkg:/images/dessert_small.png"
                screen.show()
                screen.setItem(msg.getIndex(),track)
            endif
        endif
        if type(msg) = "roAudioPlayerEvent"
            if msg.isStatusMessage() then
                if (msg.getMessage() = "startup progress") then 
                    
                endif
                if (msg.getMessage() = "start of play") then
                    print "   status message  " + msg.getMessage()
                    print "   index = "; msg.getIndex()
                    print "   started next track"
                    print "   index = ";index
                    track = list[index]
                    track.HDSmallIconUrl = ""
                    index = index + 1
                    track = list[index]
                    print "   index = ";index
                    track.HDSmallIconUrl = "pkg:/images/dessert_small.png"
                    print "   updated"
                    screen.show()
                    screen.setItem(index,track)
                endif
                if msg.isRequestSucceeded()
                    print "request succeeded on track: ";msg.getIndex()
                endif
                if msg.isRequestFailed()
                    print "request succeeded on track: ";msg.getIndex()
                endif
            endif
        endif
        quick:
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

