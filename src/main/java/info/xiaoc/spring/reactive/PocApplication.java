package info.xiaoc.spring.reactive;

import info.xiaoc.spring.reactive.handler.PersonHandler;
import info.xiaoc.spring.reactive.repo.PersonRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class PocApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocApplication.class, args);
	}

	@Bean(initMethod = "init")
	public PersonRepository getPersonRepository() {
		return new PersonRepository();
	}

	@Bean
	public PersonHandler getPersonHandler() {
		return new PersonHandler(getPersonRepository());
	}

	@Bean
	RouterFunction<ServerResponse> getPersonRouter() {
		return route(GET("/person/{id}").and(accept(APPLICATION_JSON)), getPersonHandler()::getPerson)
				.andRoute(GET("/person").and(accept(APPLICATION_JSON)), getPersonHandler()::listPeople)
				.andRoute(POST("/person").and(contentType(APPLICATION_JSON)), getPersonHandler()::createPerson);
	}
}
