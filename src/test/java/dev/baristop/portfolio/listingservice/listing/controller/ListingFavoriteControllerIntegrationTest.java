package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.UserFavoriteListing;
import dev.baristop.portfolio.listingservice.listing.repository.UserFavoriteListingRepository;
import dev.baristop.portfolio.listingservice.security.WithMockCustomUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.testdata.UserTestFactory;
import dev.baristop.portfolio.listingservice.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ListingFavoriteControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingTestFactory listingTestFactory;

    @Autowired
    private UserTestFactory userTestFactory;

    @Autowired
    private UserFavoriteListingRepository favoriteRepository;

    @Test
    @WithMockCustomUser(id = "user1", roles = {Role.USER})
    void toggleFavoriteListing_shouldToggleFavoriteTwice() throws Exception {
        // Prepare test data
        Listing listing = listingTestFactory.createDefaultListing();
        User currentUser = userTestFactory.createUser("user1");

        // Ensure no favorites exist initially
        assertTrue(
            favoriteRepository.findAll().isEmpty(),
            "Favorite should not exist before toggle"
        );

        // First toggle: add favorite
        mockMvc.perform(post("/api/v1/listings/{id}/toggle-favorite", listing.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isFavorite").value(true));

        // Verify favorite exists in DB
        UserFavoriteListing favoriteAfterAdd = favoriteRepository.findAll().getFirst();
        assertEquals(
            currentUser.getId(),
            favoriteAfterAdd.getUser().getId(),
            "User ID should match"
        );
        assertEquals(
            listing.getId(),
            favoriteAfterAdd.getListing().getId(),
            "Listing ID should match"
        );

        // Second toggle: remove favorite
        mockMvc.perform(post("/api/v1/listings/{id}/toggle-favorite", listing.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isFavorite").value(false));

        // Verify favorite was removed from DB
        assertTrue(
            favoriteRepository.findAll().isEmpty(),
            "Favorite should be removed after second toggle"
        );
    }
}
