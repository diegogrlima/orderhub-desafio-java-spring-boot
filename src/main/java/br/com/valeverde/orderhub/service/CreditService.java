package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.domain.repository.InstallmentRepository;
import br.com.valeverde.orderhub.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exposição de crédito = parcelas não liquidadas + pedidos pendentes (ainda não faturados).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditService {

    private final InstallmentRepository installmentRepository;
    private final OrderRepository orderRepository;

    public CreditSummary summarize(Customer customer) {
        return new CreditSummary(
                customer.getId(),
                customer.getCreditLimit(),
                installmentRepository.sumOpenAmountByCustomer(customer.getId()),
                orderRepository.sumPendingTotalByCustomer(customer.getId()));
    }
}
