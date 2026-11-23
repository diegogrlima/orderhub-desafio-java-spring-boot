package br.com.valeverde.orderhub.service.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InstallmentSchedulerTest {

    private final InstallmentScheduler scheduler = new InstallmentScheduler(new BusinessCalendar());

    @Test
    @DisplayName("Parcela única corresponde ao total da fatura")
    void singleInstallmentEqualsTotal() {
        List<ScheduledInstallment> result = scheduler.schedule(new BigDecimal("349.90"), 1, LocalDate.of(2026, 3, 2), null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().amount()).isEqualByComparingTo("349.90");
        assertThat(result.getFirst().dueDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("Vencimento em sábado é prorrogado para segunda-feira")
    void dueDateOnSaturdayMovesToMonday() {
        // 01/01/2026 + 30 dias = 31/01/2026 (sábado)
        List<ScheduledInstallment> result = scheduler.schedule(new BigDecimal("100.00"), 1, LocalDate.of(2026, 1, 1), null);

        assertThat(result.getFirst().dueDate()).isEqualTo(LocalDate.of(2026, 2, 2));
    }

    @Test
    @DisplayName("Dia de vencimento preferencial do cliente é respeitado")
    void billingDayIsRespected() {
        List<ScheduledInstallment> result = scheduler.schedule(new BigDecimal("200.00"), 2, LocalDate.of(2026, 3, 10), 15);

        assertThat(result).extracting(ScheduledInstallment::dueDate)
                .containsExactly(LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 15));
    }

    @Test
    @DisplayName("Soma das parcelas é igual ao total; centavos residuais vão para a 1ª parcela")
    void installmentsAddUpToInvoiceTotal() {
        List<ScheduledInstallment> result = scheduler.schedule(new BigDecimal("100.00"), 3, LocalDate.of(2026, 3, 2), null);

        BigDecimal sum = result.stream().map(ScheduledInstallment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("100.00");
        assertThat(result.getFirst().amount()).isEqualByComparingTo("33.34");
    }
}
