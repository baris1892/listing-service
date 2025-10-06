package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateResponse;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import dev.baristop.portfolio.listingservice.response.ValidationErrorResponse;
import dev.baristop.portfolio.listingservice.security.annotation.CurrentUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        @Parameter(description = "Listing data to create", required = true)
        @Valid @RequestBody ListingCreateRequest listingCreateRequest,

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
}
