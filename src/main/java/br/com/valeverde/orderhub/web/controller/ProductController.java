package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.service.ProductService;
import br.com.valeverde.orderhub.web.dto.*;
import br.com.valeverde.orderhub.web.mapper.CatalogMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public PageResponse<ProductResponse> search(@RequestParam(required = false) String search,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(defaultValue = "false") boolean onlyActive,
                                                @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return PageResponse.of(productService.search(search, categoryId, onlyActive, pageable), CatalogMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return CatalogMapper.toResponse(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product product = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(location).body(CatalogMapper.toResponse(product));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return CatalogMapper.toResponse(productService.update(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        return CatalogMapper.toResponse(productService.adjustStock(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
