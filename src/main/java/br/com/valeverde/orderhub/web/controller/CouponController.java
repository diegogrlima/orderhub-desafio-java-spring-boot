package br.com.valeverde.orderhub.web.controller;

import br.com.valeverde.orderhub.service.CouponService;
import br.com.valeverde.orderhub.web.dto.CouponRequest;
import br.com.valeverde.orderhub.web.dto.CouponResponse;
import br.com.valeverde.orderhub.web.mapper.CatalogMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public List<CouponResponse> list() {
        return couponService.listAll().stream().map(CatalogMapper::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return CatalogMapper.toResponse(couponService.create(request));
    }
}
