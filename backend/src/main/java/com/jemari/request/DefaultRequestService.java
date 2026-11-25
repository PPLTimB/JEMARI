package com.jemari.request;

import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.request.dto.CreateRequestDto;
import com.jemari.request.dto.RequestResponse;
import com.jemari.request.dto.UpdateRequestDto;
import com.jemari.umkm.UmkmProfile;
import com.jemari.umkm.UmkmProfileRepository;
import com.jemari.user.Role;
import com.jemari.user.User;
import com.jemari.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class DefaultRequestService implements RequestService {

    private final RequestRepository requestRepository;
    private final UmkmProfileRepository umkmProfileRepository;
    private final UserRepository userRepository;

    public DefaultRequestService(RequestRepository requestRepository,
                                 UmkmProfileRepository umkmProfileRepository,
                                 UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.umkmProfileRepository = umkmProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RequestResponse createRequest(CreateRequestDto dto, String userEmail) {
        User user = getUserByEmail(userEmail);

        if (user.getRole() != Role.UMKM && user.getRole() != Role.ADMIN) {
            throw new AppException("Hanya pengguna dengan peran UMKM yang dapat membuat request kebutuhan", HttpStatus.FORBIDDEN);
        }

        if (dto.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Anggaran harus lebih besar dari 0", HttpStatus.BAD_REQUEST);
        }

        if (dto.getDeadline().isBefore(LocalDate.now())) {
            throw new AppException("Tenggat waktu tidak boleh tanggal lampau", HttpStatus.BAD_REQUEST);
        }

        UmkmProfile profile = umkmProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UmkmProfile newProfile = new UmkmProfile();
                    newProfile.setUser(user);
                    newProfile.setBusinessName("UMKM " + (user.getEmail().contains("@") ? user.getEmail().split("@")[0] : user.getEmail()));
                    newProfile.setCategory(dto.getCategory());
                    newProfile.setActive(true);
                    return umkmProfileRepository.save(newProfile);
                });

        ServiceRequest serviceRequest = new ServiceRequest(
                profile,
                dto.getTitle(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getBudget(),
                dto.getDeadline()
        );

        ServiceRequest saved = requestRepository.save(serviceRequest);
        return RequestResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponse> getMyRequests(String userEmail) {
        User user = getUserByEmail(userEmail);

        return umkmProfileRepository.findByUserId(user.getId())
                .map(profile -> requestRepository.findByUmkmProfileIdAndActiveTrueOrderByCreatedAtDesc(profile.getId())
                        .stream()
                        .map(RequestResponse::fromEntity)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponse> getAllOpenRequests(String category) {
        List<ServiceRequest> requests;
        if (category != null && !category.trim().isEmpty()) {
            requests = requestRepository.findByCategoryIgnoreCaseAndStatusAndActiveTrueOrderByCreatedAtDesc(category.trim(), RequestStatus.OPEN);
        } else {
            requests = requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN);
        }

        return requests.stream()
                .map(RequestResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponse getRequestById(Long id) {
        ServiceRequest request = getActiveRequestById(id);
        return RequestResponse.fromEntity(request);
    }

    @Override
    public RequestResponse updateRequest(Long id, UpdateRequestDto dto, String userEmail) {
        ServiceRequest request = getActiveRequestById(id);
        validateOwnership(request, userEmail);

        if (request.getStatus() != RequestStatus.OPEN) {
            throw new AppException(
                    "Request tidak dapat diubah karena saat ini sedang berjalan atau telah ditutup (Status: " + request.getStatus() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Anggaran harus lebih besar dari 0", HttpStatus.BAD_REQUEST);
        }

        if (dto.getDeadline().isBefore(LocalDate.now())) {
            throw new AppException("Tenggat waktu tidak boleh tanggal lampau", HttpStatus.BAD_REQUEST);
        }

        request.setTitle(dto.getTitle());
        request.setCategory(dto.getCategory());
        request.setDescription(dto.getDescription());
        request.setBudget(dto.getBudget());
        request.setDeadline(dto.getDeadline());

        ServiceRequest updated = requestRepository.save(request);
        return RequestResponse.fromEntity(updated);
    }

    @Override
    public RequestResponse closeRequest(Long id, String userEmail) {
        ServiceRequest request = getActiveRequestById(id);
        validateOwnership(request, userEmail);

        if (request.getStatus() == RequestStatus.CLOSED) {
            throw new AppException("Request sudah ditutup sebelumnya", HttpStatus.BAD_REQUEST);
        }

        if (request.getStatus() != RequestStatus.OPEN) {
            throw new AppException(
                    "Request tidak dapat ditutup karena sedang dalam proses pengerjaan (Status: " + request.getStatus() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        request.setStatus(RequestStatus.CLOSED);
        ServiceRequest saved = requestRepository.save(request);
        return RequestResponse.fromEntity(saved);
    }

    @Override
    public void deleteRequest(Long id, String userEmail) {
        ServiceRequest request = getActiveRequestById(id);
        validateOwnership(request, userEmail);

        if (request.getStatus() == RequestStatus.IN_PROGRESS) {
            throw new AppException("Request sedang dikerjakan sebagai proyek dan tidak dapat dihapus", HttpStatus.BAD_REQUEST);
        }

        request.setActive(false);
        requestRepository.save(request);
    }

    private ServiceRequest getActiveRequestById(Long id) {
        return requestRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request dengan ID " + id + " tidak ditemukan"));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna dengan email " + email + " tidak ditemukan"));
    }

    private void validateOwnership(ServiceRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (request.getUmkmProfile() == null ||
                request.getUmkmProfile().getUser() == null ||
                !request.getUmkmProfile().getUser().getId().equals(user.getId())) {
            throw new AppException("Anda tidak memiliki hak akses untuk mengelola request ini", HttpStatus.FORBIDDEN);
        }
    }
}
