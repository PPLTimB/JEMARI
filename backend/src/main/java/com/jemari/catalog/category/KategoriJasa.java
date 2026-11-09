package com.jemari.catalog.category;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kategori_jasa")
public class KategoriJasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nama;

    @Column(columnDefinition = "TEXT")
    private String deskripsi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private KategoriJasa parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<KategoriJasa> subKategori = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public KategoriJasa() {
    }

    public KategoriJasa(String nama, String deskripsi, KategoriJasa parent) {
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.parent = parent;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public KategoriJasa getParent() {
        return parent;
    }

    public void setParent(KategoriJasa parent) {
        this.parent = parent;
    }

    public List<KategoriJasa> getSubKategori() {
        return subKategori;
    }

    public void setSubKategori(List<KategoriJasa> subKategori) {
        this.subKategori = subKategori;
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
