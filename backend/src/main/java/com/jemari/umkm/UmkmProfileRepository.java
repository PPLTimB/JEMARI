
package com.jemari.umkm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UmkmProfileRepository extends JpaRepository<UmkmProfile, Long> {

    Optional<UmkmProfile> findByUserId(Long userId);

}
