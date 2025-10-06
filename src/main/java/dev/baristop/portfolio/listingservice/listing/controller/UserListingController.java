package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.dto.ListingQueryRequestDto;
import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import dev.baristop.portfolio.listingservice.response.PaginatedResponse;
import dev.baristop.portfolio.listingservice.security.annotation.CurrentUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@AllArgsConstructor
@Validated
public class UserListingController {

    private final ListingService listingService;

    @GetMapping("/listings")
    @Secured({Role.USER})
    @Operation(
        summary = "Get all public listings",
        description = "Returns paginated list of listings based on query parameters"
    )
    public PaginatedResponse<ListingDto> getMyListings(
        @Parameter(description = "Query parameters for filtering listings") @Valid ListingQueryRequestDto listingQueryRequestDto,
        @CurrentUser User user
    ) {
        listingQueryRequestDto.updateUser(user);

        Page<ListingDto> resultPage = listingService.getAllListings(listingQueryRequestDto);

        return new PaginatedResponse<>(resultPage);
    }

}
