package dev.baristop.portfolio.listingservice.listing.repository;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.UserFavoriteListing;
import dev.baristop.portfolio.listingservice.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFavoriteListingRepository extends JpaRepository<UserFavoriteListing, Long> {
    Optional<UserFavoriteListing> findByUserAndListing(User user, Listing listing);
}
