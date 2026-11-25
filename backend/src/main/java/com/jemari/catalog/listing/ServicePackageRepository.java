package com.jemari.catalog.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long>, JpaSpecificationExecutor<ServicePackage> {

    boolean existsByCategoryId(Long categoryId);

    long countByCategoryId(Long categoryId);
}

