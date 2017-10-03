package io.github.dwclark.ignite;

import groovy.transform.CompileStatic;
import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.events.EventType.*;

@CompileStatic
class Client extends SharedConfig {

    final Ignite ignite;
    
    Client() {
        this.ignite = Ignition.start(basic().cache().clientMode().config())
    }

    void stop() {
        ignite.close();
    }
}
