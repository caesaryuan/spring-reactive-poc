package info.xiaoc.spring.reactive.repo;

import info.xiaoc.spring.reactive.publisher.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

public class ReactivePagingQueryManager<T> {

    private static final Logger logger = LoggerFactory.getLogger(ReactivePagingQueryManager.class);

    private DataListener<T> listener;

    private ExecutorService executorService;

    private BiFunction<Integer, Integer, List<T>> query;

    private Integer startSeq;

    private Integer pageSize;

    private boolean stopped = false;

    public ReactivePagingQueryManager(ExecutorService executorService, BiFunction<Integer, Integer, List<T>> query, Integer startSeq, Integer pageSize) {
        this.executorService = executorService;
        this.query = query;
        this.startSeq = startSeq;
        this.pageSize = pageSize;
    }

    public void setListener(DataListener<T> listener) {
        this.listener = listener;
    }

    public void start() {
        try {
            int resultSetSize = pageSize + 1;
            while (resultSetSize > pageSize && !stopped) {
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

    public void stop() {
        logger.info("Query stopped.");
        this.stopped = true;
    }
}
