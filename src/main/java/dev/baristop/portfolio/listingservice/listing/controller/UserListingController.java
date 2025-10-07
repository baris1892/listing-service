package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.dto.ListingQueryRequestDto;
import dev.baristop.portfolio.listingservice.listing.repository.UserFavoriteListingRepository;
import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import dev.baristop.portfolio.listingservice.response.PaginatedResponse;
import dev.baristop.portfolio.listingservice.security.annotation.CurrentUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/users/me")
@AllArgsConstructor
@Validated
public class UserListingController {

    private final ListingService listingService;
    private final UserFavoriteListingRepository favoriteRepository;

    @GetMapping("/listings")
    @Secured({Role.USER})
    @Operation(
        summary = "Get all public listings",
        description = "Returns paginated list of listings based on query parameters",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public PaginatedResponse<ListingDto> getMyListings(
        @Parameter(description = "Query parameters for filtering listings") @Valid ListingQueryRequestDto listingQueryRequestDto,
        @CurrentUser User user
    ) {
        listingQueryRequestDto.updateOwner(user);

        Page<ListingDto> resultPage = listingService.getAllListings(listingQueryRequestDto, user);

        return new PaginatedResponse<>(resultPage);
    }

    @GetMapping("/favorites")
    @Secured({Role.USER})
    @Operation(
        summary = "Get all favorited listings",
        description = "Returns a paginated list of listings that the current user has favorited",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public PaginatedResponse<ListingDto> getMyFavoriteListings(
        @Parameter(description = "Query parameters for filtering listings") @Valid ListingQueryRequestDto listingQueryRequestDto,
        @CurrentUser User user
    ) {
        // Pass the current user so that "isFavorite" flags are set correctly
        Page<ListingDto> resultPage = listingService.getAllListings(listingQueryRequestDto, user);

        Set<Long> favoriteIds = favoriteRepository.findFavoriteListingIdsByUserId(user.getId());

        List<ListingDto> filtered = resultPage.stream()
            .filter(dto -> favoriteIds.contains(dto.getId()))
            .toList();

        Page<ListingDto> filteredPage = new PageImpl<>(filtered, resultPage.getPageable(), filtered.size());

        return new PaginatedResponse<>(filteredPage);
    }
}
