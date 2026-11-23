package br.com.valeverde.orderhub.service.billing;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Calendário de dias úteis. Feriados ainda não são considerados (ver backlog FIN-112).
 */
@Component
public class BusinessCalendar {

    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /** Retorna a própria data se for dia útil, ou o próximo dia útil. */
    public LocalDate nextBusinessDay(LocalDate date) {
        LocalDate result = date;
        while (!isBusinessDay(result)) {
            result = result.plusDays(1);
        }
        return result;
    }
}
