package com.jemari.profile.repository;

import com.jemari.profile.entity.FreelancerExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FreelancerExperienceRepository extends JpaRepository<FreelancerExperience, Long> {

    List<FreelancerExperience> findByFreelancerProfileIdOrderByStartDateDesc(Long freelancerProfileId);

    long countByFreelancerProfileId(Long freelancerProfileId);
}