package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.exception.InvalidListingStateException;
import dev.baristop.portfolio.listingservice.exception.ResourceNotFoundException;
import dev.baristop.portfolio.listingservice.kafka.ListingStatusProducer;
import dev.baristop.portfolio.listingservice.kafka.dto.ListingStatusChangedEvent;
import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.mapper.ListingMapper;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ListingStatusService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final ListingStatusProducer listingStatusProducer;

    @Transactional
    @CachePut(value = "listings", key = "#listingId")
    public ListingDto updateListingStatus(Long listingId, ListingStatus status) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        if (listing.getStatus() != ListingStatus.PENDING) {
            throw new InvalidListingStateException("Only pending listings can be updated");
        }

        listing.setStatus(status);
        listingRepository.save(listing);

        // produce kafka event
        ListingStatusChangedEvent event = new ListingStatusChangedEvent(
            listing.getStatus(),
            listing.getOwner().getEmail(),
            listing.getId(),
            listing.getTitle(),
            listing.getDescription()
        );
        listingStatusProducer.sendListingStatusEvent(event);

        return listingMapper.toDto(listing);
    }
}
