package br.com.valeverde.orderhub.service.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta o cronograma de parcelas de uma fatura.
 */
@Component
@RequiredArgsConstructor
public class InstallmentScheduler {

    private final BusinessCalendar calendar;

    public List<ScheduledInstallment> schedule(BigDecimal total, int count, LocalDate issueDate, Integer billingDay) {
        if (count < 1) {
            throw new IllegalArgumentException("Quantidade de parcelas inválida: " + count);
        }
        BigDecimal amount = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);

        List<ScheduledInstallment> result = new ArrayList<>(count);
        for (int n = 1; n <= count; n++) {
            result.add(new ScheduledInstallment(n, dueDateFor(issueDate, n, billingDay), amount));
        }
        return result;
    }

    LocalDate dueDateFor(LocalDate issueDate, int installmentNumber, Integer billingDay) {
        LocalDate due;
        if (billingDay == null) {
            due = issueDate.plusDays(30L * installmentNumber);
        } else {
            YearMonth month = YearMonth.from(issueDate).plusMonths(installmentNumber);
            due = LocalDate.of(month.getYear(), month.getMonth(), billingDay);
        }
        return calendar.nextBusinessDay(due);
    }
}
