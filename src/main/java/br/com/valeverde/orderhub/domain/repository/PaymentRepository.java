package br.com.valeverde.orderhub.domain.repository;

import br.com.valeverde.orderhub.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByInstallmentInvoiceId(Long invoiceId);

    List<Payment> findByInstallmentId(Long installmentId);
}
