package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.*;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import dev.baristop.portfolio.listingservice.response.ErrorResponse;
import dev.baristop.portfolio.listingservice.response.PaginatedResponse;
import dev.baristop.portfolio.listingservice.response.SuccessResponse;
import dev.baristop.portfolio.listingservice.response.ValidationErrorResponse;
import dev.baristop.portfolio.listingservice.security.annotation.CurrentUser;
import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/listings")
@AllArgsConstructor
@Validated
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @Secured({Role.USER})
    @Operation(
        summary = "Create a new listing",
        description = "Creates a new listing with the provided data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Listing created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation errors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidationErrorResponse.class)
            )
        )
    })
    public ResponseEntity<ListingCreateResponse> createListing(
        @Parameter(description = "Listing data to create", required = true) @Valid @RequestBody ListingCreateRequest listingCreateRequest,
        @CurrentUser User user
    ) {
        Listing listing = listingService.createListing(listingCreateRequest, user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                new ListingCreateResponse(
                    "Listing created successfully",
                    HttpStatus.CREATED.value(),
                    listing.getId()
                )
            );
    }

    @PutMapping("/{id}")
    @Secured({Role.USER})
    @Operation(summary = "Update existing listing", description = "Updates an existing listing with the provided data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listing updated successfully"),
        @ApiResponse(responseCode = "404", description = "Listing not found")
    })
    public ResponseEntity<SuccessResponse> updateListing(
        @Parameter(description = "ID of the listing to update", required = true) @PathVariable Long id,
        @Parameter(description = "Updated listing data", required = true) @RequestBody ListingUpdateRequest updateRequest,
        @CurrentUser User user
    ) {
        listingService.updateListing(id, updateRequest, user);

        return ResponseEntity.ok(
            new SuccessResponse("Listing updated successfully", HttpStatus.OK.value())
        );
    }

    @DeleteMapping("/{id}")
    @Secured({Role.USER})
    @Operation(
        summary = "Delete a listing",
        description = "Deletes the listing. Only own listings can be deleted",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listing deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Listing not found")
    })
    public ResponseEntity<SuccessResponse> deleteListing(
        @Parameter(description = "ID of the listing to delete", required = true) @PathVariable Long id,
        @CurrentUser UserPrincipal userPrincipal
    ) {
        listingService.deleteListing(id, userPrincipal);

        return ResponseEntity.ok(
            new SuccessResponse("Listing deleted successfully", HttpStatus.OK.value())
        );
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get listing by ID",
        description = "Returns the listing identified by the given ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listing found"),
        @ApiResponse(
            responseCode = "404",
            description = "Listing not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<ListingDto> getListing(
        @Parameter(description = "ID of the listing to retrieve", required = true) @PathVariable Long id,
        @CurrentUser UserPrincipal currentUser
    ) {
        ListingDto dto = listingService.getListingById(id, currentUser);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(
        summary = "Get all public listings",
        description = "Returns paginated list of listings based on query parameters"
    )
    public PaginatedResponse<ListingDto> getAllListings(
        @Parameter(description = "Query parameters for filtering listings")
        @Valid ListingQueryRequestDto listingQueryRequestDto
    ) {
        Page<ListingDto> resultPage = listingService.getAllListings(listingQueryRequestDto);

        return new PaginatedResponse<>(resultPage);
    }

}
