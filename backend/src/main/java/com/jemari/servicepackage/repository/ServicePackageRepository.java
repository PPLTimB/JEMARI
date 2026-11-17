package com.jemari.servicepackage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jemari.servicepackage.entity.ServicePackage;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {

    List<ServicePackage> findByIsActiveTrue();

    List<ServicePackage> findByFreelancerIdAndIsActiveTrue(Long freelancerId);

    Optional<ServicePackage> findByIdAndIsActiveTrue(Long id);
}