package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.domain.repository.CustomerRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.web.dto.CustomerRequest;
import br.com.valeverde.orderhub.web.dto.CustomerUpdateRequest;
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
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Page<Customer> search(String term, Pageable pageable) {
        if (term == null || term.isBlank()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.search(term.trim(), pageable);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Customer create(CustomerRequest request) {
        String document = normalizeDocument(request.document());
        String email = normalizeEmail(request.email());

        if (customerRepository.existsByDocument(document)) {
            throw new BusinessException("Já existe um cliente cadastrado com o CNPJ " + document);
        }
        if (customerRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail já cadastrado para outro cliente: " + email);
        }

        Customer customer = new Customer();
        customer.setDocument(document);
        customer.setEmail(email);
        customer.setLegalName(request.legalName().trim());
        customer.setTradeName(request.tradeName());
        customer.setPhone(request.phone());
        customer.setCity(request.city().trim());
        customer.setState(request.state());
        customer.setTier(request.tier());
        customer.setCreditLimit(request.creditLimit());
        customer.setBillingDay(request.billingDay());

        Customer saved = customerRepository.save(customer);
        log.info("Cliente {} cadastrado (CNPJ {})", saved.getId(), saved.getDocument());
        return saved;
    }

    @Transactional
    public Customer update(Long id, CustomerUpdateRequest request) {
        Customer customer = findById(id);
        String email = normalizeEmail(request.email());

        if (customerRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail já cadastrado para outro cliente: " + email);
        }

        customer.setEmail(email);
        customer.setLegalName(request.legalName().trim());
        customer.setTradeName(request.tradeName());
        customer.setPhone(request.phone());
        customer.setCity(request.city().trim());
        customer.setState(request.state());
        customer.setTier(request.tier());
        customer.setCreditLimit(request.creditLimit());
        customer.setBillingDay(request.billingDay());

        log.info("Cliente {} atualizado", id);
        return customer;
    }

    @Transactional
    public void deactivate(Long id) {
        Customer customer = findById(id);
        customer.setActive(false);
        log.info("Cliente {} inativado", id);
    }

    private String normalizeDocument(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 14) {
            throw new BusinessException("CNPJ deve conter 14 dígitos");
        }
        return digits;
    }

    private String normalizeEmail(String raw) {
        return raw.trim().toLowerCase();
    }
}
