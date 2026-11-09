package com.jemari.catalog.listing;

import com.jemari.catalog.category.KategoriJasa;
import com.jemari.catalog.category.KategoriJasaRepository;
import com.jemari.catalog.listing.dto.FreelancerItemResponse;
import com.jemari.catalog.listing.dto.KatalogFilterParam;
import com.jemari.catalog.listing.dto.KatalogPagedResponse;
import com.jemari.catalog.listing.dto.PaketJasaItemResponse;
import com.jemari.common.dto.ApiResponse;
import com.jemari.common.dto.ErrorDetail;
import com.jemari.common.exception.AppException;
import com.jemari.user.FreelancerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class KatalogService {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    public static final int NEW_FREELANCER_SLOT_INTERVAL = 5;

    private final PaketJasaRepository paketJasaRepository;
    private final KategoriJasaRepository kategoriRepository;
    private final FreelancerStatsService freelancerStatsService;

    public KatalogService(
            PaketJasaRepository paketJasaRepository,
            KategoriJasaRepository kategoriRepository,
            FreelancerStatsService freelancerStatsService
    ) {
        this.paketJasaRepository = paketJasaRepository;
        this.kategoriRepository = kategoriRepository;
        this.freelancerStatsService = freelancerStatsService;
    }

    @Transactional(readOnly = true)
    public ApiResponse<KatalogPagedResponse> searchKatalog(KatalogFilterParam param) {
        validateFilterParam(param);

        int page = param.page() != null && param.page() >= 0 ? param.page() : DEFAULT_PAGE;
        int size = param.size() != null && param.size() > 0 ? Math.min(param.size(), MAX_SIZE) : DEFAULT_SIZE;

        // Ambil kategori beserta seluruh sub-kategorinya jika filter kategori diisi
        List<Long> categoryIds = collectCategoryIds(param.kategoriId());

        Specification<PaketJasa> baseSpec = PaketJasaSpecifications.buildBaseSpecification(
                param.keyword(),
                categoryIds,
                param.hargaMin(),
                param.hargaMax()
        );

        long totalElements = paketJasaRepository.count(baseSpec);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        if (totalElements == 0) {
            KatalogPagedResponse emptyResponse = new KatalogPagedResponse(
                    Collections.emptyList(),
                    page,
                    size,
                    0,
                    0
            );
            return ApiResponse.ok("Jasa tidak ditemukan. Coba longgarkan filter pencarian Anda.", emptyResponse);
        }

        Sort sortOrder = determineSort(param.sort());

        // Hitung slot adil bagi freelancer baru (1 dari setiap 5 posisi)
        int targetNewCount = 0;
        for (int i = 0; i < size; i++) {
            if (i % NEW_FREELANCER_SLOT_INTERVAL == 0) {
                targetNewCount++;
            }
        }
        int targetRegularCount = size - targetNewCount;

        Specification<PaketJasa> newPoolSpec = Specification.where(baseSpec)
                .and(PaketJasaSpecifications.isNewFreelancerPool(true));
        Specification<PaketJasa> regularPoolSpec = Specification.where(baseSpec)
                .and(PaketJasaSpecifications.isNewFreelancerPool(false));

        Page<PaketJasa> newPage = paketJasaRepository.findAll(newPoolSpec, PageRequest.of(page, targetNewCount, sortOrder));
        Page<PaketJasa> regularPage = paketJasaRepository.findAll(regularPoolSpec, PageRequest.of(page, targetRegularCount, sortOrder));

        List<PaketJasa> newItems = new ArrayList<>(newPage.getContent());
        List<PaketJasa> regularItems = new ArrayList<>(regularPage.getContent());

        List<PaketJasa> mergedItems = mergeWithFairness(newItems, regularItems, size);

        List<PaketJasaItemResponse> content = mergedItems.stream()
                .map(this::mapToItemResponse)
                .toList();

        KatalogPagedResponse pagedResponse = new KatalogPagedResponse(
                content,
                page,
                size,
                totalElements,
                totalPages
        );

        return ApiResponse.ok("Katalog jasa berhasil dimuat", pagedResponse);
    }

    private void validateFilterParam(KatalogFilterParam param) {
        List<ErrorDetail> errors = new ArrayList<>();

        if (param.hargaMin() != null && param.hargaMin().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(new ErrorDetail("hargaMin", "hargaMin tidak boleh bernilai negatif"));
        }
        if (param.hargaMax() != null && param.hargaMax().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(new ErrorDetail("hargaMax", "hargaMax tidak boleh bernilai negatif"));
        }
        if (param.hargaMin() != null && param.hargaMax() != null
                && param.hargaMin().compareTo(param.hargaMax()) > 0) {
            errors.add(new ErrorDetail("hargaMin", "hargaMin tidak boleh lebih besar dari hargaMax"));
        }

        if (!errors.isEmpty()) {
            throw new AppException("Validasi filter harga gagal", HttpStatus.BAD_REQUEST, errors);
        }
    }

    private List<Long> collectCategoryIds(Long kategoriId) {
        if (kategoriId == null) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        ids.add(kategoriId);

        // Rekursif mengumpulkan sub-kategori aktif
        collectSubCategoryIds(kategoriId, ids);
        return ids;
    }

    private void collectSubCategoryIds(Long parentId, List<Long> ids) {
        List<KategoriJasa> subCategories = kategoriRepository.findAllByParentId(parentId);
        for (KategoriJasa sub : subCategories) {
            if (sub.isActive()) {
                ids.add(sub.getId());
                collectSubCategoryIds(sub.getId(), ids);
            }
        }
    }

    private Sort determineSort(String sortParam) {
        if (!StringUtils.hasText(sortParam)) {
            // Default: gabungan terbaru dan id untuk menjaga konsistensi urutan
            return Sort.by(Sort.Direction.DESC, "createdAt")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
        }

        String sortLower = sortParam.trim().toLowerCase();
        if ("harga_asc".equals(sortLower)) {
            return Sort.by(Sort.Direction.ASC, "hargaMulai")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .and(Sort.by(Sort.Direction.DESC, "id"));
        } else if ("harga_desc".equals(sortLower)) {
            return Sort.by(Sort.Direction.DESC, "hargaMulai")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .and(Sort.by(Sort.Direction.DESC, "id"));
        }

        return Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private List<PaketJasa> mergeWithFairness(List<PaketJasa> newItems, List<PaketJasa> regularItems, int targetSize) {
        List<PaketJasa> merged = new ArrayList<>();
        int newIdx = 0;
        int regIdx = 0;

        for (int i = 0; i < targetSize; i++) {
            boolean isNewSlot = (i % NEW_FREELANCER_SLOT_INTERVAL == 0);

            if (isNewSlot) {
                if (newIdx < newItems.size()) {
                    merged.add(newItems.get(newIdx++));
                } else if (regIdx < regularItems.size()) {
                    merged.add(regularItems.get(regIdx++));
                }
            } else {
                if (regIdx < regularItems.size()) {
                    merged.add(regularItems.get(regIdx++));
                } else if (newIdx < newItems.size()) {
                    merged.add(newItems.get(newIdx++));
                }
            }
        }

        return merged;
    }

    private PaketJasaItemResponse mapToItemResponse(PaketJasa paket) {
        FreelancerProfile profile = paket.getFreelancerProfile();
        boolean isBaru = isNewFreelancer(profile);

        FreelancerItemResponse freelancerResponse = new FreelancerItemResponse(
                profile != null ? profile.getId() : null,
                profile != null ? profile.getFullName() : null,
                profile != null ? profile.getAvatarUrl() : null
        );

        String kategoriNama = paket.getKategori() != null ? paket.getKategori().getNama() : null;

        return new PaketJasaItemResponse(
                paket.getId(),
                paket.getJudul(),
                kategoriNama,
                freelancerResponse,
                paket.getHargaMulai(),
                isBaru
        );
    }

    public boolean isNewFreelancer(FreelancerProfile profile) {
        if (profile == null) {
            return false;
        }

        boolean profileComplete = StringUtils.hasText(profile.getFullName())
                && StringUtils.hasText(profile.getBio())
                && StringUtils.hasText(profile.getHeadline())
                && StringUtils.hasText(profile.getCity());

        if (!profileComplete) {
            return false;
        }

        return freelancerStatsService.getCompletedOrderCount(profile.getId()) == 0;
    }
}
