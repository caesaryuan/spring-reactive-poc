package info.xiaoc.spring.reactive.client;

import info.xiaoc.spring.reactive.bean.Person;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

public class PersonServerSentEventClient {
    public static void main(final String[] args) {
        final WebClient client = WebClient.create();
        client.get()
                .uri("http://localhost:8080/personStream")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(response -> response.body(BodyExtractors.toFlux(new ParameterizedTypeReference<ServerSentEvent<Person>>() {
                })))
                .filter(sse -> Objects.nonNull(sse.data()))
                .map(ServerSentEvent::data)
                .doOnNext(p -> System.out.println(p))
                .doOnComplete(()-> System.out.println("Completed."))
                .blockLast();
    }
}
