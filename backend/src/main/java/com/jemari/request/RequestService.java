package com.jemari.request;

import com.jemari.request.dto.CreateRequestDto;
import com.jemari.request.dto.RequestResponse;
import com.jemari.request.dto.UpdateRequestDto;

import java.util.List;

public interface RequestService {

    RequestResponse createRequest(CreateRequestDto dto, String userEmail);

    List<RequestResponse> getMyRequests(String userEmail);

    List<RequestResponse> getAllOpenRequests(String category);

    RequestResponse getRequestById(Long id);

    RequestResponse updateRequest(Long id, UpdateRequestDto dto, String userEmail);

    RequestResponse closeRequest(Long id, String userEmail);

    void deleteRequest(Long id, String userEmail);
}
