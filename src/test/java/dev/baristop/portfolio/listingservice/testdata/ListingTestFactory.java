package dev.baristop.portfolio.listingservice.testdata;

import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.dto.ListingUpdateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ListingTestFactory {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserTestFactory userTestFactory;

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
        User owner = userTestFactory.createDefaultUser();

        Listing listing = new Listing();
        listing.setTitle("Test Title");
        listing.setDescription("Test Description");
        listing.setCity("Test City");
        listing.setPrice(BigDecimal.valueOf(100));
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.PENDING);

        return listingRepository.save(listing);
    }

    public List<Listing> prepareDataForAllListings(User user) {
        listingRepository.deleteAll();
        List<Listing> listings = List.of(
            new Listing("Google Pixel 8", "good condition", new BigDecimal("500"), "Saarlouis", ListingStatus.PENDING, user),
            new Listing("iPhone 14", "like new", new BigDecimal("800"), "Augsburg", ListingStatus.PENDING, user),
            new Listing("Galaxy S23", "used", new BigDecimal("400"), "Karlsruhe", ListingStatus.ACTIVE, user),
            new Listing("Galaxy S22", "like new", new BigDecimal("550"), "Karlsruhe", ListingStatus.ACTIVE, user)
        );

        return listingRepository.saveAll(listings);
    }
}
