package info.xiaoc.spring.reactive.repo;

import info.xiaoc.spring.reactive.bean.Person;
import info.xiaoc.spring.reactive.publisher.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ionst on 16/03/2017.
 */
public class PersonRepository {

    private static final Logger logger = LoggerFactory.getLogger(PersonRepository.class);

    private Map<Long, Person> peopleMap;

    private AtomicInteger idSequence;

    private final int totalNumberOfPeople = 7890;

    private final int pageSize = 500;

    private final int threadPoolSize = 1;

    private ExecutorService threadPool;

    public void init() {
        logger.info("PersonRepository initializing...");
        idSequence = new AtomicInteger(0);
        peopleMap = new ConcurrentHashMap<>();
        savePerson(Mono.just(new Person("Bill")))
                .mergeWith(savePerson(Mono.just(new Person("Mike"))))
                .mergeWith(savePerson(Mono.just(new Person("Nancy"))))
                .subscribe();
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        logger.info("PersonRepository initialized.");
    }

    public Flux<Person> allPeople() {
        logger.info("PersonRepository.allPeople()...");
        return Flux.fromIterable(peopleMap.values()).log();
    }

    public Flux<Person> allPeopleAsStream() {
        logger.info("PersonRepository.allPeopleAsStream()...");

        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> new Person(i, "Person " + i))
                .take(100);
    }

    public Flux<Person> allPeopleAsStream(int startSeq) {
        logger.info("PersonRepository.allPeopleAsStream(startSeq)...");
        return Flux.create(sink -> {
            ReactivePagingQueryManager<Person> queryManager = new ReactivePagingQueryManager<>(threadPool, this::getPeopleByPage, startSeq, pageSize);
            queryManager.setListener(new EventListener<Person>() {
                @Override
                public void onDataChunk(Collection<Person> chunk) {
                    chunk.forEach(sink::next);
                }

                @Override
                public void processComplete() {
                    sink.complete();
                }
            });
            queryManager.run();
        });
    }

    private List<Person> getPeopleByPage(int startSeq, int pageSize) {
        Random random = new Random();
        logger.info("Running query...startSeq = " +  startSeq + " pageSize = " + pageSize);
        try {
            TimeUnit.MILLISECONDS.sleep((random.nextInt(4) + 1) * 500);
        } catch (InterruptedException e) {

        }
        List<Person> result = new ArrayList<>();
        for (int i = startSeq; i < startSeq + pageSize && i <= totalNumberOfPeople; i++) {
            result.add(new Person(i, "Person " + i));
        }
        logger.info("Completed query...startSeq = " +  startSeq + " pageSize = " + pageSize + " result count = " + result.size());
        return result;
    }

    public Mono<Person> savePerson(Mono<Person> person) {
        logger.info("PersonRepository.savePerson()...");
        return person.log()
                .map(p ->{p.setId((long) idSequence.addAndGet(1));return p;})
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

    public Mono<Person> getPerson(Long personId) {
        logger.info("PersonRepository.getPerson()...");
        return Mono.justOrEmpty(peopleMap.get(personId));
    }
}
