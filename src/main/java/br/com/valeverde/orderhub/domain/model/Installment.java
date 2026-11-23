package br.com.valeverde.orderhub.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "installments")
@Getter
@Setter
@NoArgsConstructor
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private Integer number;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstallmentStatus status = InstallmentStatus.OPEN;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "late_fee", precision = 12, scale = 2)
    private BigDecimal lateFee;

    @Column(precision = 12, scale = 2)
    private BigDecimal interest;

    public Installment(int number, LocalDate dueDate, BigDecimal amount) {
        this.number = number;
        this.dueDate = dueDate;
        this.amount = amount;
        this.status = InstallmentStatus.OPEN;
    }

    public void markPaid(LocalDate paidAt, BigDecimal paidAmount, BigDecimal lateFee, BigDecimal interest) {
        this.status = InstallmentStatus.PAID;
        this.paidAt = paidAt;
        this.paidAmount = paidAmount;
        this.lateFee = lateFee;
        this.interest = interest;
    }
}
