package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Category;
import br.com.valeverde.orderhub.domain.model.OrderStatus;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.domain.repository.CategoryRepository;
import br.com.valeverde.orderhub.domain.repository.OrderItemRepository;
import br.com.valeverde.orderhub.domain.repository.ProductRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.exception.ResourceNotFoundException;
import br.com.valeverde.orderhub.web.dto.ProductRequest;
import br.com.valeverde.orderhub.web.dto.ProductUpdateRequest;
import br.com.valeverde.orderhub.web.dto.StockAdjustmentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public Page<Product> search(String term, Long categoryId, boolean onlyActive, Pageable pageable) {
        String normalized = (term == null || term.isBlank()) ? null : term.trim();
        return productRepository.search(normalized, categoryId, onlyActive, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }

    @Transactional
    public Product create(ProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (productRepository.existsBySku(sku)) {
            throw new BusinessException("Já existe um produto com o SKU " + sku);
        }
        Product product = new Product();
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setCategory(findCategory(request.categoryId()));
        product.setUnit(request.unit().trim().toUpperCase());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        Product saved = productRepository.save(product);
        log.info("Produto {} ({}) cadastrado", saved.getId(), saved.getSku());
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = findById(id);
        if (product.getPrice().compareTo(request.price()) != 0) {
            log.info("Produto {}: preço alterado de {} para {}", product.getSku(), product.getPrice(), request.price());
        }
        product.setName(request.name().trim());
        product.setCategory(findCategory(request.categoryId()));
        product.setUnit(request.unit().trim().toUpperCase());
        product.setPrice(request.price());
        product.setActive(request.active());
        return product;
    }

    @Transactional
    public Product adjustStock(Long id, StockAdjustmentRequest request) {
        Product product = findById(id);
        int newQuantity = product.getStockQuantity() + request.delta();
        if (newQuantity < 0) {
            throw new BusinessException("Ajuste deixaria o estoque negativo (atual " + product.getStockQuantity()
                    + ", ajuste " + request.delta() + ")");
        }
        product.setStockQuantity(newQuantity);
        log.info("Estoque do produto {} ajustado em {} ({}). Novo saldo: {}",
                product.getSku(), request.delta(), request.reason(), newQuantity);
        return product;
    }

    /**
     * Produtos com histórico de vendas são apenas inativados para preservar pedidos e relatórios.
     * Produtos sem nenhuma venda podem ser removidos fisicamente (ex.: cadastro feito por engano).
     */
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        boolean hasSales = orderItemRepository.existsByProductIdAndOrderStatus(id, OrderStatus.DELIVERED);
        if (hasSales) {
            product.setActive(false);
            log.info("Produto {} possui histórico de vendas e foi inativado", product.getSku());
        } else {
            productRepository.delete(product);
            log.info("Produto {} removido", product.getSku());
        }
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", categoryId));
    }
}
