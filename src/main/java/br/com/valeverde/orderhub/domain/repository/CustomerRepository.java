package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByDocument(String document);

    boolean existsByDocument(String document);

    boolean existsByEmail(String email);

    @Query("""
           SELECT c FROM Customer c
            WHERE c.legalName LIKE CONCAT('%', :term, '%')
               OR c.tradeName LIKE CONCAT('%', :term, '%')
           """)
    Page<Customer> search(@Param("term") String term, Pageable pageable);
}
