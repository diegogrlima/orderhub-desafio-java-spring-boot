package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.service.OverdueService;
import br.com.valeverde.orderhub.web.dto.OverdueJobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class AdminController {

    private final OverdueService overdueService;
    private final Clock clock;

    /** Execução manual da rotina de vencimento (normalmente executada pelo agendador). */
    @PostMapping("/overdue-installments")
    public OverdueJobResponse runOverdueJob(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return overdueService.markOverdue(referenceDate == null ? LocalDate.now(clock) : referenceDate);
    }
}
