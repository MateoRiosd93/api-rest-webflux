package com.reactive.webflux.api.restful;

import com.reactive.webflux.api.restful.models.documents.Category;
import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.category.CategoryService;
import com.reactive.webflux.api.restful.services.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@RequiredArgsConstructor
@SpringBootApplication
public class RestfulApplication implements CommandLineRunner {

	private final ProductService productService;
	private final CategoryService categoryService;
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(RestfulApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(RestfulApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reactiveMongoTemplate.dropCollection("products").subscribe();
		reactiveMongoTemplate.dropCollection("categories").subscribe();

		Category electronico = new Category("Electronico");
		Category computacion = new Category("Computacion");
		Category deportes = new Category("Deportes");

		// then: retorna un Mono, thenMany: retorna un flux (Acaba el primer flujo y luego se emite el otro flujo)
		Flux.just(electronico, computacion, deportes)
				.flatMap(categoryService::save)
				.doOnNext(category -> log.info("Category created: {}", category.getName()))
				.thenMany(
						Flux.just(
										new Product("Play station 5", 2590000.0, electronico),
										new Product("Monitor LG", 890000.0, computacion),
										new Product("Guayos F50", 800000.0, deportes),
										new Product("Teclado Logitech", 600000.0, computacion),
										new Product("Mouse Logitech", 190000.0, computacion),
										new Product("Silla Ergonomica", 1500000.0, computacion),
										new Product("Notebook", 1800000.0, electronico)
								)
								.flatMap(product -> {
									product.setCreateAt(new Date());
									return productService.save(product);
								})
				)
				.subscribe(product -> log.info("Insert: {} {}", product.getId(), product.getName()));
	}
}
