package io.github.dwclark.ignite;

import org.apache.ignite.cache.store.CacheStoreAdapter;
import groovy.transform.CompileStatic;
import javax.cache.Cache;
import org.apache.ignite.lang.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.resources.IgniteInstanceResource;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.Ignite;
import javax.cache.configuration.Factory;

@CompileStatic
class BulkLoader extends CacheStoreAdapter<Object,Object> {

    @IgniteInstanceResource
    private Ignite ignite;

    public Object load(final Object key) {
        return null;
    }

    public void delete(final Object key) {
        //NO-OP
    }

    public void write(final Cache.Entry<Object,Object> entry) {
        //NO-OP
    }

    private File processFile(final IgniteBiInClosure<Object,Object> closure, final File file) {
        file.withInputStream { InputStream is ->
            DataInputStream dataIs = new DataInputStream(is);
            while(is.available() > 0) {
                int first = dataIs.readInt();
                int[] ary = new int[100];
                for(int i = 0; i < 100; ++i) {
                    ary[i] = dataIs.readInt();
                }
                
                closure.apply(first, ary)
            }
        }

        return file;
    }

    private List<Integer> partitions() {
        Affinity aff = ignite.affinity(SharedConfig.CACHE_NAME);
        ClusterNode node = ignite.cluster().localNode();
        return Arrays.asList(aff.primaryPartitions(node));
    }
    
    @Override
    public void loadCache(final IgniteBiInClosure<Object,Object> closure, Object[] args) {
        File directory = new File(args[0].toString());
        List<CompletableFuture<File>> futures = [];
        partitions().each { Integer partition ->
            File file = new File(directory, "${partition}.bin");
            if(file.exists()) {
                futures << CompletableFuture.supplyAsync(this.&processFile.curry(closure, file) as Supplier<File>);
            }
        }

        futures.each { CompletableFuture<File> future ->
            println("Finished processing: ${future.get()}");
        }
    }
}

@CompileStatic
class BulkLoaderFactory implements Factory<BulkLoader> {

    BulkLoader create() {
        return new BulkLoader();
    }
}
