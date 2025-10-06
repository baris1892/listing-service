package dev.baristop.portfolio.listingservice.testdata;

import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.dto.ListingUpdateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class ListingTestFactory {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    public Listing sampleListing(String title, User owner) {
        if (owner == null) {
            owner = createUser("dummy-keycloak-id");
        }

        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription("This is a test listing");
        listing.setPrice(BigDecimal.valueOf(100));
        listing.setCity("Saarlouis");
        listing.setStatus(ListingStatus.PENDING);
        listing.setOwner(owner);
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());

        return listingRepository.saveAndFlush(listing);
    }

    public ListingCreateRequest defaultListingCreateRequest() {
        ListingCreateRequest request = new ListingCreateRequest();
        request.setTitle("Test Title");
        request.setDescription("Test Description");
        request.setCity("Test City");
        request.setPrice(BigDecimal.valueOf(100));

        return request;
    }

    public ListingUpdateRequest defaultListingUpdateRequest() {
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCity("Updated City");
        updateRequest.setPrice(BigDecimal.valueOf(123));

        return updateRequest;
    }

    public Listing createDefaultListing() {
        User owner = createDefaultUser();

        Listing listing = new Listing();
        listing.setTitle("Test Title");
        listing.setDescription("Test Description");
        listing.setCity("Test City");
        listing.setPrice(BigDecimal.valueOf(100));
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.PENDING);

        return listingRepository.save(listing);
    }

    public User createUser(String id) {
        User user = new User();
        user.setKeycloakId(id);

        return userRepository.saveAndFlush(user);
    }

    public User createDefaultUser() {
        return createUser("test-id");
    }
}
