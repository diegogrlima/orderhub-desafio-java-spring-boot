package br.com.valeverde.orderhub.web.mapper;

import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.service.CreditSummary;
import br.com.valeverde.orderhub.web.dto.CreditSummaryResponse;
import br.com.valeverde.orderhub.web.dto.CustomerResponse;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getLegalName(), c.getTradeName(), c.getDocument(), c.getEmail(),
                c.getPhone(), c.getCity(), c.getState(), c.getTier(), c.getCreditLimit(), c.getBillingDay(),
                c.isActive(), c.getCreatedAt(), c.getUpdatedAt());
    }

    public static CreditSummaryResponse toResponse(CreditSummary s) {
        return new CreditSummaryResponse(s.customerId(), s.creditLimit(), s.openInstallments(), s.pendingOrders(),
                s.exposure(), s.availableCredit());
    }
}
