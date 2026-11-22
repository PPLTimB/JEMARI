package com.jemari.request.repository;

import com.jemari.request.entity.RequestReferencePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestReferencePhotoRepository extends JpaRepository<RequestReferencePhoto, Long> {
    // Mengambil foto berdasarkan requestId, diurutkan berdasarkan sort_order
    List<RequestReferencePhoto> findByRequestIdOrderBySortOrderAsc(Long requestId);
    
    // Menghitung jumlah foto untuk validasi maksimal 5
    long countByRequestId(Long requestId);
}