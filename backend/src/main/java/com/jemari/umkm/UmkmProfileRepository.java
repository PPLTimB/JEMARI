package com.jemari.umkm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UmkmProfileRepository extends JpaRepository<UmkmProfile, Long> {
    Optional<UmkmProfile> findByUserId(Long userId);
    Optional<UmkmProfile> findByUserEmail(String email);
    boolean existsByUserId(Long userId);
}