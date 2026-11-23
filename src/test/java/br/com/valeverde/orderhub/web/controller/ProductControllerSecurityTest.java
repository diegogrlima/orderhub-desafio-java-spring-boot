package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.config.SecurityConfig;
import br.com.valeverde.orderhub.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerSecurityTest {

    private static final String VALID_PRODUCT = """
            {"sku":"TST-001","name":"Produto Teste","categoryId":1,"unit":"CX","price":10.00,"stockQuantity":5}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("Requisição sem autenticação recebe 401")
    void anonymousIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    @DisplayName("Financeiro não pode cadastrar produtos")
    void financeCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Administrador pode excluir produtos")
    void adminCanDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SALES")
    @DisplayName("Comercial NÃO pode excluir produtos")
    void salesCannotDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isForbidden());
    }
}
