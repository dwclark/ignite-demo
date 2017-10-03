package io.github.dwclark.ignite;

import groovy.transform.CompileStatic;
import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.events.EventType.*;

@CompileStatic
class CacheClient {

    final Ignite ignite;
    
    CacheClient(final Client client) {
        this.ignite = client.ignite;
    }
    
    void putLarge(final int loops) {
        IgniteCache<Integer, int[]> cache = ignite.getOrCreateCache(SharedConfig.CACHE_NAME);
        for(int i = 0; i < loops; ++i) {
            int[] ary = new int[4096];
            Arrays.fill(ary, i);
            cache.put(i, ary);
            if(i % 100 == 0) {
                println("put into cache ary ${i}");
            }
        }
    }

    void readLarge(final int loops) {
        IgniteCache<Integer, int[]> cache = ignite.getOrCreateCache(SharedConfig.CACHE_NAME);
        for(int i = 0; i < loops; ++i) {
            int[] got = cache.get(i);
            if(i % 100 == 0) {
                println("got back array of size: ${got.length}, first element: ${got[0]}");
            }
        }
    }
}
