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

public final class PaketJasaSpecifications {

    private PaketJasaSpecifications() {
    }

    public static Specification<PaketJasa> buildBaseSpecification(
            String keyword,
            List<Long> kategoriIds,
            BigDecimal hargaMin,
            BigDecimal hargaMax
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Paket aktif
            predicates.add(cb.isTrue(root.get("active")));

            // 2. Freelancer user aktif
            Join<PaketJasa, FreelancerProfile> freelancerJoin = root.join("freelancerProfile", JoinType.INNER);
            Join<FreelancerProfile, User> userJoin = freelancerJoin.join("user", JoinType.INNER);
            predicates.add(cb.isTrue(userJoin.get("active")));

            // 3. Keyword pada judul dan deskripsi (tidak peka huruf besar/kecil)
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate judulLike = cb.like(cb.lower(root.get("judul")), pattern);
                Predicate deskripsiLike = cb.like(cb.lower(root.get("deskripsi")), pattern);
                predicates.add(cb.or(judulLike, deskripsiLike));
            }

            // 4. Filter Kategori (mencakup parent dan sub-kategori)
            if (kategoriIds != null && !kategoriIds.isEmpty()) {
                predicates.add(root.get("kategori").get("id").in(kategoriIds));
            }

            // 5. Filter Rentang Harga
            if (hargaMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("hargaMulai"), hargaMin));
            }
            if (hargaMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hargaMulai"), hargaMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PaketJasa> isNewFreelancerPool(boolean isNew) {
        return (root, query, cb) -> {
            Join<PaketJasa, FreelancerProfile> fp = root.join("freelancerProfile", JoinType.INNER);

            Predicate fullNameValid = cb.and(cb.isNotNull(fp.get("fullName")), cb.notEqual(cb.trim(fp.get("fullName")), ""));
            Predicate bioValid = cb.and(cb.isNotNull(fp.get("bio")), cb.notEqual(cb.trim(fp.get("bio")), ""));
            Predicate headlineValid = cb.and(cb.isNotNull(fp.get("headline")), cb.notEqual(cb.trim(fp.get("headline")), ""));
            Predicate cityValid = cb.and(cb.isNotNull(fp.get("city")), cb.notEqual(cb.trim(fp.get("city")), ""));

            Predicate profileComplete = cb.and(fullNameValid, bioValid, headlineValid, cityValid);

            return isNew ? profileComplete : cb.not(profileComplete);
        };
    }
}
