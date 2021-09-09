package info.xiaoc.spring.reactive.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.xiaoc.spring.reactive.bean.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Objects;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

public class PersonServerSentEventClient {

    private static final Logger logger = LoggerFactory.getLogger(PersonServerSentEventClient.class);

    public static void main(final String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        final WebClient client = WebClient.create();
        client.get()
                .uri("http://localhost:8080/personStream?seq=1")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(response -> response.body(BodyExtractors.toFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})))
                .filter(sse -> Objects.nonNull(sse.data()))
                .doOnNext(sse -> {
                    if ("Internal Error".equals(sse.event())) {
                        throw new RuntimeException(sse.data());
                    } else {
                        try {
                            Person person = mapper.readValue(sse.data(), Person.class);
                            logger.info(person.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .doOnComplete(()-> logger.info("Completed."))
                .blockLast();
    }
}
