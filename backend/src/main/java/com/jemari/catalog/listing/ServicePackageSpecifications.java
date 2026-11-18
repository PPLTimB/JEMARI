package com.jemari.catalog.listing;

import com.jemari.user.FreelancerProfile;
import com.jemari.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ServicePackageSpecifications {

    private ServicePackageSpecifications() {
    }

    public static Specification<ServicePackage> buildBaseSpecification(
            String keyword,
            List<Long> categoryIds,
            BigDecimal hargaMin,
            BigDecimal hargaMax
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Paket aktif
            predicates.add(cb.isTrue(root.get("active")));

            // 2. Freelancer user aktif
            Join<ServicePackage, FreelancerProfile> freelancerJoin = root.join("freelancerProfile", JoinType.INNER);
            Join<FreelancerProfile, User> userJoin = freelancerJoin.join("user", JoinType.INNER);
            predicates.add(cb.isTrue(userJoin.get("active")));

            // 3. Keyword pada title dan description (case-insensitive)
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, descLike));
            }

            // 4. Filter Kategori (mencakup parent dan sub-kategori)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // 5. Filter Rentang Harga (base_price)
            if (hargaMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), hargaMin));
            }
            if (hargaMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), hargaMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ServicePackage> isNewFreelancerPool(boolean isNew) {
        return (root, query, cb) -> {
            Join<ServicePackage, FreelancerProfile> fp = root.join("freelancerProfile", JoinType.INNER);

            Predicate fullNameValid = cb.and(cb.isNotNull(fp.get("fullName")), cb.notEqual(cb.trim(fp.get("fullName")), ""));
            Predicate bioValid = cb.and(cb.isNotNull(fp.get("bio")), cb.notEqual(cb.trim(fp.get("bio")), ""));
            Predicate headlineValid = cb.and(cb.isNotNull(fp.get("headline")), cb.notEqual(cb.trim(fp.get("headline")), ""));
            Predicate cityValid = cb.and(cb.isNotNull(fp.get("city")), cb.notEqual(cb.trim(fp.get("city")), ""));

            Predicate profileComplete = cb.and(fullNameValid, bioValid, headlineValid, cityValid);

            return isNew ? profileComplete : cb.not(profileComplete);
        };
    }
}

