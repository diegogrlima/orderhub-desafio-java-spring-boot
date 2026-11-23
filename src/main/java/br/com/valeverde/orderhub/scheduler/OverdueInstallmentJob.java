package br.com.valeverde.orderhub.scheduler;

import br.com.valeverde.orderhub.service.OverdueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Rotina diária que marca parcelas vencidas (executa logo após a meia-noite, horário de Brasília).
 */
@Component
@RequiredArgsConstructor
public class OverdueInstallmentJob {

    private final OverdueService overdueService;
    private final Clock clock;

    @Scheduled(cron = "${orderhub.jobs.overdue-cron}", zone = "America/Sao_Paulo")
    public void run() {
        overdueService.markOverdue(LocalDate.now(clock));
    }
}
