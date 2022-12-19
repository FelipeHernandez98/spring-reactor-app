package com.reactor.spring.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

//		Flux<String> nombres = Flux.just("Andres", "Felipe", "Angie", "Liceth")
//				.doOnNext(System.out::println);

	 Flux<String> nombres = Flux.just("Andres", "Felipe", "", "Angie", "Liceth")
			 .doOnNext( nomb ->  {
				 if(nomb.isEmpty()){
					 throw new RuntimeException("El nombre no puede ser vacio");
				 }{
					 System.out.println(nomb);
				 }
			 });

//	 nombres.subscribe(log::info); //Se resume el codigo y queda mas limpio
//	            ó
	 nombres.subscribe( nomb ->log.info(nomb),
			 error -> log.error(error.getMessage()));
	}
}
