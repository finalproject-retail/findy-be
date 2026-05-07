package com.princesses7.findy.product.domain.product.dto.response;

import java.math.BigDecimal;
import com.princesses7.findy.product.domain.product.entity.Product;
import com.princesses7.findy.product.domain.product.entity.SaleStatus;

public record ProductResponse(
        Long productId,
        Long categoryId,
        String brandName,
        String productName,
        String barcode,
        Integer originalPrice,
        Integer salePrice,
        BigDecimal discountRate,
        String description,
        String imageUrl,
        String packagingType,
        String salesUnit,
        String volume,
        String allergyInfo,
        String badgeText,
        SaleStatus saleStatus
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getCategoryId(),
                product.getBrandName(),
                product.getProductName(),
                product.getBarcode(),
                product.getOriginalPrice(),
                product.getSalePrice(),
                product.getDiscountRate(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPackagingType(),
                product.getSalesUnit(),
                product.getVolume(),
                product.getAllergyInfo(),
                product.getBadgeText(),
                product.getSaleStatus()
        );
    }
}