package br.com.valeverde.orderhub.service.billing;

import br.com.valeverde.orderhub.domain.model.Installment;
import br.com.valeverde.orderhub.domain.model.Invoice;
import br.com.valeverde.orderhub.domain.model.InvoiceStatus;
import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Monta a fatura (e suas parcelas) de um pedido confirmado. Não persiste.
 */
@Component
@RequiredArgsConstructor
public class InvoiceFactory {

    private final InstallmentScheduler scheduler;
    private final InvoiceRepository invoiceRepository;

    public Invoice createFor(Order order, LocalDate issueDate) {
        List<ScheduledInstallment> schedule = scheduler.schedule(
                order.getTotalAmount(),
                order.getInstallments(),
                issueDate,
                order.getCustomer().getBillingDay());

        Invoice invoice = new Invoice();
        invoice.setNumber(formatNumber(issueDate, invoiceRepository.nextInvoiceSequence()));
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setIssueDate(issueDate);
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setStatus(InvoiceStatus.OPEN);
        schedule.forEach(s -> invoice.addInstallment(new Installment(s.number(), s.dueDate(), s.amount())));
        return invoice;
    }

    private String formatNumber(LocalDate issueDate, long sequence) {
        return "NF-%d-%06d".formatted(issueDate.getYear(), sequence);
    }
}
