package info.xiaoc.spring.reactive.resilience;

import java.util.concurrent.CompletableFuture;

public interface RemoteResource {
    CompletableFuture<String> get();
}
