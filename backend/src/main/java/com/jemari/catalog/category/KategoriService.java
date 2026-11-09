package com.jemari.catalog.category;

import com.jemari.catalog.category.dto.CreateKategoriRequest;
import com.jemari.catalog.category.dto.KategoriResponse;
import com.jemari.catalog.category.dto.KategoriTreeNodeResponse;
import com.jemari.catalog.category.dto.UpdateKategoriRequest;
import com.jemari.catalog.listing.ServicePackageRepository;
import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class KategoriService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServicePackageRepository servicePackageRepository;

    public KategoriService(
            ServiceCategoryRepository serviceCategoryRepository,
            ServicePackageRepository servicePackageRepository
    ) {
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.servicePackageRepository = servicePackageRepository;
    }

    @Transactional
    public KategoriResponse createKategori(CreateKategoriRequest request) {
        String nama = request.nama().trim();
        ServiceCategory parent = null;

        if (request.parentId() != null) {
            parent = serviceCategoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori induk tidak ditemukan"));

            if (serviceCategoryRepository.existsByNameIgnoreCaseAndParent(nama, parent)) {
                throw new AppException("Nama sub-kategori '" + nama + "' sudah digunakan pada kategori induk ini", HttpStatus.CONFLICT);
            }
        } else {
            if (serviceCategoryRepository.existsByNameIgnoreCaseAndParentIsNull(nama)) {
                throw new AppException("Nama kategori '" + nama + "' sudah digunakan", HttpStatus.CONFLICT);
            }
        }

        ServiceCategory kategori = new ServiceCategory(nama, request.deskripsi(), parent);
        ServiceCategory saved = serviceCategoryRepository.save(kategori);
        return KategoriResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<KategoriResponse> getAllKategori() {
        return serviceCategoryRepository.findAll()
                .stream()
                .map(KategoriResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public KategoriResponse getKategoriById(Long id) {
        ServiceCategory kategori = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori dengan ID " + id + " tidak ditemukan"));
        return KategoriResponse.fromEntity(kategori);
    }

    @Transactional
    public KategoriResponse updateKategori(Long id, UpdateKategoriRequest request) {
        ServiceCategory kategori = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori dengan ID " + id + " tidak ditemukan"));

        String nama = request.nama().trim();
        ServiceCategory parent = null;

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new AppException("Kategori tidak dapat menjadi sub-kategori dari dirinya sendiri", HttpStatus.BAD_REQUEST);
            }
            parent = serviceCategoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori induk tidak ditemukan"));

            if (serviceCategoryRepository.existsByNameIgnoreCaseAndParentAndIdNot(nama, parent, id)) {
                throw new AppException("Nama sub-kategori '" + nama + "' sudah digunakan pada kategori induk ini", HttpStatus.CONFLICT);
            }
        } else {
            if (serviceCategoryRepository.existsByNameIgnoreCaseAndParentIsNullAndIdNot(nama, id)) {
                throw new AppException("Nama kategori '" + nama + "' sudah digunakan", HttpStatus.CONFLICT);
            }
        }

        kategori.setName(nama);
        kategori.setDescription(request.deskripsi());
        kategori.setParent(parent);
        if (request.active() != null) {
            kategori.setActive(request.active());
        }

        ServiceCategory updated = serviceCategoryRepository.save(kategori);
        return KategoriResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteKategori(Long id) {
        ServiceCategory kategori = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori dengan ID " + id + " tidak ditemukan"));

        if (serviceCategoryRepository.existsByParentId(id)) {
            throw new AppException("Kategori tidak dapat dihapus karena masih memiliki sub-kategori", HttpStatus.BAD_REQUEST);
        }

        if (servicePackageRepository.countByCategoryId(id) > 0) {
            throw new AppException("Kategori tidak dapat dihapus karena masih digunakan oleh paket jasa", HttpStatus.BAD_REQUEST);
        }

        serviceCategoryRepository.delete(kategori);
    }

    @Transactional(readOnly = true)
    public List<KategoriTreeNodeResponse> getActiveKategoriTree() {
        List<ServiceCategory> rootCategories = serviceCategoryRepository.findAllByActiveTrueAndParentIsNull();
        return rootCategories.stream()
                .map(this::mapToTreeNode)
                .toList();
    }

    private KategoriTreeNodeResponse mapToTreeNode(ServiceCategory kategori) {
        List<KategoriTreeNodeResponse> subKategoriResponses = new ArrayList<>();
        if (kategori.getSubCategories() != null) {
            for (ServiceCategory sub : kategori.getSubCategories()) {
                if (sub.isActive()) {
                    subKategoriResponses.add(mapToTreeNode(sub));
                }
            }
        }
        return new KategoriTreeNodeResponse(
                kategori.getId(),
                kategori.getName(),
                kategori.getDescription(),
                subKategoriResponses
        );
    }
}
