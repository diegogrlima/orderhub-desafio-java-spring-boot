package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query(value = """
           SELECT COALESCE(SUM(o.total_amount), 0)
             FROM orders o
            WHERE o.customer_id = :customerId
              AND o.status = 'PENDING'
           """, nativeQuery = true)
    BigDecimal sumPendingTotalByCustomer(@Param("customerId") Long customerId);

    /**
     * Vendas por dia: [0] data, [1] quantidade de pedidos, [2] valor total.
     */
    @Query(value = """
           SELECT CAST(o.created_at AS DATE) AS sales_day,
                  COUNT(*)                   AS order_count,
                  SUM(o.total_amount)        AS revenue
             FROM orders o
            WHERE o.status <> 'CANCELLED'
              AND o.created_at >= :start
              AND o.created_at <= :end
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY sales_day
           """, nativeQuery = true)
    List<Object[]> dailySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Faturamento por cliente: [0] id, [1] razão social, [2] pedidos, [3] faturado, [4] recebido.
     */
    @Query(value = """
           SELECT c.id                           AS customer_id,
                  c.legal_name                   AS customer_name,
                  COUNT(DISTINCT o.id)           AS order_count,
                  COALESCE(SUM(o.total_amount), 0) AS revenue,
                  COALESCE(SUM(p.amount), 0)     AS received
             FROM customers c
             JOIN orders o          ON o.customer_id = c.id
             LEFT JOIN invoices i   ON i.order_id = o.id
             LEFT JOIN installments inst ON inst.invoice_id = i.id
             LEFT JOIN payments p   ON p.installment_id = inst.id
            WHERE o.status <> 'CANCELLED'
              AND o.created_at >= :start
              AND o.created_at <  :end
            GROUP BY c.id, c.legal_name
            ORDER BY revenue DESC
           """, nativeQuery = true)
    List<Object[]> revenueByCustomer(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
