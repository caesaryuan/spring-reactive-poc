package info.xiaoc.spring.reactive.handler;

import info.xiaoc.spring.reactive.bean.Person;
import info.xiaoc.spring.reactive.repo.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class PersonHandler {

    private final PersonRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(PersonHandler.class);

    public PersonHandler(PersonRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> listPeople(ServerRequest request) {
        logger.info("PersonHandler.listPeople()...");
        Flux<Person> people = repository.allPeople();
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(people, Person.class);
    }

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        logger.info("PersonHandler.createPerson()...");
        Mono<Person> person = request.bodyToMono(Person.class);
        Mono<Person> personMono = repository.savePerson(person);
        Mono<ServerResponse> badRequest = ServerResponse.badRequest().build();
        return personMono.subscribeOn(Schedulers.elastic())
                .flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(p)))
                .switchIfEmpty(badRequest);
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) {
        int personId = Integer.valueOf(request.pathVariable("id"));
        logger.info("PersonHandler.getPerson(" + personId + ")...");
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        Mono<Person> personMono = this.repository.getPerson(personId);
        return personMono
                .flatMap(person -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(person)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> getPeopleAsStream(ServerRequest request) {
        logger.info("PersonHandler.getPeopleAsStream()...");
        Flux<ServerSentEvent> people = repository.allPeopleAsStream()
                .map(p -> ServerSentEvent.<Person>builder()
                .event("Person")
                .data(p)
                .id(p.getId().toString())
                .build());
        return ServerResponse.ok().contentType(TEXT_EVENT_STREAM).body(people, ServerSentEvent.class);
    }
}
