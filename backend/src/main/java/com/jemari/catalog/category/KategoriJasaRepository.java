package com.jemari.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KategoriJasaRepository extends JpaRepository<KategoriJasa, Long> {

    boolean existsByNamaIgnoreCaseAndParent(String nama, KategoriJasa parent);

    boolean existsByNamaIgnoreCaseAndParentIsNull(String nama);

    boolean existsByNamaIgnoreCaseAndParentAndIdNot(String nama, KategoriJasa parent, Long id);

    boolean existsByNamaIgnoreCaseAndParentIsNullAndIdNot(String nama, Long id);

    boolean existsByParentId(Long parentId);

    List<KategoriJasa> findAllByParentId(Long parentId);

    List<KategoriJasa> findAllByActiveTrueAndParentIsNull();

    List<KategoriJasa> findAllByActiveTrue();
}
