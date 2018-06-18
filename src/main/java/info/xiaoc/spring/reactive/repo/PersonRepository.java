package info.xiaoc.spring.reactive.repo;

import info.xiaoc.spring.reactive.bean.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ionst on 16/03/2017.
 */
public class PersonRepository {

    private static final Logger logger = LoggerFactory.getLogger(PersonRepository.class);

    private Map<Integer, Person> peopleMap;

    private AtomicInteger idSequence;

    public void init() {
        logger.info("PersonRepository initializing...");
        idSequence = new AtomicInteger(0);
        peopleMap = new ConcurrentHashMap<>();
        savePerson(Mono.just(new Person("Bill")))
                .mergeWith(savePerson(Mono.just(new Person("Mike"))))
                .mergeWith(savePerson(Mono.just(new Person("Nancy"))))
                .subscribe();
        logger.info("PersonRepository initialized.");
    }

    public Flux<Person> allPeople() {
        logger.info("PersonRepository.allPeople()...");
        return Flux.fromIterable(peopleMap.values()).log();
    }

    public Mono<Person> savePerson(Mono<Person> person) {
        logger.info("PersonRepository.savePerson()...");
        return person.log()
                .map(p ->{p.setId(idSequence.addAndGet(1));return p;})
                .map(this::savePerson);
    }

    private Person savePerson(Person p) {
        logger.info("Save person start: " + p);
        peopleMap.put(p.getId(), p);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Save person end: " + p);
        return p;
    }

    public Mono<Person> getPerson(int personId) {
        logger.info("PersonRepository.getPerson()...");
        return Mono.justOrEmpty(peopleMap.get(personId));
    }
}
