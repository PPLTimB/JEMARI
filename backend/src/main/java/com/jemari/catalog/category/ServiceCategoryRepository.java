package com.jemari.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    boolean existsByNameIgnoreCaseAndParent(String name, ServiceCategory parent);

    boolean existsByNameIgnoreCaseAndParentIsNull(String name);

    boolean existsByNameIgnoreCaseAndParentAndIdNot(String name, ServiceCategory parent, Long id);

    boolean existsByNameIgnoreCaseAndParentIsNullAndIdNot(String name, Long id);

    boolean existsByParentId(Long parentId);

    List<ServiceCategory> findAllByParentId(Long parentId);

    List<ServiceCategory> findAllByActiveTrueAndParentIsNull();

    List<ServiceCategory> findAllByActiveTrue();
}

