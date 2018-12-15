package info.xiaoc.spring.reactive.publisher;

import java.util.Collection;

/**
 * Created by ionst on 17/06/2018.
 */
public interface EventListener<T> {
    void onDataChunk(Collection<T> chunk);
    void processComplete();
}
