package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Invoice;
import br.com.valeverde.orderhub.domain.model.InvoiceStatus;
import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.repository.InvoiceRepository;
import br.com.valeverde.orderhub.domain.repository.PaymentRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura", id));
    }

    public Invoice findByOrder(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura do pedido", orderId));
    }

    public Page<Invoice> list(Long customerId, InvoiceStatus status, Pageable pageable) {
        if (customerId != null && status != null) {
            return invoiceRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        }
        if (customerId != null) {
            return invoiceRepository.findByCustomerId(customerId, pageable);
        }
        if (status != null) {
            return invoiceRepository.findByStatus(status, pageable);
        }
        return invoiceRepository.findAll(pageable);
    }

    /**
     * Cancela a fatura de um pedido. Faturas com qualquer pagamento registrado não podem ser canceladas.
     */
    @Transactional
    public void cancelInvoiceOf(Order order) {
        invoiceRepository.findByOrderId(order.getId()).ifPresent(invoice -> {
            if (paymentRepository.existsByInstallmentInvoiceId(invoice.getId())) {
                throw new BusinessException("A fatura " + invoice.getNumber()
                        + " possui pagamentos registrados; o pedido não pode ser cancelado");
            }
            invoice.cancel();
            log.info("Fatura {} cancelada junto com o pedido {}", invoice.getNumber(), order.getId());
        });
    }
}
