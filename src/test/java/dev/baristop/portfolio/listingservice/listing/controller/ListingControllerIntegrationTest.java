package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.WithMockCustomUser;
import dev.baristop.portfolio.listingservice.security.util.Role;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.utils.AbstractIntegrationTest;
import dev.baristop.portfolio.listingservice.utils.JsonTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ListingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ListingTestFactory listingTestFactory;

    @Test
    void createListing_shouldReturn401_whenUserIsAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(listingTestFactory.defaultListingRequest())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createListing_shouldReturn400_whenValidationFails() throws Exception {
        // payload that violates validation rules
        ListingCreateRequest request = new ListingCreateRequest();
        request.setTitle("a");
        request.setDescription("b");
        request.setCity("c");
        request.setPrice(BigDecimal.valueOf(-1));

        mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(request)))
            // .andDo(JsonTestUtils::printJson)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.city").value("size must be between 2 and 255"))
            .andExpect(jsonPath("$.errors.price").value("must be greater than or equal to 0"))
            .andExpect(jsonPath("$.errors.description").value("size must be between 2 and 2000"))
            .andExpect(jsonPath("$.errors.title").value("size must be between 2 and 255"));
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void createListing_shouldReturn201_whenUserIsAuthenticated() throws Exception {
        // Arrange
        BigDecimal price = BigDecimal.valueOf(100);
        String title = "Test Title";
        String description = "Test Description";
        String city = "Test City";

        ListingCreateRequest request = new ListingCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCity(city);
        request.setPrice(price);

        // Act & Assert
        mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Listing created successfully"));

        Listing listing = listingRepository.findByTitleAndDescriptionAndCity(title, description, city)
            .orElseThrow();

        assertThat(listing.getTitle()).isEqualTo(title);
        assertThat(listing.getDescription()).isEqualTo(description);
        assertThat(listing.getCity()).isEqualTo(city);
        assertThat(listing.getPrice()).isEqualTo(price);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.PENDING);
    }
}
