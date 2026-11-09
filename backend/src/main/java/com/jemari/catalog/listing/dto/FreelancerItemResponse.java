package com.jemari.catalog.listing.dto;

public record FreelancerItemResponse(
        Long profileId,
        String fullName,
        String avatarUrl
) {
}
