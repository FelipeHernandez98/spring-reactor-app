package com.reactor.spring.app;

import com.reactor.spring.app.models.Usuario;
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

	 Flux<Usuario> nombres = Flux.just("Andres Hernandez", "Felipe Caicedo", "Angie Rodriguez", "Liceth Ramos", "lionel Messi", "Rodrigo DePaul", "Kun Aguero")
			 .map(nomb -> new Usuario(nomb.split(" ")[0].toUpperCase(), nomb.split(" ")[1].toUpperCase()))// Corta el nombre con espacios y devuelve una array
			 .filter(nomb -> nomb.getNombre().equalsIgnoreCase("lionel"))
			 .doOnNext( nomb ->  {
				 if(nomb == null){
					 throw new RuntimeException("El nombre no puede ser vacio");
				 }{
					 System.out.println(nomb.getNombre().concat(" ").concat(nomb.getApellido()));
				 }
			 })
			 .map(nomb ->{
				 String nombre = nomb.getNombre().toLowerCase();
				 nomb.setNombre(nombre);
				  return nomb;
			 });

//	 nombres.subscribe(log::info); //Se resume el codigo y queda mas limpio
//	            ó
	 nombres.subscribe(nomb -> log.info(nomb.toString()),
			 error -> log.error(error.getMessage()),
			 new Runnable() {
				 @Override
				 public void run() {
					 log.info("Ha finalizado la ejecución del observable con exito!!");
				 }
			 }
	 );
	}
}
