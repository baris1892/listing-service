package dev.baristop.portfolio.listingservice.listing.repository;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.UserFavoriteListing;
import dev.baristop.portfolio.listingservice.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserFavoriteListingRepository extends JpaRepository<UserFavoriteListing, Long> {
    Optional<UserFavoriteListing> findByUserAndListing(User user, Listing listing);

    @Query("SELECT f.listing.id FROM UserFavoriteListing f WHERE f.user.id = :userId")
    Set<Long> findFavoriteListingIdsByUserId(@Param("userId") Long userId);
}
