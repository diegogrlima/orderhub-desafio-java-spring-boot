package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Category;
import br.com.valeverde.orderhub.domain.repository.CategoryRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.web.dto.CategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> listAll() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    @Transactional
    public Category create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessException("Categoria já existente: " + request.name());
        }
        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(request.description());
        return categoryRepository.save(category);
    }
}
