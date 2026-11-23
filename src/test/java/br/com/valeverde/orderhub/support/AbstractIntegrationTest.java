package br.com.valeverde.orderhub.support;

import br.com.valeverde.orderhub.domain.model.Category;
import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.domain.model.CustomerTier;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.domain.repository.CategoryRepository;
import br.com.valeverde.orderhub.domain.repository.CustomerRepository;
import br.com.valeverde.orderhub.domain.repository.ProductRepository;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base dos testes de integração: sobe um PostgreSQL real embarcado (sem Docker),
 * executa as migrations Flyway (incluindo a carga de dados) e compartilha o contexto Spring.
 */
@Tag("integration")
@SpringBootTest
public abstract class AbstractIntegrationTest {

    private static final EmbeddedPostgres POSTGRES;
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() % 1_000_000_000L);

    static {
        try {
            POSTGRES = EmbeddedPostgres.builder().start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    POSTGRES.close();
                } catch (IOException ignored) {
                    // encerrando a JVM
                }
            }));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível iniciar o PostgreSQL embarcado", e);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    protected Customer newCustomer(CustomerTier tier, BigDecimal creditLimit, Integer billingDay) {
        long n = SEQUENCE.incrementAndGet();
        Customer customer = new Customer();
        customer.setLegalName("Cliente Teste " + n + " Ltda");
        customer.setTradeName("Teste " + n);
        customer.setDocument("%014d".formatted(n));
        customer.setEmail("cliente" + n + "@teste.com.br");
        customer.setCity("Campinas");
        customer.setState("SP");
        customer.setTier(tier);
        customer.setCreditLimit(creditLimit);
        customer.setBillingDay(billingDay);
        return customerRepository.save(customer);
    }

    protected Product newProduct(BigDecimal price, int stock) {
        long n = SEQUENCE.incrementAndGet();
        Category category = categoryRepository.findById(1L).orElseThrow();
        Product product = new Product();
        product.setSku("TST-" + n);
        product.setName("Produto Teste " + n);
        product.setCategory(category);
        product.setUnit("CX");
        product.setPrice(price);
        product.setStockQuantity(stock);
        return productRepository.save(product);
    }

    protected int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getStockQuantity();
    }
}
