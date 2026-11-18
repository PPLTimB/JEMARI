package com.jemari.request;

import com.jemari.common.dto.ApiResponse;
import com.jemari.request.dto.CreateRequestDto;
import com.jemari.request.dto.RequestResponse;
import com.jemari.request.dto.UpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('UMKM', 'ADMIN')")
    public ResponseEntity<ApiResponse<RequestResponse>> createRequest(@Valid @RequestBody CreateRequestDto dto,
                                                                      Principal principal) {
        RequestResponse response = requestService.createRequest(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Request kebutuhan berhasil diposting", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('UMKM', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RequestResponse>>> getMyRequests(Principal principal) {
        List<RequestResponse> list = requestService.getMyRequests(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Daftar request berhasil dimuat", list));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RequestResponse>>> getAllOpenRequests(
            @RequestParam(required = false) String category) {
        List<RequestResponse> list = requestService.getAllOpenRequests(category);
        return ResponseEntity.ok(ApiResponse.ok("Daftar request terbuka berhasil dimuat", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestResponse>> getRequestById(@PathVariable Long id) {
        RequestResponse response = requestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.ok("Detail request berhasil dimuat", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UMKM', 'ADMIN')")
    public ResponseEntity<ApiResponse<RequestResponse>> updateRequest(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateRequestDto dto,
                                                                      Principal principal) {
        RequestResponse response = requestService.updateRequest(id, dto, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Request kebutuhan berhasil diperbarui", response));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('UMKM', 'ADMIN')")
    public ResponseEntity<ApiResponse<RequestResponse>> closeRequest(@PathVariable Long id,
                                                                     Principal principal) {
        RequestResponse response = requestService.closeRequest(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Request kebutuhan berhasil ditutup", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UMKM', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id,
                                                           Principal principal) {
        requestService.deleteRequest(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Request kebutuhan berhasil dihapus", null));
    }
}
