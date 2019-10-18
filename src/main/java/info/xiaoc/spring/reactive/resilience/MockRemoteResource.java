package info.xiaoc.spring.reactive.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MockRemoteResource implements RemoteResource {

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(20, new CustomizableThreadFactory("ServerThread"));
    private static final Logger logger = LoggerFactory.getLogger(MockRemoteResource.class);
    private long delay = 1000;
    AtomicLong startTime = new AtomicLong(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    AtomicBoolean isError = new AtomicBoolean(true);

    @Override
    public CompletableFuture<String> get() {
        CompletableFuture<String> future = new CompletableFuture<>();
        TimerTask task = new TimerTask() {
            public void run() {
                transit();
                if (isError.get()) {
                    logger.info("Failing task. Delay = " + delay);
                    future.completeExceptionally(new RuntimeException("Failed to retrieve token"));
                } else {
                    logger.info("Completing task. Delay = " + delay);
                    future.complete("A Token");
                }
            }
        };
        logger.info("Creating task. Delay = " + delay);
        executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        return future;
    }

    private void transit() {
        Long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        if (now - startTime.get() <= 30) {
            isError.set(true);
        } else {
            isError.set(false);
        }
    }
}
