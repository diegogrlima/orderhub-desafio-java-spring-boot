package br.com.valeverde.orderhub.domain.model;

import br.com.valeverde.orderhub.exception.InvalidStatusTransitionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tier_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal tierDiscount = BigDecimal.ZERO;

    @Column(name = "coupon_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer installments = 1;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order")
    private Invoice invoice;

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public void confirm(LocalDateTime now) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidStatusTransitionException("o pedido", status, "confirmar");
        }
        status = OrderStatus.CONFIRMED;
        confirmedAt = now;
    }

    public void ship(LocalDateTime now) {
        if (status != OrderStatus.CONFIRMED) {
            throw new InvalidStatusTransitionException("o pedido", status, "expedir");
        }
        status = OrderStatus.SHIPPED;
        shippedAt = now;
    }

    public void deliver(LocalDateTime now) {
        if (status != OrderStatus.SHIPPED) {
            throw new InvalidStatusTransitionException("o pedido", status, "entregar");
        }
        status = OrderStatus.DELIVERED;
        deliveredAt = now;
    }

    public void cancel(String reason, LocalDateTime now) {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new InvalidStatusTransitionException("o pedido", status, "cancelar");
        }
        status = OrderStatus.CANCELLED;
        cancelledAt = now;
        cancellationReason = reason;
    }
}
