package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.service.ReportService;
import br.com.valeverde.orderhub.web.dto.CustomerRevenueResponse;
import br.com.valeverde.orderhub.web.dto.SalesReportResponse;
import br.com.valeverde.orderhub.web.dto.TopProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public SalesReportResponse sales(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.salesReport(from, to);
    }

    @GetMapping("/revenue-by-customer")
    public List<CustomerRevenueResponse> revenueByCustomer(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.revenueByCustomer(from, to);
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                @RequestParam(defaultValue = "10") int limit) {
        return reportService.topProducts(from, to, limit);
    }
}
