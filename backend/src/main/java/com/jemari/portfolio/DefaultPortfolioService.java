package com.jemari.portfolio;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.portfolio.dto.CreatePortfolioDto;
import com.jemari.portfolio.dto.PortfolioResponse;
import com.jemari.portfolio.dto.UpdatePortfolioDto;
import com.jemari.profile.FreelancerProfile;
import com.jemari.profile.FreelancerProfileRepository;
import com.jemari.user.Role;
import com.jemari.user.User;
import com.jemari.user.UserRepository;

@Service
public class DefaultPortfolioService implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final UserRepository userRepository;

    public DefaultPortfolioService(PortfolioRepository portfolioRepository,
                                  FreelancerProfileRepository freelancerProfileRepository,
                                  UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PortfolioResponse createPortfolio(Long userId, CreatePortfolioDto dto) {
        validatePortfolioInput(dto.getTitle(), dto.getImageUrl());

        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfileForUser(userId));

        Portfolio portfolio = new Portfolio(
                profile,
                dto.getTitle().trim(),
                dto.getDescription() != null ? dto.getDescription().trim() : null,
                dto.getImageUrl().trim()
        );

        Portfolio saved = portfolioRepository.save(portfolio);
        return PortfolioResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getMyPortfolios(Long userId) {
        return freelancerProfileRepository.findByUserId(userId)
                .map(profile -> portfolioRepository.findByFreelancerProfileIdAndActiveTrueOrderByCreatedAtDesc(profile.getId())
                        .stream()
                        .map(PortfolioResponse::fromEntity)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getPortfoliosByFreelancerProfileId(Long profileId) {
        return portfolioRepository.findByFreelancerProfileIdAndActiveTrueOrderByCreatedAtDesc(profileId)
                .stream()
                .map(PortfolioResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Karya portofolio tidak ditemukan"));
        return PortfolioResponse.fromEntity(portfolio);
    }

    @Override
    @Transactional
    public PortfolioResponse updatePortfolio(Long userId, Long portfolioId, UpdatePortfolioDto dto) {
        validatePortfolioInput(dto.getTitle(), dto.getImageUrl());

        Portfolio portfolio = portfolioRepository.findByIdAndActiveTrue(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Karya portofolio tidak ditemukan"));

        validateOwnership(userId, portfolio);

        portfolio.setTitle(dto.getTitle().trim());
        portfolio.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        portfolio.setImageUrl(dto.getImageUrl().trim());

        Portfolio updated = portfolioRepository.save(portfolio);
        return PortfolioResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deletePortfolio(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndActiveTrue(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Karya portofolio tidak ditemukan"));

        validateOwnership(userId, portfolio);

        portfolio.setActive(false);
        portfolioRepository.save(portfolio);
    }

    private void validateOwnership(Long userId, Portfolio portfolio) {
        if (portfolio.getFreelancerProfile() == null ||
                portfolio.getFreelancerProfile().getUser() == null ||
                !portfolio.getFreelancerProfile().getUser().getId().equals(userId)) {
            throw new AppException("Anda tidak memiliki akses untuk mengubah atau menghapus karya portofolio ini", HttpStatus.FORBIDDEN);
        }
    }

    private void validatePortfolioInput(String title, String imageUrl) {
        if (title == null || title.trim().isEmpty()) {
            throw new AppException("Judul karya wajib diisi", HttpStatus.BAD_REQUEST);
        }
        if (title.trim().length() > 150) {
            throw new AppException("Judul karya tidak boleh melebihi 150 karakter", HttpStatus.BAD_REQUEST);
        }
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new AppException("Berkas gambar atau tautan gambar karya wajib diisi", HttpStatus.BAD_REQUEST);
        }

        String trimmedImage = imageUrl.trim();

        // 1. Validasi Base64 data URI (format dan ukuran)
        if (trimmedImage.startsWith("data:")) {
            boolean validFormat = trimmedImage.startsWith("data:image/jpeg") ||
                    trimmedImage.startsWith("data:image/jpg") ||
                    trimmedImage.startsWith("data:image/png") ||
                    trimmedImage.startsWith("data:image/webp");

            if (!validFormat) {
                throw new AppException("Format berkas tidak didukung. Harap unggah format JPG, PNG, atau WebP", HttpStatus.BAD_REQUEST);
            }

            int commaIndex = trimmedImage.indexOf(',');
            if (commaIndex != -1) {
                String base64Data = trimmedImage.substring(commaIndex + 1);
                long estimatedBytes = (long) (base64Data.length() * 0.75);
                long maxBytes = 5L * 1024 * 1024; // 5 MB
                if (estimatedBytes > maxBytes) {
                    throw new AppException("Ukuran berkas melebihi batas maksimal 5 MB", HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            // 2. Validasi URL / Path berkas
            String lower = trimmedImage.toLowerCase();
            int qIndex = lower.indexOf('?');
            if (qIndex != -1) {
                lower = lower.substring(0, qIndex);
            }
            if (lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".svg") ||
                    lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") ||
                    lower.endsWith(".zip") || lower.endsWith(".exe") || lower.endsWith(".mp4")) {
                throw new AppException("Format berkas tidak didukung. Harap unggah format JPG, PNG, atau WebP", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private FreelancerProfile createDefaultProfileForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna tidak ditemukan"));

        if (user.getRole() != Role.FREELANCER) {
            throw new AppException("Hanya pengguna dengan peran FREELANCER yang dapat memiliki portofolio", HttpStatus.FORBIDDEN);
        }

        FreelancerProfile newProfile = new FreelancerProfile();
        newProfile.setUser(user);
        String name = user.getEmail().contains("@") ? user.getEmail().substring(0, user.getEmail().indexOf('@')) : user.getEmail();
        newProfile.setFullName(name);
        return freelancerProfileRepository.save(newProfile);
    }
}
