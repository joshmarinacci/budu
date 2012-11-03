package com.joshondesign.tuneserver;

import java.io.*;
import java.net.InterfaceAddress;
import java.net.URI;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/23/12
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class u {
    public static void p(String s) {
        System.out.println(s);
    }

    public static File streamToFile(URI uri, File foo) throws IOException {
        u.p("copying " + uri + " to " + foo.getAbsolutePath());
        InputStream input = uri.toURL().openStream();
        OutputStream output = new FileOutputStream(foo);
        byte[] buff = new byte[1024*16];
        long total = 0;
        while(true) {
            int n = input.read(buff);
            if(n < 0) break;
            output.write(buff,0,n);
            total += n;
        }
        p("wrote " + total + " bytes");
        input.close();
        output.close();
        return foo;
    }

    public static void p(List<InterfaceAddress> interfaceAddresses) {
        for(InterfaceAddress address : interfaceAddresses) {
            p(address.toString());
            p("");
        }
    }
}
