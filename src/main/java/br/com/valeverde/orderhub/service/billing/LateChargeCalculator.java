package br.com.valeverde.orderhub.service.billing;

import br.com.valeverde.orderhub.config.BusinessProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * Encargos por atraso: multa fixa (percentual) + juros mensais pro rata die (mês comercial de 30 dias).
 */
@Component
@RequiredArgsConstructor
public class LateChargeCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal COMMERCIAL_MONTH_DAYS = new BigDecimal("30");

    private final BusinessProperties properties;

    public LateCharges calculate(BigDecimal amount, LocalDate dueDate, LocalDate paymentDate) {
        if (!paymentDate.isAfter(dueDate)) {
            return LateCharges.none();
        }

        int daysLate = Period.between(dueDate, paymentDate).getDays();
        if (daysLate <= 0) {
            return LateCharges.none();
        }

        BigDecimal lateFee = amount.multiply(properties.lateFeePercent())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal interest = amount.multiply(properties.monthlyInterestPercent())
                .multiply(BigDecimal.valueOf(daysLate))
                .divide(ONE_HUNDRED.multiply(COMMERCIAL_MONTH_DAYS), 2, RoundingMode.HALF_UP);

        return new LateCharges(daysLate, lateFee, interest);
    }
}
