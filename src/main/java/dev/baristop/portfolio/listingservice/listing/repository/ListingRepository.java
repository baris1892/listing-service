package dev.baristop.portfolio.listingservice.listing.repository;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Optional<Listing> findByTitleAndDescriptionAndCity(String title, String description, String city);

}
