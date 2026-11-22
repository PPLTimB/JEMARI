package com.jemari.profile.repository;

import com.jemari.profile.entity.FreelancerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FreelancerSkillRepository extends JpaRepository<FreelancerSkill, Long> {

    List<FreelancerSkill> findByFreelancerProfileIdOrderByCreatedAtAsc(Long freelancerProfileId);

    boolean existsByFreelancerProfileIdAndSkillName(Long freelancerProfileId, String skillName);

    long countByFreelancerProfileId(Long freelancerProfileId);
}