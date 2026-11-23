package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.model.OrderStatus;
import br.com.valeverde.orderhub.service.OrderService;
import br.com.valeverde.orderhub.web.dto.*;
import br.com.valeverde.orderhub.web.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public PageResponse<OrderSummaryResponse> list(@RequestParam(required = false) Long customerId,
                                                   @RequestParam(required = false) OrderStatus status,
                                                   @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                                   Pageable pageable) {
        return PageResponse.of(orderService.list(customerId, status, pageable), OrderMapper::toSummary);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return OrderMapper.toResponse(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(OrderMapper.toResponse(order));
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable Long id) {
        return OrderMapper.toResponse(orderService.confirm(id));
    }

    @PostMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable Long id) {
        return OrderMapper.toResponse(orderService.ship(id));
    }

    @PostMapping("/{id}/deliver")
    public OrderResponse deliver(@PathVariable Long id) {
        return OrderMapper.toResponse(orderService.deliver(id));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelOrderRequest request) {
        return OrderMapper.toResponse(orderService.cancel(id, request.reason()));
    }
}
