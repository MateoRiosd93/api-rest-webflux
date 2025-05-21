package com.reactive.webflux.api.restful;

import com.reactive.webflux.api.restful.models.documents.Category;
import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.category.CategoryService;
import com.reactive.webflux.api.restful.services.product.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Con el AutoConfigureWebTestClient y con SpringBootTest.WebEnvironment.MOCK levantamos los test con el contexto del proyecto pero con un mock
// Con el webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT levanta un servidor de netty en un puerto random para ejecutar los tests.
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RestfulApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Value("${config.base.endpoint}")
	private String BASE_URL;

	@Test
	public void getProducts() {
		webTestClient.get()
				.uri(BASE_URL)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				// En caso de querer hacer assertions sobre la respuesta como tal tenemos el metodo consumeWith()
				.consumeWith(response -> {
					List<Product> productList = response.getResponseBody();

					Assertions.assertNotNull(productList);
				});
	}

	@Test
	public void getProductDetail() {
		Product product = productService.findByName("Play station 5").block();

		webTestClient.get()
				.uri(BASE_URL + "/{id}", Collections.singletonMap("id", Objects.requireNonNull(product).getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product1 = response.getResponseBody();

                    if (product1 != null) {
                        org.assertj.core.api.Assertions.assertThat(product1.getId()).isNotEmpty();
						org.assertj.core.api.Assertions.assertThat(product1.getName()).isEqualTo("Play station 5");
                    }


				});
				// Esto seria una forma de validar sin tener la respuesta como tipo Product
				//.jsonPath("$.id").isNotEmpty()
				//.jsonPath("$.name").isEqualTo("Play station 5");
	}

	@Test
	public void createProduct(){
		Category category = categoryService.findByName("Electronico").block();

		Product product = new Product("Play station 2", 100000.0, category);

		webTestClient.post()
				.uri(BASE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Play station 2")
				.jsonPath("$.category.name").isEqualTo("Electronico");
	}

	@Test
	public void createProductV2(){
		Category category = categoryService.findByName("Electronico").block();

		Product product = new Product("Play station 2", 100000.0, category);

		webTestClient.post()
				.uri(BASE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product1 = response.getResponseBody();

					if (product1 != null){
						org.assertj.core.api.Assertions.assertThat(product1.getId()).isNotEmpty();
						org.assertj.core.api.Assertions.assertThat(product1.getName()).isEqualTo("Play station 2");
						org.assertj.core.api.Assertions.assertThat(product1.getCategory().getName()).isEqualTo("Electronico");
					}
				});
	}

	@Test
	public void editProduct(){
		Product product = productService.findByName("Play station 5").block();
		Category category = categoryService.findByName("Electronico").block();

		Product productEdit = new Product("Play station 5 Slim", 350000.0, category);

		webTestClient.put()
				.uri(BASE_URL + "/{id}", Collections.singletonMap("id", Objects.requireNonNull(product).getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(productEdit), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Play station 5 Slim")
				.jsonPath("$.price").isEqualTo(350000.0);
	}

	@Test
	public void deleteProduct(){
		Product product = productService.findByName("Monitor LG").block();

		webTestClient.delete()
				.uri(BASE_URL + "/{id}", Collections.singletonMap("id", Objects.requireNonNull(product).getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();
	}

}
