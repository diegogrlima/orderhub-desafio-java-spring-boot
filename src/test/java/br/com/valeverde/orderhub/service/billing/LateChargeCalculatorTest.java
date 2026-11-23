package br.com.valeverde.orderhub.service.billing;

import br.com.valeverde.orderhub.support.TestProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LateChargeCalculatorTest {

    private final LateChargeCalculator calculator = new LateChargeCalculator(TestProperties.defaults());

    @Test
    @DisplayName("Pagamento no vencimento não gera encargos")
    void noChargesWhenPaidOnDueDate() {
        LateCharges charges = calculator.calculate(new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10));

        assertThat(charges.daysLate()).isZero();
        assertThat(charges.total()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Pagamento antecipado não gera encargos")
    void noChargesWhenPaidEarly() {
        LateCharges charges = calculator.calculate(new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 5));

        assertThat(charges.total()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("10 dias de atraso: multa de 2% + juros de 1% a.m. pro rata")
    void tenDaysLate() {
        LateCharges charges = calculator.calculate(new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 20));

        assertThat(charges.daysLate()).isEqualTo(10);
        assertThat(charges.lateFee()).isEqualByComparingTo("20.00");
        assertThat(charges.interest()).isEqualByComparingTo("3.33");
    }

    @Test
    @DisplayName("45 dias de atraso: juros proporcionais a todos os dias corridos")
    void fortyFiveDaysLate() {
        LateCharges charges = calculator.calculate(new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 4, 24));

        assertThat(charges.daysLate()).isEqualTo(45);
        assertThat(charges.lateFee()).isEqualByComparingTo("20.00");
        assertThat(charges.interest()).isEqualByComparingTo("15.00");
    }
}
