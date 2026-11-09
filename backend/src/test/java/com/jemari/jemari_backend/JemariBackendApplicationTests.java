package com.jemari.jemari_backend;

import com.jemari.catalog.category.ServiceCategoryRepository;
import com.jemari.catalog.listing.ServicePackageRepository;
import com.jemari.user.FreelancerProfileRepository;
import com.jemari.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class JemariBackendApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FreelancerProfileRepository freelancerProfileRepository;

    @MockBean
    private ServiceCategoryRepository serviceCategoryRepository;

    @MockBean
    private ServicePackageRepository servicePackageRepository;

    @Test
    void contextLoads() {
    }

}
