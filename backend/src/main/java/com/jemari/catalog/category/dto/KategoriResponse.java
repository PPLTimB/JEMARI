package com.jemari.catalog.category.dto;

import com.jemari.catalog.category.KategoriJasa;
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
    public static KategoriResponse fromEntity(KategoriJasa entity) {
        Long parentId = entity.getParent() != null ? entity.getParent().getId() : null;
        String parentNama = entity.getParent() != null ? entity.getParent().getNama() : null;
        return new KategoriResponse(
                entity.getId(),
                entity.getNama(),
                entity.getDeskripsi(),
                parentId,
                parentNama,
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
