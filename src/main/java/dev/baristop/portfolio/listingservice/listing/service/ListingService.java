package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;

    public void createListing(
        ListingCreateRequest listingCreateRequest,
        User owner
    ) {
        Listing listing = new Listing();
        listing.setTitle(listingCreateRequest.getTitle());
        listing.setDescription(listingCreateRequest.getDescription());
        listing.setPrice(listingCreateRequest.getPrice());
        listing.setCreatedAt(Instant.now());
        listing.setCity(listingCreateRequest.getCity());
        listing.setOwner(owner);

        listingRepository.save(listing);
        log.info("Created new listing with id={}", listing.getId());
    }

}
