package info.xiaoc.spring.reactive.NonBlocking;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimedEventSupport {

    private static final Timer timer = new Timer();
    private static final Logger logger = LoggerFactory.getLogger(TimedEventSupport.class);

    /**
     * Build a future to return the value after a delay.
     *
     * @param delay
     * @param value
     * @return future
     */
    public static <T> CompletableFuture<T> delayedSuccess(int delay, T value) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        TimerTask task = new TimerTask() {
            public void run() {
                logger.info("Executing task. Delay = " + delay + ", " + "Return value = " + value);
                future.complete(value);
            }
        };
        logger.info("Creating task. Delay = " + delay + ", " + "Return value = " + value);
        timer.schedule(task, delay * 1000);
        return future;
    }

    /**
     * Build a future to return a throwable after a delay.
     *
     * @param delay
     * @param t
     * @return future
     */
    public static <T> CompletableFuture<T> delayedFailure(int delay, Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        TimerTask task = new TimerTask() {
            public void run() {
                future.completeExceptionally(t);
            }
        };
        timer.schedule(task, delay * 1000);
        return future;
    }
}