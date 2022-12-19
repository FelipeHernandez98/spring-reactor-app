package com.reactor.spring.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringReactorApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	 Flux<String> nombres = Flux.just("Andres", "Felipe", "Angie", "Liceth")
			 .doOnNext( nomb ->  {
				 System.out.println(nomb);
			 });

	 nombres.subscribe();
	}
}
