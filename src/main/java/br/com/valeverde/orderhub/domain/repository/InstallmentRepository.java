package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Installment;
import br.com.valeverde.orderhub.domain.model.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    /**
     * Soma das parcelas ainda não liquidadas do cliente (compõe a exposição de crédito).
     */
    @Query(value = """
           SELECT COALESCE(SUM(i.amount), 0)
             FROM installments i
             JOIN invoices v ON v.id = i.invoice_id
            WHERE v.customer_id = :customerId
              AND i.status <> 'CANCELLED'
           """, nativeQuery = true)
    BigDecimal sumOpenAmountByCustomer(@Param("customerId") Long customerId);

    @Modifying
    @Query("""
           UPDATE Installment i
              SET i.status = :overdue
            WHERE i.status = :open
              AND i.dueDate < :today
           """)
    int markOverdue(@Param("today") LocalDate today,
                    @Param("open") InstallmentStatus open,
                    @Param("overdue") InstallmentStatus overdue);
}
