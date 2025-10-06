package dev.baristop.portfolio.listingservice.listing.dto;

import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ListingDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String city;
    private ListingStatus status;
    private Instant createdAt;
}
