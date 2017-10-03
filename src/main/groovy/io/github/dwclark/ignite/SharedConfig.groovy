package io.github.dwclark.ignite;

import groovy.transform.CompileStatic;
import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.events.EventType.*;

@CompileStatic
class SharedConfig {

    static final String CACHE_NAME = 'demoCache';
    static final String MEMORY_NAME = 'default';
    
    final IgniteConfiguration config;
    final String path;

    SharedConfig() {
        this(null);
    }

    SharedConfig(final String path) {
        this.path = path;
        this.config = new IgniteConfiguration();
    }

    final SharedConfig basic() {
        config.peerClassLoadingEnabled = true;
        config.includeEventTypes = [ EVT_TASK_STARTED,
                                     EVT_TASK_FINISHED,
                                     EVT_TASK_FAILED,
                                     EVT_TASK_TIMEDOUT,
                                     EVT_TASK_SESSION_ATTR_SET,
                                     EVT_TASK_REDUCED,
                                     EVT_CACHE_OBJECT_PUT,
                                     EVT_CACHE_OBJECT_READ,
                                     EVT_CACHE_OBJECT_REMOVED ] as int[];

        def discovery = new TcpDiscoverySpi();
        def all = (47500..47509).collect { val -> "127.0.0.1:${val}".toString(); }
        def multicast = new TcpDiscoveryMulticastIpFinder();
        multicast.addresses = all;
        discovery.ipFinder = multicast;
        config.discoverySpi = discovery
        return this;
    }

    final SharedConfig persistence() {
        if(path == null) {
            throw new IllegalStateException();
        }
        
        config.persistentStoreConfiguration = new PersistentStoreConfiguration(persistentStorePath: path);
        return this;
    }

    final SharedConfig memory() {
        def memPolicy = new MemoryPolicyConfiguration(name: MEMORY_NAME, maxSize: 2L * 1_024L * 1_024L * 1024L);
        def memConfig = new MemoryConfiguration(defaultMemoryPolicyName: MEMORY_NAME,
                                                memoryPolicies: [ memPolicy ] as MemoryPolicyConfiguration[]);
        config.memoryConfiguration = memConfig;
        return this;
    }

    final SharedConfig clientMode() {
        config.clientMode = true;
        return this;
    }
    
    final SharedConfig cache() {
        config.cacheConfiguration = [ new CacheConfiguration(name: CACHE_NAME, atomicityMode: ATOMIC, backups: 0,
                                                             cacheStoreFactory: new BulkLoaderFactory()) ] as CacheConfiguration[];
        return this;
    }

    final IgniteConfiguration config() {
        return config;
    }
}
