package dev.baristop.portfolio.listingservice.listing.repository;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.entity.UserFavoriteListing;
import dev.baristop.portfolio.listingservice.security.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserFavoriteListingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserFavoriteListingRepository userFavoriteListingRepository;

    private User owner;
    private Listing listing1; // This will be the favorite
    private Listing listing2; // This will NOT be the favorite

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setKeycloakId("test-id");
        owner.setEmail("test@example.com");
        entityManager.persist(owner);

        listing1 = new Listing();
        listing1.setTitle("Test 1");
        listing1.setDescription("Desc 1");
        listing1.setCity("City A");
        listing1.setStatus(ListingStatus.APPROVED);
        listing1.setCreatedAt(Instant.now().minusSeconds(15 * 24 * 3600));
        listing1.setOwner(owner);
        entityManager.persist(listing1);

        listing2 = new Listing();
        listing2.setTitle("Test 2");
        listing2.setDescription("Desc 2");
        listing2.setCity("City B");
        listing2.setStatus(ListingStatus.APPROVED);
        listing2.setCreatedAt(Instant.now().minusSeconds(5 * 24 * 3600));
        listing2.setOwner(owner);
        entityManager.persist(listing2);

        // Create the favorite relationship
        UserFavoriteListing favorite1 = new UserFavoriteListing(owner, listing1);
        listing1.addFavorite(favorite1);

        entityManager.flush();
        // Clear the persistence context so subsequent find operations hit the DB,
        // not the cache. (Crucial for testing projections/updates).
        entityManager.clear();
    }

    @Test
    void testFindFavoriteListingIdsByUserId_ShouldReturnCorrectIdSet() {
        // Act
        Set<Long> favoriteListingIds = userFavoriteListingRepository.findFavoriteListingIdsByUserId(owner.getId());

        // Assert
        // Check if the set size is correct (only one favorite expected)
        assertThat(favoriteListingIds).as("Check that only one listing ID was returned").hasSize(1);

        // Check if the set contains the ID of listing1 (the favorited one)
        assertThat(favoriteListingIds).as("Check if the set contains the ID of listing1")
            .containsExactly(listing1.getId());

        // Ensure it does NOT contain the ID of listing2 (the non-favorited one)
        assertThat(favoriteListingIds).as("Check if the set does NOT contain the ID of listing2")
            .doesNotContain(listing2.getId());
    }

    @Test
    void testFindByUserAndListing_ShouldFindExistingFavorite() {
        // Arrange: Re-fetch entities to ensure they are detached if necessary,
        // though the clear() in setUp should handle this.
        User user = entityManager.find(User.class, owner.getId());
        Listing listing = entityManager.find(Listing.class, listing1.getId());

        // Act
        Optional<UserFavoriteListing> foundFavorite = userFavoriteListingRepository.findByUserAndListing(user, listing);

        assertThat(foundFavorite).isPresent();
        assertThat(foundFavorite.get().getListing().getId()).isEqualTo(listing1.getId());
        assertThat(foundFavorite.get().getUser().getId()).isEqualTo(owner.getId());
    }
}
