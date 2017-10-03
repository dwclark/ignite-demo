package io.github.dwclark.ignite;

import groovy.transform.CompileStatic;
import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.events.EventType.*;

@CompileStatic
class Server extends SharedConfig {

    final Ignite ignite;

    Server(final String path, final boolean activate) {
        super(path);
        this.ignite = Ignition.start(basic().persistence().memory().cache().config());
        if(activate) {
            ignite.active(true);
            println("Cluster should now be active");
        }
    }

    public static void main(String[] args) {
        boolean activate = (args.length > 1 && args[1] == 'activate');
        new Server(args[0], activate);
    }
}
