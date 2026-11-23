package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.service.PaymentService;
import br.com.valeverde.orderhub.web.dto.InstallmentResponse;
import br.com.valeverde.orderhub.web.dto.PaymentRequest;
import br.com.valeverde.orderhub.web.dto.PaymentResponse;
import br.com.valeverde.orderhub.web.dto.PayoffResponse;
import br.com.valeverde.orderhub.web.mapper.InvoiceMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/installments")
@RequiredArgsConstructor
public class InstallmentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public InstallmentResponse findById(@PathVariable Long id) {
        return InvoiceMapper.toResponse(paymentService.findInstallment(id));
    }

    @GetMapping("/{id}/payoff")
    public PayoffResponse payoff(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return paymentService.payoff(id, date);
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@PathVariable Long id, @Valid @RequestBody PaymentRequest request, Principal principal) {
        return InvoiceMapper.toResponse(paymentService.register(id, request, principal.getName()));
    }
}
