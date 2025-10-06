package dev.baristop.portfolio.listingservice.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ListingCreateResponse {
    private String message;
    private int status;
    private Long listingId;
}
