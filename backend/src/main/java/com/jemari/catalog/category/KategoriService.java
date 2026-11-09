package com.jemari.catalog.category;

import com.jemari.catalog.category.dto.CreateKategoriRequest;
import com.jemari.catalog.category.dto.KategoriResponse;
import com.jemari.catalog.category.dto.KategoriTreeNodeResponse;
import com.jemari.catalog.category.dto.UpdateKategoriRequest;
import com.jemari.catalog.listing.PaketJasaRepository;
import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class KategoriService {

    private final KategoriJasaRepository kategoriRepository;
    private final PaketJasaRepository paketJasaRepository;

    public KategoriService(KategoriJasaRepository kategoriRepository, PaketJasaRepository paketJasaRepository) {
        this.kategoriRepository = kategoriRepository;
        this.paketJasaRepository = paketJasaRepository;
    }

    @Transactional
    public KategoriResponse createKategori(CreateKategoriRequest request) {
        String nama = request.nama().trim();
        KategoriJasa parent = null;

        if (request.parentId() != null) {
            parent = kategoriRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori induk tidak ditemukan"));

            if (kategoriRepository.existsByNamaIgnoreCaseAndParent(nama, parent)) {
                throw new AppException("Nama sub-kategori '" + nama + "' sudah digunakan pada kategori induk ini", HttpStatus.CONFLICT);
            }
        } else {
            if (kategoriRepository.existsByNamaIgnoreCaseAndParentIsNull(nama)) {
                throw new AppException("Nama kategori '" + nama + "' sudah digunakan", HttpStatus.CONFLICT);
            }
        }

        KategoriJasa kategori = new KategoriJasa(nama, request.deskripsi(), parent);
        KategoriJasa saved = kategoriRepository.save(kategori);
        return KategoriResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<KategoriResponse> getAllKategori() {
        return kategoriRepository.findAll()
                .stream()
                .map(KategoriResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public KategoriResponse getKategoriById(Long id) {
        KategoriJasa kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori dengan ID " + id + " tidak ditemukan"));
        return KategoriResponse.fromEntity(kategori);
    }

    @Transactional
    public KategoriResponse updateKategori(Long id, UpdateKategoriRequest request) {
        KategoriJasa kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori dengan ID " + id + " tidak ditemukan"));

        String nama = request.nama().trim();
        KategoriJasa parent = null;

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new AppException("Kategori tidak dapat menjadi sub-kategori dari dirinya sendiri", HttpStatus.BAD_REQUEST);
            }
            parent = kategoriRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori induk tidak ditemukan"));

            if (kategoriRepository.existsByNamaIgnoreCaseAndParentAndIdNot(nama, parent, id)) {
                throw new AppException("Nama sub-kategori '" + nama + "' sudah digunakan pada kategori induk ini", HttpStatus.CONFLICT);
            }
        } else {
            if (kategoriRepository.existsByNamaIgnoreCaseAndParentIsNullAndIdNot(nama, id)) {
                throw new AppException("Nama kategori '" + nama + "' sudah digunakan", HttpStatus.CONFLICT);
            }
        }

        kategori.setNama(nama);
        kategori.setDeskripsi(request.deskripsi());
        kategori.setParent(parent);
        if (request.active() != null) {
            kategori.setActive(request.active());
        }

        KategoriJasa updated = kategoriRepository.save(kategori);
        return KategoriResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteKategori(Long id) {
        KategoriJasa kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori dengan ID " + id + " tidak ditemukan"));

        if (kategoriRepository.existsByParentId(id)) {
            throw new AppException("Kategori tidak dapat dihapus karena masih memiliki sub-kategori", HttpStatus.BAD_REQUEST);
        }

        if (paketJasaRepository.existsByKategoriId(id)) {
            throw new AppException("Kategori tidak dapat dihapus karena masih digunakan oleh paket jasa", HttpStatus.BAD_REQUEST);
        }

        kategoriRepository.delete(kategori);
    }

    @Transactional(readOnly = true)
    public List<KategoriTreeNodeResponse> getActiveKategoriTree() {
        List<KategoriJasa> rootCategories = kategoriRepository.findAllByActiveTrueAndParentIsNull();
        return rootCategories.stream()
                .map(this::mapToTreeNode)
                .toList();
    }

    private KategoriTreeNodeResponse mapToTreeNode(KategoriJasa kategori) {
        List<KategoriTreeNodeResponse> subKategoriResponses = new ArrayList<>();
        if (kategori.getSubKategori() != null) {
            for (KategoriJasa sub : kategori.getSubKategori()) {
                if (sub.isActive()) {
                    subKategoriResponses.add(mapToTreeNode(sub));
                }
            }
        }
        return new KategoriTreeNodeResponse(
                kategori.getId(),
                kategori.getNama(),
                kategori.getDeskripsi(),
                subKategoriResponses
        );
    }
}
