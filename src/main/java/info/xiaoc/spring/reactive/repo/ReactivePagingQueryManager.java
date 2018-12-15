package info.xiaoc.spring.reactive.repo;

import info.xiaoc.spring.reactive.publisher.EventListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

public class ReactivePagingQueryManager<T> {

    private EventListener<T> listener;

    private ExecutorService executorService;

    private BiFunction<Integer, Integer, List<T>> query;

    private Integer startSeq;

    private Integer pageSize;

    public ReactivePagingQueryManager(ExecutorService executorService, BiFunction<Integer, Integer, List<T>> query, Integer startSeq, Integer pageSize) {
        this.executorService = executorService;
        this.query = query;
        this.startSeq = startSeq;
        this.pageSize = pageSize;
    }

    public void run() {
        try {
            int resultSetSize = pageSize + 1;
            while (resultSetSize > pageSize) {
                Future<List<T>> future = executorService.submit(() -> query.apply(startSeq, pageSize + 1));
                List<T> result = future.get();
                resultSetSize = result.size();
                if (resultSetSize > pageSize) {
                    result = result.subList(0, resultSetSize - 1);
                }
                listener.onDataChunk(result);
                startSeq += pageSize;
            }
            listener.processComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(EventListener<T> listener) {
        this.listener = listener;
    }
}
