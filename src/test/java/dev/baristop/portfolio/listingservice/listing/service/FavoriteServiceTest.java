package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.UserFavoriteListing;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.listing.repository.UserFavoriteListingRepository;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.testdata.UserTestFactory;
import dev.baristop.portfolio.listingservice.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FavoriteServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserFavoriteListingRepository favoriteRepository;


    @Autowired
    private ListingTestFactory listingTestFactory;
    @Autowired
    private UserTestFactory userTestFactory;

    private User user;
    private Listing listing;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        user = userTestFactory.createDefaultUser();
        listing = listingTestFactory.createListing("Redis Test");
    }

    @Test
    void toggleFavorite_addsFavorite_whenNotExists() {
        boolean result = favoriteService.toggleFavorite(user.getId(), listing.getId());

        assertThat(result).isTrue();

        Optional<UserFavoriteListing> favoriteOpt = favoriteRepository.findByUserAndListing(user, listing);
        assertThat(favoriteOpt).isPresent();
    }

    @Test
    void toggleFavorite_removesFavorite_whenExists() {
        // First add favorite
        favoriteService.toggleFavorite(user.getId(), listing.getId());

        // Now remove
        boolean result = favoriteService.toggleFavorite(user.getId(), listing.getId());

        assertThat(result).isFalse();

        Optional<UserFavoriteListing> favoriteOpt = favoriteRepository.findByUserAndListing(user, listing);
        assertThat(favoriteOpt).isEmpty();
    }

    @Test
    void toggleFavorite_multipleToggles_workCorrectly() {
        // Add
        boolean first = favoriteService.toggleFavorite(user.getId(), listing.getId());
        assertThat(first).isTrue();

        // Remove
        boolean second = favoriteService.toggleFavorite(user.getId(), listing.getId());
        assertThat(second).isFalse();

        // Add again
        boolean third = favoriteService.toggleFavorite(user.getId(), listing.getId());
        assertThat(third).isTrue();
    }
}
