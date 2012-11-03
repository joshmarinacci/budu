package com.joshondesign.tuneserver.mdns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 11/3/12
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class JMDNSTest {
    public static void main(String ... args) throws IOException, InterruptedException {
        JmDNS mdns = JmDNS.create();
        Map<String,String> props = new HashMap<String,String>();
        props.put("foo","bar");
        props.put("baz","quxx");
        ServiceInfo service = ServiceInfo.create("_http._tcp.local.", "buduserver", 8954, 0,0, props);
        mdns.registerService(service);
        p("waiting for 15 seconds");
        Thread.sleep(15000);
        p("unregisetering");
        mdns.unregisterService(service);
        mdns.unregisterAllServices();
        mdns.close();
        p("done");
        System.exit(0);
    }

    private static void p(String done) {
        System.out.println(done);
    }
}
