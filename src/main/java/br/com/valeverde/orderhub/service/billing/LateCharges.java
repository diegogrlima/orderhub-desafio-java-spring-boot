package br.com.valeverde.orderhub.service.billing;

import java.math.BigDecimal;

public record LateCharges(int daysLate, BigDecimal lateFee, BigDecimal interest) {

    public static LateCharges none() {
        return new LateCharges(0, BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2));
    }

    public BigDecimal total() {
        return lateFee.add(interest);
    }
}
