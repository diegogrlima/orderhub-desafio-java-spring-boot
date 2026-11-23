package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.service.CategoryService;
import br.com.valeverde.orderhub.web.dto.CategoryRequest;
import br.com.valeverde.orderhub.web.dto.CategoryResponse;
import br.com.valeverde.orderhub.web.mapper.CatalogMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.listAll().stream().map(CatalogMapper::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return CatalogMapper.toResponse(categoryService.create(request));
    }
}
