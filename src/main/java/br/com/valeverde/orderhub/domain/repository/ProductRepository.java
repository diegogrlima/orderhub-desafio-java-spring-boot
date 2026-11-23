package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("""
           SELECT p FROM Product p
            WHERE (:term IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                                 OR LOWER(p.sku)  LIKE LOWER(CONCAT('%', CAST(:term AS string), '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:onlyActive = false OR p.active = true)
           """)
    Page<Product> search(@Param("term") String term,
                         @Param("categoryId") Long categoryId,
                         @Param("onlyActive") boolean onlyActive,
                         Pageable pageable);
}
