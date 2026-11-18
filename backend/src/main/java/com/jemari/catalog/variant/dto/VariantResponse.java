package com.jemari.catalog.variant.dto;

import com.jemari.catalog.variant.entity.ServicePackageVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response untuk detail varian paket jasa sisi Freelancer.
 */
public record VariantResponse(
    Long id,
    Long packageId,
    String name,
    String description,
    BigDecimal price,
    Integer estimatedDays,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static VariantResponse fromEntity(ServicePackageVariant entity) {
        return new VariantResponse(
            entity.getId(),
            entity.getPackageId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getEstimatedDays(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
