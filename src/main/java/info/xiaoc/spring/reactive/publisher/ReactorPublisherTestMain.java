package info.xiaoc.spring.reactive.publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ionst on 18/06/2018.
 */
public class ReactorPublisherTestMain {

    public static void main(String[] args) {
        MockTradeQueueDao tradeQueueDao = new MockTradeQueueDao(10);
        TimedTradeQueueSource tradeQueueSource = new TimedTradeQueueSource(10000, 100, tradeQueueDao);
        TradeQueuePublisher publisher = new TradeQueuePublisher(1000, 5000);
        Flux<Long> bridge = Flux.create(sink -> {
            tradeQueueSource.registerListener(new EventListener<Long>() {
                @Override
                public void onDataChunk(Collection<Long> chunk) {
                    chunk.stream().forEach(sink::next);
                }

                @Override
                public void processComplete() {
                    sink.complete();
                }
            });
        }, FluxSink.OverflowStrategy.ERROR);
        bridge.publishOn(Schedulers.newParallel("TradePublishThread", 2), 1)
                .subscribe(publisher::publish);
        tradeQueueSource.start();
        try {
            TimeUnit.MILLISECONDS.sleep(60000);
        } catch (InterruptedException e) {

        }
        tradeQueueSource.stop();
    }

}
