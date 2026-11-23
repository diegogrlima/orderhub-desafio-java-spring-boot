package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Coupon;
import br.com.valeverde.orderhub.domain.repository.CouponRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.web.dto.CouponRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> listAll() {
        return couponRepository.findAll(Sort.by(Sort.Direction.DESC, "validUntil"));
    }

    @Transactional
    public Coupon create(CouponRequest request) {
        if (request.validUntil().isBefore(request.validFrom())) {
            throw new BusinessException("Data final da vigência anterior à data inicial");
        }
        if (couponRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Cupom já existente: " + request.code());
        }
        Coupon coupon = new Coupon();
        coupon.setCode(request.code().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountPercent(request.discountPercent());
        coupon.setValidFrom(request.validFrom());
        coupon.setValidUntil(request.validUntil());
        coupon.setMaxUses(request.maxUses());
        coupon.setMinOrderAmount(request.minOrderAmount() == null ? BigDecimal.ZERO : request.minOrderAmount());
        return couponRepository.save(coupon);
    }

    /**
     * Valida o cupom para o pedido e registra a utilização.
     * Deve ser chamado dentro da transação de criação do pedido.
     */
    @Transactional
    public Coupon validateAndConsume(String code, BigDecimal orderSubtotal, LocalDate today) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException("Cupom inválido: " + code));
        validate(coupon, orderSubtotal, today);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        return coupon;
    }

    void validate(Coupon coupon, BigDecimal orderSubtotal, LocalDate today) {
        if (!coupon.isActive()) {
            throw new BusinessException("Cupom " + coupon.getCode() + " está inativo");
        }
        boolean withinPeriod = !today.isBefore(coupon.getValidFrom()) && today.isBefore(coupon.getValidUntil());
        if (!withinPeriod) {
            throw new BusinessException("Cupom " + coupon.getCode() + " fora do período de validade");
        }
        if (coupon.getUsedCount() > coupon.getMaxUses()) {
            throw new BusinessException("Cupom " + coupon.getCode() + " esgotado");
        }
        if (orderSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException("Pedido não atinge o valor mínimo do cupom (R$ " + coupon.getMinOrderAmount() + ")");
        }
    }
}
