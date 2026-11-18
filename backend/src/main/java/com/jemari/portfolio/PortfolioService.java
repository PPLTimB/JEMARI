package com.jemari.portfolio;

import java.util.List;

import com.jemari.portfolio.dto.CreatePortfolioDto;
import com.jemari.portfolio.dto.PortfolioResponse;
import com.jemari.portfolio.dto.UpdatePortfolioDto;

public interface PortfolioService {

    PortfolioResponse createPortfolio(Long userId, CreatePortfolioDto dto);

    List<PortfolioResponse> getMyPortfolios(Long userId);

    List<PortfolioResponse> getPortfoliosByFreelancerProfileId(Long profileId);

    PortfolioResponse getPortfolioById(Long id);

    PortfolioResponse updatePortfolio(Long userId, Long portfolioId, UpdatePortfolioDto dto);

    void deletePortfolio(Long userId, Long portfolioId);
}
