package com.jemari.catalog.category.dto;

import com.jemari.catalog.category.ServiceCategory;
import java.time.LocalDateTime;

public record KategoriResponse(
        Long id,
        String nama,
        String deskripsi,
        Long parentId,
        String parentNama,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static KategoriResponse fromEntity(ServiceCategory entity) {
        Long parentId = entity.getParent() != null ? entity.getParent().getId() : null;
        String parentNama = entity.getParent() != null ? entity.getParent().getName() : null;
        return new KategoriResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                parentId,
                parentNama,
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
