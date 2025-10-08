package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.WithMockCustomUser;
import dev.baristop.portfolio.listingservice.security.util.Role;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminListingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingTestFactory listingTestFactory;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    public void approveListing_withoutAuth_shouldReturn401() throws Exception {
        Listing listing = listingTestFactory.createListing("Test Listing");

        mockMvc.perform(patch("/api/v1/admin/listings/" + listing.getId() + "/approve"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(id = "user1", roles = {Role.USER})
    public void approveListing_asUser_shouldReturn403() throws Exception {
        Listing listing = listingTestFactory.createListing("Test Listing");

        mockMvc.perform(patch("/api/v1/admin/listings/" + listing.getId() + "/approve"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(id = "admin1", roles = {Role.ADMIN})
    public void approveListing_asAdmin_shouldReturn200AndUpdateStatus() throws Exception {
        Listing listing = listingTestFactory.createListing("Test Listing");

        mockMvc.perform(patch("/api/v1/admin/listings/" + listing.getId() + "/approve"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(ListingStatus.APPROVED.name()));

        Listing updated = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ListingStatus.APPROVED);
    }

    @Test
    @WithMockCustomUser(id = "admin1", roles = {Role.ADMIN})
    public void rejectListing_asAdmin_shouldReturn200AndUpdateStatus() throws Exception {
        Listing listing = listingTestFactory.createListing("Test Listing");

        mockMvc.perform(patch("/api/v1/admin/listings/" + listing.getId() + "/reject"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(ListingStatus.REJECTED.name()));

        Listing updated = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ListingStatus.REJECTED);
    }
}
