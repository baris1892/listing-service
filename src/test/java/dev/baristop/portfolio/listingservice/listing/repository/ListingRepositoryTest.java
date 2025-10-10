package dev.baristop.portfolio.listingservice.listing.repository;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.security.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ListingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ListingRepository listingRepository;

    private Listing listing1;
    private Listing listing2;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setKeycloakId("test-id");
        owner.setEmail("test@example.com");
        entityManager.persist(owner);

        listing1 = new Listing();
        listing1.setTitle("Test 1");
        listing1.setDescription("Desc 1");
        listing1.setCity("City A");
        listing1.setStatus(ListingStatus.APPROVED);
        listing1.setCreatedAt(Instant.now().minusSeconds(15 * 24 * 3600)); // 15 Tage alt
        listing1.setOwner(owner);
        entityManager.persist(listing1);

        listing2 = new Listing();
        listing2.setTitle("Test 2");
        listing2.setDescription("Desc 2");
        listing2.setCity("City B");
        listing2.setStatus(ListingStatus.APPROVED);
        listing2.setCreatedAt(Instant.now().minusSeconds(5 * 24 * 3600)); // 5 Tage alt
        listing2.setOwner(owner);
        entityManager.persist(listing2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testUpdateStatusOlderThan() {
        Instant threshold = Instant.now().minusSeconds(10 * 24 * 3600);

        int updated = listingRepository.updateStatusOlderThan(
            threshold,
            ListingStatus.APPROVED,
            ListingStatus.INACTIVE,
            Instant.now()
        );

        assertThat(updated).isEqualTo(1);

        Listing refreshed = listingRepository.findById(listing1.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(ListingStatus.INACTIVE);

        Listing notUpdated = listingRepository.findById(listing2.getId()).orElseThrow();
        assertThat(notUpdated.getStatus()).isEqualTo(ListingStatus.APPROVED);
    }

    @Test
    void testFindByTitleAndDescriptionAndCity() {
        Optional<Listing> found = listingRepository.findByTitleAndDescriptionAndCity(
            "Test 1", "Desc 1", "City A"
        );

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(listing1.getId());
    }
}
