package com.jemari.catalog.category;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_categories")
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ServiceCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ServiceCategory> subCategories = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public ServiceCategory() {
    }

    public ServiceCategory(String name, String description, ServiceCategory parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceCategory getParent() {
        return parent;
    }

    public void setParent(ServiceCategory parent) {
        this.parent = parent;
    }

    public List<ServiceCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<ServiceCategory> subCategories) {
        this.subCategories = subCategories;
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

