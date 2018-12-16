package info.xiaoc.spring.reactive.publisher;

import java.util.Collection;

/**
 * Created by ionst on 17/06/2018.
 */
public interface DataListener<T> {
    void onDataChunk(Collection<T> chunk);
    void processComplete();
}
