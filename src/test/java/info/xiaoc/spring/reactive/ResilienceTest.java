package info.xiaoc.spring.reactive;

import info.xiaoc.spring.reactive.resilience.MockRemoteResource;
import info.xiaoc.spring.reactive.resilience.ResilienceClient;
import org.junit.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ResilienceTest {

    public static void main(String[] args) {
        ResilienceClient client = new ResilienceClient(new MockRemoteResource());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(20, new CustomizableThreadFactory("ClientThread"));
        for (int i = 1; i <=10; i++) {
            executor.scheduleAtFixedRate(() -> client.invoke(), 10 + i, 3, TimeUnit.SECONDS);
            executor.scheduleAtFixedRate(() -> client.invoke(), 10 + i, 3, TimeUnit.SECONDS);
        }
    }
}
