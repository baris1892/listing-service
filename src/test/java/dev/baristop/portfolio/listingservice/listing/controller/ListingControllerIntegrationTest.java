package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.dto.ListingUpdateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.WithMockCustomUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.utils.AbstractIntegrationTest;
import dev.baristop.portfolio.listingservice.utils.JsonTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .content(JsonTestUtils.toJson(listingTestFactory.defaultListingCreateRequest())))
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

    @Test
    void updateListing_shouldReturn401_whenNotAuthenticated() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        ListingUpdateRequest updateRequest = listingTestFactory.defaultListingUpdateRequest();

        mockMvc.perform(put("/api/v1/listings/{id}", listing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(updateRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void updateListing_shouldReturn200_whenOwnerAndPending() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        ListingUpdateRequest updateRequest = listingTestFactory.defaultListingUpdateRequest();

        mockMvc.perform(put("/api/v1/listings/{id}", listing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Listing updated successfully"));

        Listing updated = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(updated.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(updated.getCity()).isEqualTo(updateRequest.getCity());
        assertThat(updated.getPrice()).isEqualByComparingTo(updateRequest.getPrice());
    }

    @Test
    @WithMockCustomUser(id = "not-owner", roles = {Role.USER})
    void updateListing_shouldThrowAccessDenied_whenNotOwner() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        ListingUpdateRequest updateRequest = listingTestFactory.defaultListingUpdateRequest();

        mockMvc.perform(put("/api/v1/listings/{id}", listing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(updateRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("You are not the owner of this listing"));
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void updateListing_shouldThrowInvalidListingState_whenNotPending() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();
        listing.setStatus(ListingStatus.ACTIVE);
        listingRepository.saveAndFlush(listing);

        ListingUpdateRequest updateRequest = listingTestFactory.defaultListingUpdateRequest();

        mockMvc.perform(put("/api/v1/listings/{id}", listing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(updateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Only pending listings can be updated"));
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void updateListing_shouldThrowResourceNotFound_whenListingDoesNotExist() throws Exception {
        ListingUpdateRequest updateRequest = listingTestFactory.defaultListingUpdateRequest();

        mockMvc.perform(put("/api/v1/listings/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Listing with ID 999 not found"));
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void updateListing_shouldReturnValidationErrors() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setTitle("a"); // too short
        updateRequest.setDescription(""); // empty
        updateRequest.setCity("c"); // too short
        updateRequest.setPrice(BigDecimal.valueOf(-1)); // invalid

        mockMvc.perform(put("/api/v1/listings/{id}", listing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonTestUtils.toJson(updateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.title").exists())
            .andExpect(jsonPath("$.errors.description").exists())
            .andExpect(jsonPath("$.errors.city").exists())
            .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void deleteListing_shouldReturn401_whenNotAuthenticated() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        mockMvc.perform(delete("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isUnauthorized());

        assertThat(listingRepository.findById(listing.getId()))
            .isPresent();
    }

    @Test
    @WithMockCustomUser(id = "not-owner", roles = {Role.USER})
    void deleteListing_shouldThrowAccessDenied_whenNotOwner() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        mockMvc.perform(delete("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isForbidden());

        assertThat(listingRepository.findById(listing.getId()))
            .isPresent();
    }

    @Test
    @WithMockCustomUser(id = "not-owner", roles = {Role.ADMIN})
    void deleteListing_shouldReturn200_whenAdmin() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();
        assertThat(listingRepository.findById(listing.getId()))
            .isPresent();

        // users with "ROLE_ADMIN" can also delete listings
        mockMvc.perform(delete("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isOk());

        assertThat(listingRepository.findById(listing.getId()))
            .isEmpty();
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void deleteListing_shouldReturn200_whenOwner() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();
        assertThat(listingRepository.findById(listing.getId()))
            .isPresent();

        mockMvc.perform(delete("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isOk());

        assertThat(listingRepository.findById(listing.getId()))
            .isEmpty();
    }

    @Test
    void getListing_shouldReturn200_whenListingStatusNotPending() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();
        listing.setStatus(ListingStatus.ACTIVE);
        listingRepository.saveAndFlush(listing);

        mockMvc.perform(get("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isOk())
            .andExpectAll(listingMatches(listing));
    }

    @Test
    @WithMockCustomUser(roles = {Role.USER})
    void getListing_shouldReturn200_whenOwner() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        mockMvc.perform(get("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isOk())
            .andExpectAll(listingMatches(listing));
    }

    @Test
    @WithMockCustomUser(roles = {Role.ADMIN})
    void getListing_shouldReturn200_whenAdmin() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        mockMvc.perform(get("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isOk())
            .andExpectAll(listingMatches(listing));
    }

    @Test
    void getListing_shouldReturn404_whenListingStatusPendingAndNotOwner() throws Exception {
        Listing listing = listingTestFactory.createDefaultListing();

        mockMvc.perform(get("/api/v1/listings/{id}", listing.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void getListing_shouldReturn404_whenListingNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/listings/{id}", 999))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllListings_InvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/listings")
                .param("page", "1")
                .param("size", "10")
                .param("sortDir", "desc")
                .param("sortBy", "invalid-sort-by")
            )
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    void getAllListings_InvalidParams() throws Exception {
        mockMvc.perform(get("/api/v1/listings").param("page", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.page").value("must be greater than or equal to 1"));

        mockMvc.perform(get("/api/v1/listings").param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.size").value("must be greater than or equal to 1"));

        mockMvc.perform(get("/api/v1/listings").param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.size").value("must be less than or equal to 100"));
    }

    @Test
    public void getAllListing_shouldReturn200() throws Exception {
        User user = listingTestFactory.createUser("test-user1");

        listingTestFactory.prepareDataForAllListings(user);

        mockMvc.perform(get("/api/v1/listings")
                .param("page", "1")
                .param("size", "10")
                .param("sortDir", "asc")
                .param("sortBy", "title")
            )
            .andExpect(status().isOk())
            //.andDo(JsonTestUtils::printJson)
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].title").value("Galaxy S22"))
            .andExpect(jsonPath("$.data[1].title").value("Galaxy S23"));
    }

    @Test
    public void getAllListing_shouldReturn200_whenFilteredByTitle() throws Exception {
        User user = listingTestFactory.createUser("test-user1");

        listingTestFactory.prepareDataForAllListings(user);

        mockMvc.perform(get("/api/v1/listings")
                .param("page", "1")
                .param("size", "10")
                .param("sortDir", "asc")
                .param("sortBy", "title")
                .param("title", "s22")
            )
            .andExpect(status().isOk())
            //.andDo(JsonTestUtils::printJson)
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].title").value("Galaxy S22"));
    }

    @Test
    public void getAllListing_shouldReturn200_whenFilteredByPrice() throws Exception {
        User user = listingTestFactory.createUser("test-user1");

        listingTestFactory.prepareDataForAllListings(user);

        mockMvc.perform(get("/api/v1/listings")
                .param("page", "1")
                .param("size", "10")
                .param("sortDir", "asc")
                .param("sortBy", "title")
                .param("priceFrom", "550")
                .param("priceTo", "550")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].title").value("Galaxy S22"));
    }

    @Test
    public void getAllListing_shouldReturn200_whenFilteredByTitleAndPrice() throws Exception {
        User user = listingTestFactory.createUser("test-user1");

        listingTestFactory.prepareDataForAllListings(user);

        mockMvc.perform(get("/api/v1/listings")
                .param("page", "1")
                .param("size", "10")
                .param("sortDir", "asc")
                .param("sortBy", "title")
                .param("title", "s22")
                .param("priceFrom", "550")
                .param("priceTo", "550")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].title").value("Galaxy S22"));
    }

    private ResultMatcher[] listingMatches(Listing listing) {
        return new ResultMatcher[]{
            jsonPath("$.id").value(listing.getId()),
            jsonPath("$.title").value(listing.getTitle()),
            jsonPath("$.description").value(listing.getDescription()),
            jsonPath("$.city").value(listing.getCity()),
            jsonPath("$.price").value(listing.getPrice()),
            jsonPath("$.status").value(listing.getStatus().toString()),
            jsonPath("$.createdAt").exists()
        };
    }
}
