package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.*;
import br.com.valeverde.orderhub.domain.repository.CustomerRepository;
import br.com.valeverde.orderhub.domain.repository.InvoiceRepository;
import br.com.valeverde.orderhub.domain.repository.OrderRepository;
import br.com.valeverde.orderhub.domain.repository.ProductRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.exception.CreditLimitExceededException;
import br.com.valeverde.orderhub.exception.InsufficientStockException;
import br.com.valeverde.orderhub.exception.ResourceNotFoundException;
import br.com.valeverde.orderhub.service.billing.InvoiceFactory;
import br.com.valeverde.orderhub.service.pricing.PricingCalculator;
import br.com.valeverde.orderhub.service.pricing.PricingResult;
import br.com.valeverde.orderhub.web.dto.CreateOrderRequest;
import br.com.valeverde.orderhub.web.dto.OrderItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final CouponService couponService;
    private final CreditService creditService;
    private final InvoiceService invoiceService;
    private final PricingCalculator pricingCalculator;
    private final InvoiceFactory invoiceFactory;
    private final Clock clock;

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    public Page<Order> list(Long customerId, OrderStatus status, Pageable pageable) {
        if (customerId != null) {
            return orderRepository.findByCustomerId(customerId, pageable);
        }
        if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        }
        if (customerId != null && status != null) {
            return orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    /**
     * Cria um pedido PENDING: reserva estoque, aplica descontos e frete, consome cupom e valida o limite de crédito.
     */
    @Transactional
    public Order create(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.customerId()));
        if (!customer.isActive()) {
            throw new BusinessException("Cliente inativo não pode realizar pedidos");
        }
        if (request.installments() > 6) {
            throw new BusinessException("Número máximo de parcelas é 6");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setInstallments(request.installments());
        order.setNotes(request.notes());
        order.setCreatedAt(now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : consolidate(request).entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto", entry.getKey()));
            if (!product.isActive()) {
                throw new BusinessException("Produto inativo não pode ser vendido: " + product.getSku());
            }
            int quantity = entry.getValue();
            if (product.getStockQuantity() < quantity) {
                throw new InsufficientStockException(product.getSku(), product.getStockQuantity(), quantity);
            }
            product.setStockQuantity(product.getStockQuantity() - quantity);

            OrderItem item = OrderItem.of(product, quantity, product.getPrice());
            order.addItem(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            coupon = couponService.validateAndConsume(request.couponCode(), subtotal, today());
            order.setCoupon(coupon);
        }

        PricingResult pricing = pricingCalculator.calculate(
                subtotal, customer.getTier(), coupon == null ? null : coupon.getDiscountPercent());
        order.setSubtotal(pricing.subtotal());
        order.setTierDiscount(pricing.tierDiscount());
        order.setCouponDiscount(pricing.couponDiscount());
        order.setShippingFee(pricing.shippingFee());
        order.setTotalAmount(pricing.total());

        CreditSummary credit = creditService.summarize(customer);
        if (pricing.total().compareTo(credit.availableCredit()) > 0) {
            throw new CreditLimitExceededException(credit.availableCredit(), pricing.total());
        }

        Order saved = orderRepository.save(order);
        log.info("Pedido {} criado para o cliente {} - total R$ {}", saved.getId(), customer.getId(), saved.getTotalAmount());
        return saved;
    }

    /**
     * Confirma o pedido e emite a fatura com as parcelas.
     */
    @Transactional
    public Order confirm(Long id) {
        Order order = findById(id);
        order.confirm(now());

        // A fatura pode ser reemitida pelo financeiro; a confirmação não deve travar a expedição.
        try {
            Invoice invoice = invoiceFactory.createFor(order, today());
            invoiceRepository.save(invoice);
            order.setInvoice(invoice);
            log.info("Fatura {} emitida para o pedido {}", invoice.getNumber(), order.getId());
        } catch (RuntimeException e) {
            log.warn("Não foi possível emitir a fatura do pedido {}: {}", order.getId(), e.getMessage());
        }
        return order;
    }

    @Transactional
    public Order ship(Long id) {
        Order order = findById(id);
        order.ship(now());
        log.info("Pedido {} expedido", id);
        return order;
    }

    @Transactional
    public Order deliver(Long id) {
        Order order = findById(id);
        order.deliver(now());
        log.info("Pedido {} entregue", id);
        return order;
    }

    /**
     * Cancela pedidos PENDING ou CONFIRMED. O estoque reservado é devolvido
     * e a fatura (se houver e não tiver pagamentos) é cancelada.
     */
    @Transactional
    public Order cancel(Long id, String reason) {
        Order order = findById(id);
        OrderStatus previousStatus = order.getStatus();
        order.cancel(reason, now());

        switch (previousStatus) {
            case PENDING -> restoreStock(order);
            case CONFIRMED -> invoiceService.cancelInvoiceOf(order);
            default -> { }
        }

        log.info("Pedido {} cancelado (status anterior {}): {}", id, previousStatus, reason);
        return order;
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private Map<Long, Integer> consolidate(CreateOrderRequest request) {
        return request.items().stream()
                .collect(Collectors.toMap(OrderItemRequest::productId, OrderItemRequest::quantity,
                        Integer::sum, LinkedHashMap::new));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
