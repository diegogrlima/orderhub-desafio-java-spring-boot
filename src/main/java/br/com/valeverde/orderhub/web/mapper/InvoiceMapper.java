package br.com.valeverde.orderhub.web.mapper;

import br.com.valeverde.orderhub.domain.model.Installment;
import br.com.valeverde.orderhub.domain.model.Invoice;
import br.com.valeverde.orderhub.domain.model.Payment;
import br.com.valeverde.orderhub.web.dto.InstallmentResponse;
import br.com.valeverde.orderhub.web.dto.InvoiceResponse;
import br.com.valeverde.orderhub.web.dto.InvoiceSummaryResponse;
import br.com.valeverde.orderhub.web.dto.PaymentResponse;

public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static InvoiceResponse toResponse(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getNumber(), i.getOrder().getId(), i.getCustomer().getId(),
                i.getCustomer().getLegalName(), i.getIssueDate(), i.getTotalAmount(), i.getStatus(),
                i.getInstallments().stream().map(InvoiceMapper::toResponse).toList());
    }

    public static InvoiceSummaryResponse toSummary(Invoice i) {
        return new InvoiceSummaryResponse(i.getId(), i.getNumber(), i.getOrder().getId(), i.getCustomer().getId(),
                i.getIssueDate(), i.getTotalAmount(), i.getStatus());
    }

    public static InstallmentResponse toResponse(Installment i) {
        return new InstallmentResponse(i.getId(), i.getNumber(), i.getDueDate(), i.getAmount(), i.getStatus(),
                i.getPaidAt(), i.getPaidAmount(), i.getLateFee(), i.getInterest());
    }

    public static PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getInstallment().getId(), p.getAmount(), p.getPaymentDate(),
                p.getMethod(), p.getRegisteredBy(), p.getInstallment().getStatus(),
                p.getInstallment().getInvoice().getStatus());
    }
}
