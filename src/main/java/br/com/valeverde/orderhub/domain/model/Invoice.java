package br.com.valeverde.orderhub.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String number;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number ASC")
    private List<Installment> installments = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addInstallment(Installment installment) {
        installment.setInvoice(this);
        installments.add(installment);
    }

    public void cancel() {
        status = InvoiceStatus.CANCELLED;
        installments.forEach(i -> i.setStatus(InstallmentStatus.CANCELLED));
    }

    /** Recalcula o status da fatura a partir das parcelas. */
    public void refreshStatus() {
        if (status == InvoiceStatus.CANCELLED) {
            return;
        }
        boolean allPaid = installments.stream().allMatch(i -> i.getStatus() == InstallmentStatus.PAID);
        boolean anyOverdue = installments.stream().anyMatch(i -> i.getStatus() == InstallmentStatus.OVERDUE);
        if (allPaid) {
            status = InvoiceStatus.PAID;
        } else if (anyOverdue) {
            status = InvoiceStatus.OVERDUE;
        } else {
            status = InvoiceStatus.OPEN;
        }
    }
}
