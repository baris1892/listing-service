package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.UserFavoriteListing;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.listing.repository.UserFavoriteListingRepository;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteService {

    private final UserFavoriteListingRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Transactional
    public boolean toggleFavorite(Long userId, Long listingId) {
        User user = userRepository.getReferenceById(userId);
        Listing listing = listingRepository.getReferenceById(listingId);

        Optional<UserFavoriteListing> existing = favoriteRepository.findByUserAndListing(user, listing);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            log.info("Removed favorite: userId={} listingId={}", userId, listingId);

            return false;
        } else {
            favoriteRepository.save(
                new UserFavoriteListing(user, listing)
            );
            log.info("Added favorite: userId={} listingId={}", userId, listingId);

            return true;
        }
    }
}
