package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);
}
