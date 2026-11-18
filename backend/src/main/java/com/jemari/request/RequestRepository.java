package com.jemari.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByUmkmProfileIdAndActiveTrueOrderByCreatedAtDesc(Long umkmProfileId);

    List<ServiceRequest> findByActiveTrueOrderByCreatedAtDesc();

    List<ServiceRequest> findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus status);

    List<ServiceRequest> findByCategoryIgnoreCaseAndStatusAndActiveTrueOrderByCreatedAtDesc(String category, RequestStatus status);

    Optional<ServiceRequest> findByIdAndActiveTrue(Long id);
}
