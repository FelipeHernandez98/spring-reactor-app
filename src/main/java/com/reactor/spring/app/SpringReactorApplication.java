package com.reactor.spring.app;

import com.reactor.spring.app.models.Comentarios;
import com.reactor.spring.app.models.Usuario;
import com.reactor.spring.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
		ejemploContraPresion();
    }

	public void ejemploContraPresion(){
		Flux.range(1, 10)
				.log()
				//.limitRate(2) Es una manera reducida de hacer lo del Subscriber, pide dos devuelve 2 y luego pide de neuvo 2
				.subscribe(new Subscriber<Integer>() { // Otra manera de suscribir y modificar

					private Subscription s;

					private Integer limite = 2;

					private Integer consumido = 0;

					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;
						s.request(limite);
					}

					@Override
					public void onNext(Integer integer) {
						log.info(integer.toString());
						consumido++;
						if (consumido == limite){
							consumido = 0;
							s.request(limite);
						}
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {

					}
				});
	}

	public void ejemploIntervalDesdeCreate(){

		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;
				@Override
				public void run() {
					emitter.next(++contador);
					if( contador == 10){
						timer.cancel();
						emitter.complete();
					}
					if( contador == 5){
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminado"));
//									Ó
//				.doOnNext(next -> log.info(next.toString()))
//				.doOnComplete(() -> log.info("Hemos terminado"))
//				.subscribe();
	}

	public void ejemploIntervalInfinito() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap( i -> {
					if(i >= 5){
						return Flux.error(new InterruptedException("Solo hasta 5!"));
					}
					return Flux.just(i);
				})
				.map( i -> "Hola " +i )
				.retry(2)
				.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));
		latch.await();
	}
	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.subscribe();

		Thread.sleep(13000); // Detiene la ejecucón pra poder visualizar las operaciones que esten en segundo plano ejecutandose
	}
	public void ejemploInterval(){
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));
		rango.zipWith(delay, (ra, de) -> ra)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast();
	}

	public void ejemploUsuarioRangos(){

		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4)
				.map(i -> i * 2)
				.zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));

//		Flux.just(1, 2, 3, 4)
//				.map(i -> i * 2)
//				.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, segundo Flux: %d", uno, dos))
//				.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioZipWith2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Felipe", "Hernandez"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola soy Felipe");
			comentarios.addComentario("Hola soy Andres");
			comentarios.addComentario("Argentina campeona del mundo");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosMono)
				.map( tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});

				usuarioConComentarios.subscribe( uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Felipe", "Hernandez"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola soy Felipe");
			comentarios.addComentario("Hola soy Andres");
			comentarios.addComentario("Argentina campeona del mundo");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono, (usuarios, comentarios) -> new UsuarioComentarios(usuarios, comentarios));
				usuarioConComentarios.subscribe( uc -> log.info(uc.toString()));
	}
	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Felipe", "Hernandez"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola soy Felipe");
			comentarios.addComentario("Hola soy Andres");
			comentarios.addComentario("Argentina campeona del mundo");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {

		List<Usuario> users = new ArrayList<>();
		users.add(new Usuario("Andres", "Hernandez"));
		users.add(new Usuario("Angie", "Rodriguez"));
		users.add(new Usuario("Luna", "Hernandez"));
		users.add(new Usuario("Salome", "Rodriguez"));

		Flux.fromIterable(users)
				.collectList()
				.subscribe( lista -> {
					lista.forEach( item -> log.info(item.toString()));
				});
	}

	public void ejemploToString() throws Exception {

		List<Usuario> users = new ArrayList<>();
		users.add(new Usuario("Andres", "Hernandez"));
		users.add(new Usuario("Angie", "Rodriguez"));
		users.add(new Usuario("Luna", "Hernandez"));
		users.add(new Usuario("Salome", "Rodriguez"));

		Flux.fromIterable(users)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nomb -> {
					if(nomb.contains("Rodriguez".toUpperCase())){
						return Mono.just(nomb);
					}else{
						return Mono.empty();
					}
				})
				.map(nomb -> {
					return  nomb.toLowerCase();
				}).subscribe(nomb -> log.info(nomb.toString()));
	}
	public void ejemploFlatMap() throws Exception {

		List<String> users = new ArrayList<String>();
		users.add("Andres Hernandez");
		users.add("Felipe Caicedo");
		users.add("Angie Rodriguez");
		users.add("lionel Messi");
		users.add("Rodrigo DePaul");
		users.add("Kun Aguero");

		Flux.fromIterable(users)
				.map(nomb -> new Usuario(nomb.split(" ")[0].toUpperCase(), nomb.split(" ")[1].toUpperCase()))
				.flatMap(nomb -> { // Con el flatMap puedo filtrar la data y devolver un flujo
					if(nomb.getNombre().equalsIgnoreCase("Angie")){
						return Mono.just(nomb); // Un mono se usa para guardar solo un dato
					}else{
						return Mono.empty(); // Si no encuentra coincidencia con el nombre, retorna el mono vacio
					}
				})
				.map(nomb -> {
					String nombre = nomb.getNombre().toLowerCase();
					nomb.setNombre(nombre);
					return nomb;
				}).subscribe(nomb -> log.info(nomb.toString()));
	}
	public void ejemploIterable() throws Exception {

//		Flux<String> nombres = Flux.just("Andres", "Felipe", "Angie", "Liceth")
//				.doOnNext(System.out::println);
		List<String> users = new ArrayList<String>();
		users.add("Andres Hernandez");
		users.add("Felipe Caicedo");
		users.add("Angie Rodriguez");
		users.add("lionel Messi");
		users.add("Rodrigo DePaul");
		users.add("Kun Aguero");

		Flux<String> nombres = Flux.fromIterable(users); /*Flux.just("Andres Hernandez", "Felipe Caicedo", "Angie Rodriguez", "Liceth Ramos", "lionel Messi", "Rodrigo DePaul", "Kun Aguero");*/

		Flux<Usuario> usuarios = nombres.map(nomb -> new Usuario(nomb.split(" ")[0].toUpperCase(), nomb.split(" ")[1].toUpperCase()))// Corta el nombre con espacios y devuelve una array
				.filter(nomb -> nomb.getNombre().equalsIgnoreCase("lionel"))
				.doOnNext(nomb -> {
					if (nomb == null) {
						throw new RuntimeException("El nombre no puede ser vacio");
					}
					{
						System.out.println(nomb.getNombre().concat(" ").concat(nomb.getApellido()));
					}
				})
				.map(nomb -> {
					String nombre = nomb.getNombre().toLowerCase();
					nomb.setNombre(nombre);
					return nomb;
				});

//	 nombres.subscribe(log::info); //Se resume el codigo y queda mas limpio
//	            ó
		usuarios.subscribe(nomb -> log.info(nomb.toString()),
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
