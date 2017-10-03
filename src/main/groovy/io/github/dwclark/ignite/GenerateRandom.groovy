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
class GenerateRandom {

    final static File directory = new File('/var/ignite/random');
    final Ignite ignite;
    final AffinityFunction affinityFunction;
    final int size;
    final List<DataOutputStream> streams;

    GenerateRandom(final Client client) {
        this.ignite = client.ignite;
        this.affinityFunction = ignite.configuration().cacheConfiguration.find { cc -> cc.name == SharedConfig.CACHE_NAME; }.affinity;
        this.size = affinityFunction.partitions();
        this.streams = new ArrayList<>(size);
    }
    
    GenerateRandom open() {
        directory.deleteDir();
        directory.mkdir();
        
        for(int i = 0; i < size; ++i) {
            def fos = new FileOutputStream(new File(directory, "${i}.bin"));
            def bos = new BufferedOutputStream(fos);
            streams << new DataOutputStream(bos);
        }

        return this;
    }

    GenerateRandom close() {
        streams.each { OutputStream os -> os.close(); };
        return this;
    }

    GenerateRandom computeRandom() {
        Random r = ThreadLocalRandom.current();
        for(int i = 0; i < 10_000_000; ++i) {
            int partition = affinityFunction.partition(i);
            DataOutputStream os = streams.get(partition);
            os.writeInt(i);
            os.writeInt(r.nextInt());
            if(i % 100_000 == 0) {
                println("Write record ${i}");
            }
        }

        return this;
    }
}
