package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Invoice;
import br.com.valeverde.orderhub.domain.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);

    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status, Pageable pageable);

    @Query(value = "SELECT nextval('invoice_number_seq')", nativeQuery = true)
    Long nextInvoiceSequence();

    @Modifying
    @Query(value = """
           UPDATE invoices
              SET status = 'OVERDUE'
            WHERE status = 'OPEN'
              AND EXISTS (SELECT 1 FROM installments i
                           WHERE i.invoice_id = invoices.id
                             AND i.status = 'OVERDUE')
           """, nativeQuery = true)
    int markInvoicesWithOverdueInstallments();
}
