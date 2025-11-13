package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.listing.dto.ToggleFavoriteResponse;
import dev.baristop.portfolio.listingservice.listing.service.FavoriteService;
import dev.baristop.portfolio.listingservice.security.annotation.CurrentUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listings")
@AllArgsConstructor
@Validated
@Tag(name = "Listing", description = "Operations related to listings and favorites")
public class ListingFavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{listingId}/toggle-favorite")
    @Secured({Role.USER})
    @Operation(
        summary = "Toggle favorite listing",
        description = "Marks/Unmarks a favorite listing",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ToggleFavoriteResponse toggleFavorite(
        @PathVariable Long listingId,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        boolean isFavorite = favoriteService.toggleFavorite(user.getId(), listingId);

        return new ToggleFavoriteResponse(isFavorite);
    }
}
