package io.github.dwclark.ignite;

import groovy.transform.CompileStatic;
import org.apache.ignite.*;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.lang.*;
import org.apache.ignite.cluster.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cache.query.ScanQuery;

@CompileStatic
class MultiScanQuery implements IgniteCallable<List> {

    @IgniteInstanceResource
    private Ignite ignite;
    private IgniteCluster cluster;
    private ClusterNode localNode;
    private Affinity affinity;
    private IgniteCache cache;

    private void populateIgnite() {
        this.cluster = ignite.cluster();
        this.localNode = cluster.localNode();
        this.affinity = ignite.affinity(SharedConfig.CACHE_NAME);
        this.cache = ignite.cache(SharedConfig.CACHE_NAME);
    }
    
    static class BiPred extends IgniteBiPredicate<Integer,int[]> {
        int number;
        long total;
        
        @Override
        boolean apply(Integer key, int[] values) {
            for(int i = 0; i < values.length; ++i) {
                ++number;
                total += values[i];
            }
            
            return false;
        }

        @Override
        String toString() {
            return "number: ${number}, total: ${total}";
        }

        Map toMap() {
            return [ number: number, total: total ];
        }
    }

    private List<List<Integer>> partitioned() {
        List<Integer> partitions = Arrays.asList(affinity.primaryPartitions(localNode));
        int cores = Runtime.getRuntime().availableProcessors();
        int collateSize = (int) (partitions.size() / cores);
        return partitions.collate(collateSize);
    }

    @Override
    List call() {
        populateIgnite();
        long start = System.currentTimeMillis();
        List<CompletableFuture<Map>> all = [];

        partitioned().each { List<Integer> partitions ->
            Supplier<Map> supplier = { ->
                BiPred pred = new BiPred();
                partitions.each { Integer partition ->
                    ScanQuery<Integer,Integer> q = new ScanQuery(partition, pred);
                    cache.query(q).all;
                };

                return pred.toMap();
            } as Supplier;
            
            all << CompletableFuture.supplyAsync(supplier);
        };
        
        List ret = [];
        all.each { CompletableFuture<Map> f -> ret << f.get(); };
        long end = System.currentTimeMillis();
        println("Total time in ms: ${end - start}");
        return ret;
    }
}
