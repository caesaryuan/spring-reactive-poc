package info.xiaoc.spring.reactive.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ResilienceClient {
    private static final Logger logger = LoggerFactory.getLogger(ResilienceClient.class);

    private String token;
    private final Supplier<CompletableFuture<String>> decoratedSupplier;
    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final Retry retry;
    private RemoteResource resource;

    public ResilienceClient(RemoteResource resource) {
        this.resource = resource;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .permittedNumberOfCallsInHalfOpenState(3)
                .minimumNumberOfCalls(5)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(5)
                .recordExceptions(Exception.class)
                .build();
        circuitBreaker = CircuitBreaker.of("MyCircuitBreaker", circuitBreakerConfig);

        circuitBreaker.getEventPublisher()
                .onEvent(event -> logger.info("CircuitBreaker event: " + event.getEventType() + ", current state : " + circuitBreaker.getState()));

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(Exception.class)
                .build();

        retry = Retry.of("MyRetry", retryConfig);

        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(1)
                .maxWaitDuration(Duration.ofMillis(0))
                .build();

        bulkhead = Bulkhead.of("MyBulkHead", bulkheadConfig);

        decoratedSupplier = Decorators.ofSupplier(() -> resource.get())
                .withRetry(retry)
                .withBulkhead(bulkhead)
                .decorate();

        init();
    }

    public void invoke() {
        if (token != null) {
            logger.info("Token = " + token);
        } else {
            logger.error("Token is not available");
            init();
        }
    }

    private void init() {
        try {
            bulkhead.executeCallable(() -> circuitBreaker.executeCompletionStage(() -> resource.get().whenComplete((result, ex) -> {
                logger.info("Token is retrieved : " + result);
                token = result;
                if (ex != null) throw new RuntimeException(ex);
            })));
        } catch (Exception e) {
            logger.error("BulkHead error: " + e.getLocalizedMessage());
        }
    }

}
