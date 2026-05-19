package com.princesses7.findy.shopping.product.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record ProductDetailResponse(
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
        com.princesses7.findy.shopping.product.entity.SaleStatus saleStatus
) {

    public static ProductDetailResponse from(com.princesses7.findy.shopping.product.entity.Product product) {
        return new ProductDetailResponse(
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
                product.getSaleStatus()
        );
    }
}
