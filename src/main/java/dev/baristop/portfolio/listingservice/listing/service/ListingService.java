package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.exception.InvalidListingStateException;
import dev.baristop.portfolio.listingservice.exception.ResourceNotFoundException;
import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.dto.ListingUpdateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.util.ValidationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;

    private final ValidationUtil validationUtil;

    public Listing createListing(
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

        return listing;
    }

    public void updateListing(Long id, ListingUpdateRequest updateRequest, User user) {
        Listing existingListing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing with ID " + id + " not found"));

        if (!existingListing.isOwner(user)) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }

        if (!existingListing.isStatusPending()) {
            throw new InvalidListingStateException("Only pending listings can be updated");
        }

        validationUtil.validate(updateRequest);

        existingListing.setTitle(updateRequest.getTitle());
        existingListing.setDescription(updateRequest.getDescription());
        existingListing.setPrice(updateRequest.getPrice());
        existingListing.setCity(updateRequest.getCity());

        listingRepository.save(existingListing);
    }

    public void deleteListing(Long id, UserPrincipal userPrincipal) {
        Listing existingListing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing with ID " + id + " not found"));

        boolean isOwner = existingListing.isOwner(userPrincipal);
        boolean isAdmin = userPrincipal.isAdmin();
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }

        listingRepository.delete(existingListing);
    }
}
