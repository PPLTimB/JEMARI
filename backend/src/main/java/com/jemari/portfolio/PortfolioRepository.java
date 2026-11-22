package com.jemari.portfolio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByFreelancerProfileIdAndActiveTrueOrderByCreatedAtDesc(Long freelancerProfileId);

    Optional<Portfolio> findByIdAndActiveTrue(Long id);

    List<Portfolio> findByFreelancerProfileUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);
}
