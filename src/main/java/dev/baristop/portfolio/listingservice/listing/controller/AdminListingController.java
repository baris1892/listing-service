package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingStatusResponse;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import dev.baristop.portfolio.listingservice.security.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/listings")
@RequiredArgsConstructor
@Secured({Role.ADMIN})
public class AdminListingController {

    private final ListingService listingService;

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ListingStatusResponse> approveListing(@PathVariable Long id) {
        Listing updated = listingService.updateListingStatus(id, ListingStatus.APPROVED);

        return ResponseEntity.ok(new ListingStatusResponse(updated.getStatus()));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ListingStatusResponse> rejectListing(@PathVariable Long id) {
        Listing updated = listingService.updateListingStatus(id, ListingStatus.REJECTED);

        return ResponseEntity.ok(new ListingStatusResponse(updated.getStatus()));
    }
}