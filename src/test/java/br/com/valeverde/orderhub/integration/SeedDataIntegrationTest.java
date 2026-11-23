package br.com.valeverde.orderhub.integration;

import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de persistência: migrations, carga inicial e consultas de repositório.
 */
class SeedDataIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Migrations carregam dados de referência e histórico")
    void seedDataIsLoaded() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM customers WHERE document NOT LIKE '0%'", Long.class)).isEqualTo(40);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE sku NOT LIKE 'TST-%'", Long.class)).isEqualTo(60);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Long.class)).isGreaterThan(3000);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM app_users", Long.class)).isEqualTo(3);
    }

    @Test
    @DisplayName("Conciliação: a soma das parcelas de cada fatura deve fechar com o total da fatura")
    void installmentsMatchInvoiceTotals() {
        Long mismatches = jdbc.queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT i.id
                      FROM invoices i
                      JOIN installments p ON p.invoice_id = i.id
                     GROUP BY i.id, i.total_amount
                    HAVING SUM(p.amount) <> i.total_amount) x
                """, Long.class);
        assertThat(mismatches).isZero();
    }

    @Test
    @DisplayName("Busca de produtos ignora maiúsculas/minúsculas")
    void productSearchIsCaseInsensitive() {
        Page<Product> page = productRepository.search("ARROZ", null, false, PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(Product::getSku).contains("MER-001");
    }

    @Test
    @DisplayName("Cliente pode ser localizado pelo CNPJ")
    void findCustomerByDocument() {
        String document = jdbc.queryForObject("SELECT document FROM customers WHERE id = 1", String.class);
        assertThat(customerRepository.findByDocument(document)).isPresent();
    }
}
