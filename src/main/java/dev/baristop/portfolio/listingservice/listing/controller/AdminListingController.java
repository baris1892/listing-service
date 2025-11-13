package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.dto.ListingStatusResponse;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import dev.baristop.portfolio.listingservice.security.util.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Listing", description = "Admin operations on listings")
public class AdminListingController {

    private final ListingService listingService;

    @PatchMapping("/{id}/approve")
    @Operation(
        summary = "Approves a listing",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ResponseEntity<ListingStatusResponse> approveListing(@PathVariable Long id) {
        return updateStatus(id, ListingStatus.APPROVED);
    }

    @Operation(
        summary = "Rejects a listing",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ListingStatusResponse> rejectListing(@PathVariable Long id) {
        return updateStatus(id, ListingStatus.REJECTED);
    }

    private ResponseEntity<ListingStatusResponse> updateStatus(Long id, ListingStatus status) {
        ListingDto updated = listingService.updateListingStatus(id, status);
        return ResponseEntity.ok(new ListingStatusResponse(updated.getStatus()));
    }
}