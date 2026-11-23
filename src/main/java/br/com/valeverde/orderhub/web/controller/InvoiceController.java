package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.domain.model.InvoiceStatus;
import br.com.valeverde.orderhub.service.InvoiceService;
import br.com.valeverde.orderhub.web.dto.InvoiceResponse;
import br.com.valeverde.orderhub.web.dto.InvoiceSummaryResponse;
import br.com.valeverde.orderhub.web.dto.PageResponse;
import br.com.valeverde.orderhub.web.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public PageResponse<InvoiceSummaryResponse> list(@RequestParam(required = false) Long customerId,
                                                     @RequestParam(required = false) InvoiceStatus status,
                                                     @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC)
                                                     Pageable pageable) {
        return PageResponse.of(invoiceService.list(customerId, status, pageable), InvoiceMapper::toSummary);
    }

    @GetMapping("/{id}")
    public InvoiceResponse findById(@PathVariable Long id) {
        return InvoiceMapper.toResponse(invoiceService.findById(id));
    }

    @GetMapping("/by-order/{orderId}")
    public InvoiceResponse findByOrder(@PathVariable Long orderId) {
        return InvoiceMapper.toResponse(invoiceService.findByOrder(orderId));
    }
}
