package dev.baristop.portfolio.listingservice.listing.dto;

import dev.baristop.portfolio.listingservice.dto.PaginationRequestDto;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class ListingQueryRequestDto extends PaginationRequestDto {

    @Schema(hidden = true)
    @Setter(AccessLevel.NONE) // prevent setting via query params (e.g. WebDataBinder)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Schema(description = "Filter by title", example = "Google Pixel 8")
    private String title;

    @Schema(description = "Filter by description", example = "good condition")
    private String description;

    @Schema(description = "Filter by city", example = "Saarlouis")
    private String city;

    @Schema(description = "Filter by price from", example = "20")
    @DecimalMin(value = "0.0", message = "priceFrom must be positive")
    private BigDecimal priceFrom;

    @Schema(description = "Filter by price to", example = "100")
    @DecimalMin(value = "0.0", message = "priceTo must be positive")
    private BigDecimal priceTo;

    // No public setter for status to avoid external manipulation.
    // This method allows controlled internal updates (e.g. by service or controller).
    public void updateStatus(ListingStatus newStatus) {
        this.status = newStatus;
    }
}
