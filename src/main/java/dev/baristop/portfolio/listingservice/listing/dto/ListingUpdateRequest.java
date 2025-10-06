package dev.baristop.portfolio.listingservice.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Request body for updating a listing")
public class ListingUpdateRequest {

    @NotNull
    @Size(min = 2, max = 255)
    @Schema(
        description = "Title of the listing",
        example = "Used iPhone 14"
    )
    private String title;

    @NotNull
    @Size(min = 2, max = 2000)
    @Schema(
        description = "Detailed description of the listing",
        example = "Used iPhone 14 in good conditions"
    )
    private String description;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Price in EUR", example = "1200.00")
    private BigDecimal price;

    @NotNull
    @Size(min = 2, max = 255)
    @Schema(description = "City where the listing is located", example = "Saarlouis")
    private String city;
}
