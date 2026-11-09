package com.jemari.catalog.listing;

import com.jemari.catalog.category.KategoriJasa;
import com.jemari.user.FreelancerProfile;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paket_jasa")
public class PaketJasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_profile_id", nullable = false)
    private FreelancerProfile freelancerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_id", nullable = false)
    private KategoriJasa kategori;

    @Column(nullable = false)
    private String judul;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String deskripsi;

    @Column(name = "harga_mulai", nullable = false, precision = 12, scale = 2)
    private BigDecimal hargaMulai;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public PaketJasa() {
    }

    public PaketJasa(FreelancerProfile freelancerProfile, KategoriJasa kategori, String judul, String deskripsi, BigDecimal hargaMulai) {
        this.freelancerProfile = freelancerProfile;
        this.kategori = kategori;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.hargaMulai = hargaMulai;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FreelancerProfile getFreelancerProfile() {
        return freelancerProfile;
    }

    public void setFreelancerProfile(FreelancerProfile freelancerProfile) {
        this.freelancerProfile = freelancerProfile;
    }

    public KategoriJasa getKategori() {
        return kategori;
    }

    public void setKategori(KategoriJasa kategori) {
        this.kategori = kategori;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public BigDecimal getHargaMulai() {
        return hargaMulai;
    }

    public void setHargaMulai(BigDecimal hargaMulai) {
        this.hargaMulai = hargaMulai;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
