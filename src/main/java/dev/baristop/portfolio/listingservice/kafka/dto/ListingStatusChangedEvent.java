package dev.baristop.portfolio.listingservice.kafka.dto;

import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ListingStatusChangedEvent {
    private ListingStatus status;
    private String recipientEmail;
    private Long listingId;
    private String listingTitle;
    private String listingDescription;
}
