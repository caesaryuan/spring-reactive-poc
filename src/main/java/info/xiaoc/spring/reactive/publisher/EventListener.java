package info.xiaoc.spring.reactive.publisher;

import java.util.List;

/**
 * Created by ionst on 17/06/2018.
 */
interface EventListener<T> {
    void onDataChunk(List<T> chunk);
    void processComplete();
}
