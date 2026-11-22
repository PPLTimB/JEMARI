package com.jemari.servicepackage.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jemari.servicepackage.dto.ServicePackageRequest;
import com.jemari.servicepackage.dto.ServicePackageResponse;
import com.jemari.servicepackage.entity.ServicePackage;
import com.jemari.servicepackage.repository.ServicePackageRepository;

@Service
public class ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;

    public ServicePackageService(ServicePackageRepository servicePackageRepository) {
        this.servicePackageRepository = servicePackageRepository;
    }

    @Transactional
    public ServicePackageResponse createPackage(Long freelancerId, ServicePackageRequest request) {
        ServicePackage pkg = new ServicePackage(
                freelancerId,
                request.getTitle(),
                request.getCategory(),
                request.getDescription(),
                request.getBasePrice()
        );
        ServicePackage saved = servicePackageRepository.save(pkg);
        return ServicePackageResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ServicePackageResponse> getAllActivePackages() {
        return servicePackageRepository.findByIsActiveTrue()
                .stream()
                .map(ServicePackageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServicePackageResponse> getMyPackages(Long freelancerId) {
        return servicePackageRepository.findByFreelancerIdAndIsActiveTrue(freelancerId)
                .stream()
                .map(ServicePackageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicePackageResponse updatePackage(Long packageId, Long freelancerId, ServicePackageRequest request) {
        ServicePackage pkg = servicePackageRepository.findByIdAndIsActiveTrue(packageId)
                .orElseThrow(() -> new RuntimeException("Paket jasa tidak ditemukan"));

        if (!pkg.getFreelancerId().equals(freelancerId)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk mengubah paket jasa ini");
        }

        pkg.setTitle(request.getTitle());
        pkg.setCategory(request.getCategory());
        pkg.setDescription(request.getDescription());
        pkg.setBasePrice(request.getBasePrice());

        ServicePackage updated = servicePackageRepository.save(pkg);
        return ServicePackageResponse.fromEntity(updated);
    }

    @Transactional
    public void softDeletePackage(Long packageId, Long freelancerId) {
        ServicePackage pkg = servicePackageRepository.findByIdAndIsActiveTrue(packageId)
                .orElseThrow(() -> new RuntimeException("Paket jasa tidak ditemukan"));

        if (!pkg.getFreelancerId().equals(freelancerId)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk menghapus paket jasa ini");
        }

        pkg.setIsActive(false);
        servicePackageRepository.save(pkg);
    }
}