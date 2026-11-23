package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Coupon;
import br.com.valeverde.orderhub.domain.repository.CouponRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = new Coupon();
        coupon.setCode("TESTE10");
        coupon.setDiscountPercent(new BigDecimal("10.00"));
        coupon.setValidFrom(TODAY.minusDays(10));
        coupon.setValidUntil(TODAY.plusDays(10));
        coupon.setMaxUses(100);
        coupon.setUsedCount(5);
        coupon.setMinOrderAmount(new BigDecimal("200.00"));
        coupon.setActive(true);
    }

    @Test
    @DisplayName("Cupom válido é aceito e tem a utilização registrada")
    void validCouponIsConsumed() {
        when(couponRepository.findByCodeIgnoreCase("teste10")).thenReturn(Optional.of(coupon));

        Coupon result = couponService.validateAndConsume("teste10", new BigDecimal("500.00"), TODAY);

        assertThat(result.getUsedCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cupom expirado é rejeitado")
    void expiredCouponIsRejected() {
        coupon.setValidUntil(TODAY.minusDays(1));

        assertThatThrownBy(() -> couponService.validate(coupon, new BigDecimal("500.00"), TODAY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("validade");
    }

    @Test
    @DisplayName("Cupom ainda não iniciado é rejeitado")
    void notStartedCouponIsRejected() {
        coupon.setValidFrom(TODAY.plusDays(1));

        assertThatThrownBy(() -> couponService.validate(coupon, new BigDecimal("500.00"), TODAY))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Cupom inativo é rejeitado")
    void inactiveCouponIsRejected() {
        coupon.setActive(false);

        assertThatThrownBy(() -> couponService.validate(coupon, new BigDecimal("500.00"), TODAY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("Pedido abaixo do valor mínimo do cupom é rejeitado")
    void belowMinimumAmountIsRejected() {
        assertThatThrownBy(() -> couponService.validate(coupon, new BigDecimal("199.99"), TODAY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mínimo");
    }
}
