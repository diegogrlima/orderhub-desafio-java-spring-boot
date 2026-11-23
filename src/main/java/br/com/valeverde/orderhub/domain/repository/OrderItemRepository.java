package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.OrderItem;
import br.com.valeverde.orderhub.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByProductIdAndOrderStatus(Long productId, OrderStatus status);
}
