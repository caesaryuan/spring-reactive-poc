package info.xiaoc.spring.reactive.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ionst on 18/06/2018.
 */
public class ReactorPublisherTestMain2 {

    private static final Logger logger = LoggerFactory.getLogger(ReactorPublisherTestMain2.class);

    public static void main(String[] args) {
        MockTradeQueueDao tradeQueueDao = new MockTradeQueueDao(10);
        TradeQueuePublisher publisher = new TradeQueuePublisher(1000, 2000);
        Disposable subscribe = Flux.interval(Duration.ofMillis(1000), Duration.ofMillis(10000), Schedulers.newSingle("Query"))
                .onBackpressureDrop(i -> {
                    logger.error("Dropped signal " + i);
                })
                .flatMap(i -> tradeQueueDao.getPendingPublishingTradeIds(100))
                .onBackpressureDrop(i -> {
                    logger.error("Dropped trade " + i);
                })
                .parallel(5, 1)
                .runOn(Schedulers.newParallel("Publish"), 1)
                .subscribe(tradeId -> publisher.publish(tradeId));
        try {
            TimeUnit.MILLISECONDS.sleep(60000);
        } catch (InterruptedException e) {

        }
        subscribe.dispose();
        logger.info("Flux disposed");
        Schedulers.shutdownNow();
    }

}
