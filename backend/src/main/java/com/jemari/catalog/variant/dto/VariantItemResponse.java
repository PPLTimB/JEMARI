package com.jemari.catalog.variant.dto;

import com.jemari.catalog.variant.entity.ServicePackageVariant;

import java.math.BigDecimal;

/**
 * DTO item varian dalam rincian biaya paket untuk sisi UMKM (UC-03).
 */
public record VariantItemResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer estimatedDays
) {
    public static VariantItemResponse fromEntity(ServicePackageVariant entity) {
        return new VariantItemResponse(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getEstimatedDays()
        );
    }
}
