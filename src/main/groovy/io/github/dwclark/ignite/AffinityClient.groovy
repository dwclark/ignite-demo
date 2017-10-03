package io.github.dwclark.ignite;

import groovy.transform.CompileStatic;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.ignite.*;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cache.affinity.AffinityFunction;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.events.EventType.*;

@CompileStatic
class AffinityClient {

    final Ignite ignite;
    final IgniteCluster cluster;
    final Affinity affinity;
    final AffinityFunction affinityFunction;

    AffinityClient(final Client client) {
        this.ignite = client.ignite;
        this.cluster = ignite.cluster();
        this.affinity = ignite.affinity(SharedConfig.CACHE_NAME);
        this.affinityFunction = ignite.configuration().cacheConfiguration.find { cc -> cc.name == SharedConfig.CACHE_NAME; }.affinity;
    }

    AffinityClient showAffinityInfo() {
        ClusterGroup group = cluster.forRemotes();
        println("Affinity: ${affinity}");
        println("AffinityFunction: ${affinityFunction}");

        for(int i = 0; i < 500_000; ++i) {
            int theInt = ThreadLocalRandom.current().nextInt();
            if(affinity.partition(theInt) != affinityFunction.partition(theInt)) {
                throw new IllegalStateException("${theInt}: ${affinity.partition(theInt)} != ${affinityFunction.partition(theInt)}");
            }            
        }

        println("Partition for ${400_000}: ${affinity.partition(400_000)}");
        println("Partition for ${400_000}: ${affinityFunction.partition(400_000)}");

        return this;
    }
}
