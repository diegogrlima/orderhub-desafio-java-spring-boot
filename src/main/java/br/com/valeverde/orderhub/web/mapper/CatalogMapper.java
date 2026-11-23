package br.com.valeverde.orderhub.web.mapper;

import br.com.valeverde.orderhub.domain.model.Category;
import br.com.valeverde.orderhub.domain.model.Coupon;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.web.dto.CategoryResponse;
import br.com.valeverde.orderhub.web.dto.CouponResponse;
import br.com.valeverde.orderhub.web.dto.ProductResponse;

public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getCategory().getId(),
                p.getCategory().getName(), p.getUnit(), p.getPrice(), p.getStockQuantity(), p.isActive());
    }

    public static CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }

    public static CouponResponse toResponse(Coupon c) {
        return new CouponResponse(c.getId(), c.getCode(), c.getDescription(), c.getDiscountPercent(),
                c.getValidFrom(), c.getValidUntil(), c.getMaxUses(), c.getUsedCount(), c.getMinOrderAmount(),
                c.isActive());
    }
}
