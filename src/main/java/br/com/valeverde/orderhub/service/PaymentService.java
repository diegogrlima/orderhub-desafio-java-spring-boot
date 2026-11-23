package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.*;
import br.com.valeverde.orderhub.domain.repository.InstallmentRepository;
import br.com.valeverde.orderhub.domain.repository.PaymentRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.exception.ResourceNotFoundException;
import br.com.valeverde.orderhub.service.billing.LateChargeCalculator;
import br.com.valeverde.orderhub.service.billing.LateCharges;
import br.com.valeverde.orderhub.web.dto.PaymentRequest;
import br.com.valeverde.orderhub.web.dto.PayoffResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final LateChargeCalculator lateChargeCalculator;
    private final Clock clock;

    public Installment findInstallment(Long id) {
        return installmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", id));
    }

    /** Simula o valor para quitação da parcela em uma data. */
    public PayoffResponse payoff(Long installmentId, LocalDate paymentDate) {
        Installment installment = findInstallment(installmentId);
        LocalDate date = paymentDate == null ? LocalDate.now(clock) : paymentDate;
        LateCharges charges = lateChargeCalculator.calculate(installment.getAmount(), installment.getDueDate(), date);
        return new PayoffResponse(installment.getId(), installment.getDueDate(), date, installment.getAmount(),
                charges.daysLate(), charges.lateFee(), charges.interest(),
                installment.getAmount().add(charges.total()));
    }

    /**
     * Registra a quitação integral de uma parcela (valor + encargos).
     */
    @Transactional
    public Payment register(Long installmentId, PaymentRequest request, String username) {
        Installment installment = findInstallment(installmentId);
        Invoice invoice = installment.getInvoice();

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new BusinessException("Parcela " + installment.getNumber() + " da fatura " + invoice.getNumber() + " já está quitada");
        }
        if (installment.getStatus() == InstallmentStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Parcela pertence a uma fatura cancelada");
        }
        if (request.paymentDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessException("Data de pagamento não pode estar no futuro");
        }
        if (request.paymentDate().isBefore(invoice.getIssueDate())) {
            throw new BusinessException("Data de pagamento anterior à emissão da fatura");
        }

        LateCharges charges = lateChargeCalculator.calculate(
                installment.getAmount(), installment.getDueDate(), request.paymentDate());
        BigDecimal due = installment.getAmount().add(charges.total());

        if (request.amount().subtract(due).abs().compareTo(TOLERANCE) > 0) {
            throw new BusinessException("Valor informado (R$ " + request.amount() + ") diverge do valor devido (R$ " + due + ")");
        }

        installment.markPaid(request.paymentDate(), request.amount(), charges.lateFee(), charges.interest());

        Payment payment = new Payment();
        payment.setInstallment(installment);
        payment.setAmount(request.amount());
        payment.setPaymentDate(request.paymentDate());
        payment.setMethod(request.method());
        payment.setRegisteredBy(username);
        paymentRepository.save(payment);

        invoice.refreshStatus();
        log.info("Pagamento de R$ {} registrado na parcela {} da fatura {} por {}",
                request.amount(), installment.getNumber(), invoice.getNumber(), username);
        return payment;
    }
}
