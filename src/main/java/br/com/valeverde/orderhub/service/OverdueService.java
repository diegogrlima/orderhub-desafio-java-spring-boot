package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.InstallmentStatus;
import br.com.valeverde.orderhub.domain.repository.InstallmentRepository;
import br.com.valeverde.orderhub.domain.repository.InvoiceRepository;
import br.com.valeverde.orderhub.web.dto.OverdueJobResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdueService {

    private final InstallmentRepository installmentRepository;
    private final InvoiceRepository invoiceRepository;

    /** Marca como vencidas as parcelas em aberto com vencimento anterior à data de referência. */
    @Transactional
    public OverdueJobResponse markOverdue(LocalDate referenceDate) {
        int installments = installmentRepository.markOverdue(referenceDate, InstallmentStatus.OPEN, InstallmentStatus.OVERDUE);
        int invoices = invoiceRepository.markInvoicesWithOverdueInstallments();
        log.info("Rotina de vencimento ({}): {} parcelas e {} faturas marcadas como OVERDUE",
                referenceDate, installments, invoices);
        return new OverdueJobResponse(referenceDate, installments, invoices);
    }
}
