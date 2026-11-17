package com.jemari.profile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long> {

    Optional<FreelancerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}