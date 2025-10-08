package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.exception.InvalidListingStateException;
import dev.baristop.portfolio.listingservice.exception.ResourceNotFoundException;
import dev.baristop.portfolio.listingservice.kafka.ListingStatusProducer;
import dev.baristop.portfolio.listingservice.kafka.dto.ListingStatusChangedEvent;
import dev.baristop.portfolio.listingservice.listing.dto.ListingCreateRequest;
import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.dto.ListingQueryRequestDto;
import dev.baristop.portfolio.listingservice.listing.dto.ListingUpdateRequest;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.mapper.ListingMapper;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.listing.repository.UserFavoriteListingRepository;
import dev.baristop.portfolio.listingservice.listing.specification.ListingSpecification;
import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.util.ValidationUtil;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserFavoriteListingRepository favoriteRepository;
    private final ValidationUtil validationUtil;
    private final ListingMapper listingMapper;
    private final ListingStatusProducer listingStatusProducer;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "price");

    public Listing createListing(
        ListingCreateRequest listingCreateRequest,
        User owner
    ) {
        Listing listing = new Listing();
        listing.setTitle(listingCreateRequest.getTitle());
        listing.setDescription(listingCreateRequest.getDescription());
        listing.setPrice(listingCreateRequest.getPrice());
        listing.setCreatedAt(Instant.now());
        listing.setCity(listingCreateRequest.getCity());
        listing.setOwner(owner);

        listingRepository.save(listing);
        log.info("Created new listing with id={}", listing.getId());

        return listing;
    }

    public void updateListing(Long listingId, ListingUpdateRequest updateRequest, User user) {
        Listing existingListing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing with ID " + listingId + " not found"));

        if (!existingListing.isOwner(user)) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }

        if (!existingListing.isStatusPending()) {
            throw new InvalidListingStateException("Only pending listings can be updated");
        }

        validationUtil.validate(updateRequest);

        existingListing.setTitle(updateRequest.getTitle());
        existingListing.setDescription(updateRequest.getDescription());
        existingListing.setPrice(updateRequest.getPrice());
        existingListing.setCity(updateRequest.getCity());

        listingRepository.save(existingListing);
    }

    public void deleteListing(Long listingId, UserPrincipal userPrincipal) {
        Listing existingListing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing with ID " + listingId + " not found"));

        boolean isOwner = existingListing.isOwner(userPrincipal);
        boolean isAdmin = userPrincipal.isAdmin();
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }

        listingRepository.delete(existingListing);
    }

    /**
     * Returns the listing identified by the given ID.
     * <p>
     * Only non-PENDING listings are visible to the public.
     * Owners and admins can view all listings, including PENDING ones.
     *
     * @param listingId     the ID of the listing to retrieve
     * @param userPrincipal the currently authenticated user, or null if unauthenticated
     *
     * @return the listing DTO
     *
     * @throws ResourceNotFoundException if the listing does not exist or is pending and the user is not authorized
     */
    public ListingDto getListingById(Long listingId, @Nullable UserPrincipal userPrincipal) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing with ID " + listingId + " not found"));

        boolean isOwner = userPrincipal != null && listing.isOwner(userPrincipal);
        boolean isAdmin = userPrincipal != null && userPrincipal.isAdmin();
        if (listing.isStatusPending() && !isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Listing with ID " + listingId + " not found");
        }

        return listingMapper.toDto(listing);
    }

    public Page<ListingDto> getAllListings(
        ListingQueryRequestDto request,
        @Nullable User currentUser
    ) {
        if (!ALLOWED_SORT_FIELDS.contains(request.getSortBy())) {
            throw new IllegalArgumentException(
                "Invalid sortBy field: " + request.getSortBy()
            );
        }

        // Build sorting
        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
            ? Sort.by(request.getSortBy()).ascending()
            : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(request.calculateZeroBasedPage(), request.getSize(), sort);

        // Build Specification using the unified DTO
        Specification<Listing> spec = ListingSpecification.withFilters(request);

        Page<Listing> listingPage = listingRepository.findAll(spec, pageable);

        // add "favorite" flag for favorized listings of currentUser
        Set<Long> favoriteListingIds = currentUser != null
            ? favoriteRepository.findFavoriteListingIdsByUserId(currentUser.getId())
            : Set.of();

        return listingPage.map(listing -> {
            ListingDto dto = listingMapper.toDto(listing);

            dto.setIsFavorite(currentUser != null
                ? favoriteListingIds.contains(listing.getId())
                : null
            );

            return dto;
        });
    }

    public Listing updateListingStatus(Long listingId, ListingStatus status) {
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

        return listing;
    }
}
