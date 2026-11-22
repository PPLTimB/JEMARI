package com.jemari.request.dto;

public class RequestReferencePhotoResponse {
    private Long id;
    private String photoUrl;
    private String originalName;
    private Integer sortOrder;

    public RequestReferencePhotoResponse() {}

    public RequestReferencePhotoResponse(Long id, String photoUrl, String originalName, Integer sortOrder) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.originalName = originalName;
        this.sortOrder = sortOrder;
    }

    // --- MANUAL GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}