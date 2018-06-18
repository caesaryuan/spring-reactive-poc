package info.xiaoc.spring.reactive.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by ionst on 18/06/2018.
 */
public class TradeQueuePublisher {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueuePublisher.class);

    private int randomPublishDelayMax;
    private int randomPublishDelayMin;

    public TradeQueuePublisher(int randomPublishDelayMin, int randomPublishDelayMax) {
        this.randomPublishDelayMax = randomPublishDelayMax;
        this.randomPublishDelayMin = randomPublishDelayMin;
    }

    public void publish(Long tradeId) {
        Random random = new Random();
        int delay = random.nextInt(randomPublishDelayMax - randomPublishDelayMin) + randomPublishDelayMin;
        logger.info("Publishing trade " + tradeId + " with delay " + delay);
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {

        }
        logger.info("Publish completed " + tradeId);
    }

}
