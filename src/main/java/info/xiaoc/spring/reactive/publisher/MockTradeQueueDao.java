package info.xiaoc.spring.reactive.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Created by ionst on 17/06/2018.
 */
public class MockTradeQueueDao {

    private static final Logger logger = LoggerFactory.getLogger(MockTradeQueueDao.class);

    private long delayMilliSecondsPerTrade;

    private AtomicLong counter = new AtomicLong(0L);

    public MockTradeQueueDao(long delayMilliSecondsPerTrade) {
        this.delayMilliSecondsPerTrade = delayMilliSecondsPerTrade;
    }

    public Flux<Long> getPendingPublishingTradeIds(int count) {
        Random random = new Random();
        int numberOfTradeIdsAvailable = random.nextInt(count + 1);
        logger.info("Running query...");
        try {
            TimeUnit.MILLISECONDS.sleep(delayMilliSecondsPerTrade * numberOfTradeIdsAvailable);
        } catch (InterruptedException e) {

        }
        long startId = counter.addAndGet(numberOfTradeIdsAvailable) - numberOfTradeIdsAvailable;
        logger.info("Queried " + numberOfTradeIdsAvailable + " trades.");
        return Flux.fromStream(LongStream.iterate(startId, i -> i+1)
                .limit(numberOfTradeIdsAvailable)
                .mapToObj(i -> Long.valueOf(i)));
    }

}
