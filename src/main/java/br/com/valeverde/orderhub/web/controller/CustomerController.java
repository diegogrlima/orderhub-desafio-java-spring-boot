package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.service.CreditService;
import br.com.valeverde.orderhub.service.CustomerService;
import br.com.valeverde.orderhub.web.dto.*;
import br.com.valeverde.orderhub.web.mapper.CustomerMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CreditService creditService;

    @GetMapping
    public PageResponse<CustomerResponse> search(@RequestParam(required = false) String search,
                                                 @PageableDefault(size = 20, sort = "legalName") Pageable pageable) {
        return PageResponse.of(customerService.search(search, pageable), CustomerMapper::toResponse);
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return CustomerMapper.toResponse(customerService.findById(id));
    }

    @GetMapping("/{id}/credit")
    public CreditSummaryResponse credit(@PathVariable Long id) {
        Customer customer = customerService.findById(id);
        return CustomerMapper.toResponse(creditService.summarize(customer));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer customer = customerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(customer.getId()).toUri();
        return ResponseEntity.created(location).body(CustomerMapper.toResponse(customer));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        return CustomerMapper.toResponse(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
