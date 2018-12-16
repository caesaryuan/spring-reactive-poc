package info.xiaoc.spring.reactive.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ionst on 17/06/2018.
 */
public class TimedTradeQueueSource {

    private static final Logger logger = LoggerFactory.getLogger(TimedTradeQueueSource.class);

    private Timer timer = new Timer();
    private List<DataListener<Long>> listeners = new ArrayList<>();
    private long queryIntervalMilliseconds;
    private int queryBatchSize;
    private MockTradeQueueDao tradeQueueDao;

    public TimedTradeQueueSource(long queryIntervalMilliseconds, int queryBatchSize, MockTradeQueueDao tradeQueueDao) {
        this.queryIntervalMilliseconds = queryIntervalMilliseconds;
        this.queryBatchSize = queryBatchSize;
        this.tradeQueueDao = tradeQueueDao;
    }

    public void registerListener(DataListener<Long> listener) {
        listeners.add(listener);
    }

    public void start() {
        TimerTask task = new TimerTask() {
            public void run() {
                logger.info("Running query with batch size of " + queryBatchSize);
                Flux<Long> pendingPublishingTradeIds = tradeQueueDao.getPendingPublishingTradeIds(queryBatchSize);
                pendingPublishingTradeIds.collectList().subscribe(tradeIds -> {
                    for (DataListener<Long> listener : listeners) {
                        listener.onDataChunk(tradeIds);
                    }
                });

            }
        };
        logger.info("Starting query task with " + queryIntervalMilliseconds + "ms interval");
        timer.scheduleAtFixedRate(task, queryIntervalMilliseconds, queryIntervalMilliseconds);
    }

    public void stop() {
        timer.cancel();
        logger.info("Stopped query task");
        for (DataListener<Long> listener : listeners) {
            listener.processComplete();
        }
    }

}
